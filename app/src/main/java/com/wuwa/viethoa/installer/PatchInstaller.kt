package com.wuwa.viethoa.installer

import android.content.Context
import com.wuwa.viethoa.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

enum class NameStyle(val displayName: String, val assetKeyword: String, val targetFilename: String) {
    ENGLISH("Tên quốc tế (Jinhsi, Changli)", "EN", "WuWaVH_99_P.pak"),
    HAN_VIET("Tên Hán Việt (Kim Tịch, Trường Ly)", "HanViet", "WuWaVH_99_P.pak")
}

enum class FontOption(val displayName: String, val assetKeyword: String?, val targetFilename: String) {
    LEXEND("Phông chữ Lexend (Khuyên dùng)", "Font", "WahuFont_100_P.pak"),
    SEGOE_UI("Phông chữ Segoe UI", "Segoe", "WahuFont_100_P.pak"),
    NONE("Phông mặc định của game", null, "WahuFont_100_P.pak")
}

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long
)

object PatchInstaller {

    // Kho lưu trữ phát hành bản dịch
    var githubRepo: String = "ZDUONGZ/WuWa_VietHoa"

    // Fallback CDN nếu không lấy được từ GitHub Releases API
    private const val FALLBACK_RELEASE_URL = "https://github.com/ZDUONGZ/WuWa_VietHoa/releases/latest/download"

    fun findMatchingPakAsset(assets: List<ReleaseAsset>, nameStyle: NameStyle): ReleaseAsset? {
        return assets.find { asset ->
            if (!asset.name.endsWith(".pak", ignoreCase = true)) return@find false
            when (nameStyle) {
                NameStyle.ENGLISH -> {
                    asset.name.contains("_EN_", ignoreCase = true) ||
                    asset.name.contains("WuWaVH_EN", ignoreCase = true) ||
                    (asset.name.contains("EN", ignoreCase = false) && !asset.name.contains("HanViet", ignoreCase = true))
                }
                NameStyle.HAN_VIET -> {
                    asset.name.contains("HanViet", ignoreCase = true) ||
                    asset.name.contains("HV", ignoreCase = false)
                }
            }
        }
    }

    fun findMatchingFontAsset(assets: List<ReleaseAsset>, fontOption: FontOption): ReleaseAsset? {
        if (fontOption == FontOption.NONE) return null
        return assets.find { asset ->
            if (!asset.name.endsWith(".pak", ignoreCase = true)) return@find false
            when (fontOption) {
                FontOption.SEGOE_UI -> asset.name.contains("Segoe", ignoreCase = true)
                FontOption.LEXEND -> {
                    asset.name.contains("Lexend", ignoreCase = true) ||
                    (asset.name.contains("Font", ignoreCase = true) && !asset.name.contains("Segoe", ignoreCase = true))
                }
                FontOption.NONE -> false
            }
        }
    }

