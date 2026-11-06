package com.wuwa.viethoa

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.wuwa.viethoa.installer.GameDetector
import com.wuwa.viethoa.installer.GameStatus
import com.wuwa.viethoa.shizuku.ShizukuManager
import com.wuwa.viethoa.ui.HomeScreen
import com.wuwa.viethoa.ui.theme.WuWaVietHoaTheme
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private var isShizukuGranted by mutableStateOf(false)
    private var gameStatus by mutableStateOf(
        GameStatus(
            isInstalled = false,
            hasDownloadedData = false,
            basePath = GameDetector.BASE_PATH,
            isPatched = false
        )
    )

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAllStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        isShizukuGranted = false
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuManager.REQUEST_CODE) {
            isShizukuGranted = (grantResult == PackageManager.PERMISSION_GRANTED)
            if (isShizukuGranted) {
                checkAllStatus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Đăng ký lắng nghe Shizuku
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        setContent {
            WuWaVietHoaTheme {
                HomeScreen(
                    isShizukuGranted = isShizukuGranted,
                    gameStatus = gameStatus,
                    onRequestShizuku = {
                        ShizukuManager.requestPermission()
                    },
                    onRefreshStatus = {
                        checkAllStatus()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAllStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }

    private fun checkAllStatus() {
        isShizukuGranted = ShizukuManager.hasPermission()
        lifecycleScope.launch {
            gameStatus = GameDetector.checkStatus()
        }
    }
}
