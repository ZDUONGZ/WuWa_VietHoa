package com.wuwa.viethoa.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

object ShizukuManager {
    const val REQUEST_CODE = 1001

    fun isRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        if (!isRunning()) return false
        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }
    }

    private val newProcessMethod by lazy {
        Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).apply { isAccessible = true }
    }

    fun requestPermission() {
        if (!isRunning() || Shizuku.isPreV11()) return
        Shizuku.requestPermission(REQUEST_CODE)
    }

    suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (!isRunning() || !hasPermission()) {
            return@withContext ShellResult(-1, "", "Shizuku chưa hoạt động hoặc chưa được cấp quyền.")
        }
        try {
            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val stdoutReader = java.io.ByteArrayOutputStream()
            val stderrReader = java.io.ByteArrayOutputStream()
            
            val outJob = launch { process.inputStream.copyTo(stdoutReader) }
            val errJob = launch { process.errorStream.copyTo(stderrReader) }
            val exitCode = process.waitFor()
            outJob.join()
            errJob.join()

            ShellResult(
                exitCode,
                stdoutReader.toString("UTF-8").trim(),
                stderrReader.toString("UTF-8").trim()
            )
        } catch (e: Exception) {
            ShellResult(-1, "", e.localizedMessage ?: "Lỗi thực thi lệnh Shell")
        }
    }
}