    suspend fun installPatch(
        context: Context,
        nameStyle: NameStyle,
        fontOption: FontOption,
        onProgress: (status: String, progressPercent: Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        val localPakFile = File(cacheDir, "patch_temp.pak")
        val localFontFile = File(cacheDir, "font_temp.pak")

        try {
            onProgress("1/4: Đang chuẩn bị thư mục mods trên Android...", -1f)
            val mkdirResult = ShizukuManager.executeCommand("mkdir -p \"${GameDetector.MODS_PATH}\"")
            if (!mkdirResult.isSuccess) {
                onProgress("Lỗi tạo thư mục mods: ${mkdirResult.stderr}", 0f)
                return@withContext false
            }

            // 1. Lấy thông tin Asset mới nhất từ GitHub Releases
            onProgress("2/4: Đang kiểm tra bản dịch mới nhất trên GitHub...", -1f)
            val releaseAssets = fetchLatestReleaseAssets(githubRepo)

            // Tìm asset phù hợp theo kiểu tên
            val pakAsset = findMatchingPakAsset(releaseAssets, nameStyle)

            val downloadUrl = pakAsset?.downloadUrl 
                ?: "$FALLBACK_RELEASE_URL/WuWaVH_${nameStyle.assetKeyword}_99_P.pak"

            onProgress("Đang tải gói dịch (${nameStyle.displayName})...", 0f)
            val downloaded = downloadFileWithProgress(downloadUrl, localPakFile) { percent ->
                onProgress("Đang tải dữ liệu: $percent%", percent / 100f)
            }

            if (!downloaded || !localPakFile.exists() || localPakFile.length() < 1024) {
                onProgress("Lỗi: Không thể tải bản dịch từ GitHub. Vui lòng kiểm tra kết nối mạng.", 0f)
                return@withContext false
            }

            // 2. Nạp file qua Shizuku
            onProgress("3/4: Đang nạp bản dịch vào game qua Shizuku...", -1f)
            val copyCmd = "cp -f \"${localPakFile.absolutePath}\" \"${GameDetector.MODS_PATH}/${nameStyle.targetFilename}\""
            val copyResult = ShizukuManager.executeCommand(copyCmd)

            if (!copyResult.isSuccess) {
                onProgress("Lỗi sao chép file: ${copyResult.stderr}", 0f)
                return@withContext false
            }

            // 3. Xử lý Font
            if (fontOption.assetKeyword != null) {
                onProgress("4/4: Đang cấu hình phông chữ (${fontOption.displayName})...", -1f)
                val fontAsset = findMatchingFontAsset(releaseAssets, fontOption)
                val fontUrl = fontAsset?.downloadUrl ?: "$FALLBACK_RELEASE_URL/WahuFont_100_P.pak"

                if (downloadFileWithProgress(fontUrl, localFontFile) { percent ->
                    onProgress("Đang tải phông chữ: $percent%", percent / 100f)
                }) {
                    val copyFontCmd = "cp -f \"${localFontFile.absolutePath}\" \"${GameDetector.MODS_PATH}/${fontOption.targetFilename}\""
                    val fontResult = ShizukuManager.executeCommand(copyFontCmd)
                    if (!fontResult.isSuccess) {
                        onProgress("Lỗi sao chép font: ${fontResult.stderr}", 0f)
                    }
                }
            } else {
                // Xóa font mod nếu chọn mặc định
                ShizukuManager.executeCommand("rm -f \"${GameDetector.MODS_PATH}/WahuFont_\"*.pak")
            }

            onProgress("Hoàn tất! Hãy mở Wuthering Waves, vào Cài đặt -> Ngôn ngữ -> Chọn English.", 1f)
            true
        } catch (e: Exception) {
            onProgress("Lỗi: ${e.localizedMessage}", 0f)
            false
        } finally {
            localPakFile.delete()
            localFontFile.delete()
        }
    }

    suspend fun uninstallPatch(onProgress: (status: String, progressPercent: Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress("Đang xóa bản Việt Hóa...", -1f)
            val rmCmd = "rm -f \"${GameDetector.MODS_PATH}/WuWaVH_\"*.pak \"${GameDetector.MODS_PATH}/WahuFont_\"*.pak && (rmdir \"${GameDetector.MODS_PATH}\" 2>/dev/null || true)"
            val rmResult = ShizukuManager.executeCommand(rmCmd)
            if (rmResult.isSuccess) {
                onProgress("Đã gỡ bỏ bản dịch hoàn toàn. Game đã quay lại ngôn ngữ gốc!", 1f)
                true
            } else {
                onProgress("Lỗi khi gỡ: ${rmResult.stderr}", 0f)
                false
            }
        } catch (e: Exception) {
            onProgress("Lỗi: ${e.localizedMessage}", 0f)
            false
        }
    }

    // Lấy danh sách link tải assets từ GitHub Releases API
    private fun fetchLatestReleaseAssets(repo: String): List<ReleaseAsset> {
        val list = mutableListOf<ReleaseAsset>()
        var conn: HttpURLConnection? = null
        try {
            val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
            conn = URL(apiUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "WuWa-VietHoa-Mobile")
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().readText()
                val root = JSONObject(jsonStr)
                val assets = root.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val obj = assets.getJSONObject(i)
                        list.add(
                            ReleaseAsset(
                                name = obj.getString("name"),
                                downloadUrl = obj.getString("browser_download_url"),
                                size = obj.optLong("size", 0L)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Không chặn tiến trình nếu GitHub API bị rate-limit, sẽ dùng URL fallback trực tiếp
        } finally {
            conn?.disconnect()
        }
        return list
    }

    // Tải file có hỗ trợ tự động chuyển hướng HTTP 302/301 của GitHub và cập nhật % tiến trình
    private fun downloadFileWithProgress(
        urlString: String,
        outputFile: File,
        onProgress: (Int) -> Unit
    ): Boolean {
        var currentUrl = urlString
        var redirectCount = 0
        val maxRedirects = 5

        try {
            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.instanceFollowRedirects = false // Tự xử lý redirect để tránh lỗi cross-protocol

                try {
                    conn.connect()
                    val code = conn.responseCode
                    if (code in 301..308) {
                        val location = conn.getHeaderField("Location") ?: return false
                        currentUrl = location
                        redirectCount++
                        continue
                    }

                    if (code !in 200..299) return false

                    val totalLength = conn.contentLengthLong
                    outputFile.parentFile?.mkdirs()

                    conn.inputStream.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            val buffer = ByteArray(65536)
                            var bytesRead: Int
                            var downloadedBytes: Long = 0
                            var lastReportedPercent = -1

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead

                                if (totalLength > 0) {
                                    val percent = ((downloadedBytes * 100) / totalLength).toInt()
                                    if (percent != lastReportedPercent) {
                                        lastReportedPercent = percent
                                        onProgress(percent)
                                    }
                                }
                            }
                        }
                    }
                    return true
                } finally {
                    conn.disconnect()
                }
            }
        } catch (_: Exception) {
            return false
        }
        return false
    }
}
