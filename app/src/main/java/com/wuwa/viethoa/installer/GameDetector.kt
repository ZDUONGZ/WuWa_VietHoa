package com.wuwa.viethoa.installer

import com.wuwa.viethoa.shizuku.ShizukuManager

data class GameStatus(
    val isInstalled: Boolean,
    val hasDownloadedData: Boolean,
    val basePath: String,
    val isPatched: Boolean
)

object GameDetector {
    val SUPPORTED_PACKAGES = listOf(
        "com.kurogame.wutheringwaves.global",
        "com.kurogame.wutheringwaves.bilibili",
        "com.kurogame.wutheringwaves"
    )
    var activePackage: String = SUPPORTED_PACKAGES[0]
    
    // Đường dẫn gốc của dữ liệu game trên Android
    val BASE_PATH get() = "/storage/emulated/0/Android/data/$activePackage/files/UE4Game/Client"
    val PAKS_PATH get() = "$BASE_PATH/Client/Content/Paks"
    val MODS_PATH get() = "$PAKS_PATH/~WuWaMods"
    val RESOURCES_PATH get() = "$BASE_PATH/Client/Saved/Resources"

    fun parseStatusTokens(tokens: List<String>): GameStatus {
        if (tokens.size >= 3 && tokens[0] != "none") {
            activePackage = tokens[0]
            return GameStatus(
                isInstalled = true,
                hasDownloadedData = tokens[1] == "1",
                basePath = BASE_PATH,
                isPatched = tokens[2] == "1"
            )
        }
        return GameStatus(
            isInstalled = false,
            hasDownloadedData = false,
            basePath = BASE_PATH,
            isPatched = false
        )
    }

    suspend fun checkStatus(
        commandRunner: suspend (String) -> com.wuwa.viethoa.shizuku.ShellResult = { ShizukuManager.executeCommand(it) }
    ): GameStatus {
        if (!ShizukuManager.hasPermission()) {
            return GameStatus(
                isInstalled = false,
                hasDownloadedData = false,
                basePath = BASE_PATH,
                isPatched = false
            )
        }

        val checkScript = buildString {
            append("for pkg in ${SUPPORTED_PACKAGES.joinToString(" ")}; do ")
            append("base=\"/storage/emulated/0/Android/data/\$pkg/files/UE4Game/Client\"; ")
            append("if [ -d \"\$base\" ]; then ")
            append("res=0; pat=0; ")
            append("[ -d \"\$base/Client/Saved/Resources\" ] && res=1; ")
            append("if [ -f \"\$base/Client/Content/Paks/~WuWaMods/WuWaVH_99_P.pak\" ] || [ -f \"\$base/Client/Content/Paks/~WuWaMods/WuWaVH_EN_99_P.pak\" ]; then pat=1; fi; ")
            append("echo \"\$pkg \$res \$pat\"; exit 0; ")
            append("fi; done; echo \"none 0 0\"")
        }

        val result = commandRunner(checkScript)
        val tokens = result.stdout.trim().split("\\s+".toRegex())
        return parseStatusTokens(tokens)
    }
}
