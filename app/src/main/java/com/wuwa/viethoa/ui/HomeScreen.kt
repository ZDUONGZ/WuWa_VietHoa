package com.wuwa.viethoa.ui

import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wuwa.viethoa.R
import com.wuwa.viethoa.installer.FontOption
import com.wuwa.viethoa.installer.GameDetector
import com.wuwa.viethoa.installer.GameStatus
import com.wuwa.viethoa.installer.NameStyle
import com.wuwa.viethoa.installer.PatchInstaller
import com.wuwa.viethoa.ui.theme.*
import kotlinx.coroutines.launch

enum class AppNavTab {
    VIET_HOA,
    INFO_GUIDE
}

@Composable
fun HomeScreen(
    isShizukuGranted: Boolean,
    gameStatus: GameStatus,
    onRequestShizuku: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(AppNavTab.VIET_HOA) }
    var selectedNameStyle by remember { mutableStateOf(NameStyle.ENGLISH) }
    var selectedFont by remember { mutableStateOf(FontOption.LEXEND) }
    var isOperating by remember { mutableStateOf(false) }
    var progressPercent by remember { mutableStateOf(-1f) }
    var consoleLogs by remember { mutableStateOf("Hệ thống sẵn sàng. Nhấn [Cài Đặt / Cập Nhật] để bắt đầu.") }

    var isCleanView by remember { mutableStateOf(false) }
    var showConfigSection by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Fallback Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_character),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        // 2. Animated Looping Video Background (Chisa Sakura Petals)
        LoopingVideoBackground(modifier = Modifier.fillMaxSize())

        // 2. Cinematic Gradient Overlay (Translucent so live video background shines through liquid glass)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66080A0F), // status bar top shadow
                            Color(0x05080A0F), // crystal clear in center
                            Color(0x18080A0F),
                            Color(0x35080A0F), // soft ~20% tint
                            Color(0x48080A0F)  // soft ~28% tint at bottom
                        )
                    )
                )
        )

        // 3. Motion particles
        CyberParticlesMotion(modifier = Modifier.fillMaxSize())

        // 4. Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Header Bar
            AppHeaderBar(
                isShizukuGranted = isShizukuGranted,
                isCleanView = isCleanView,
                onToggleView = { isCleanView = !isCleanView }
            )

            // Audio Sync Indicator (only on Tab 1)
            if (currentTab == AppNavTab.VIET_HOA) {
                AudioWaveIndicator(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            // Main Body: Tab 1 vs Tab 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    AppNavTab.VIET_HOA -> {
                        // TRANG 1: Trang chủ tính năng Việt Hóa
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(
                                modifier = Modifier
                                    .weight(if (isCleanView) 1f else 0.15f)
                                    .animateContentSize()
                            )

                            // Liquid Glass Sheet Menu (~60% Opacity)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x991A2538), // 60% liquid glass
                                                Color(0x850E1624), // 52% translucent
                                                Color(0x99080C14)  // 60% base
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            1.5.dp,
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xB3FFFFFF), // specular top reflection
                                                    Color(0x4D00F5D4), // liquid mint refraction
                                                    Color(0x20FFFFFF)
                                                )
                                            )
                                        ),
                                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                                    )
                                    .padding(16.dp)
                                    .animateContentSize()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (!isCleanView) Modifier.verticalScroll(rememberScrollState())
                                            else Modifier
                                        ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Liquid Glass Drag Handle & Specular Shine
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .width(42.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0x66FFFFFF), Color(0xCC00F5D4), Color(0x66FFFFFF))
                                                )
                                            )
                                    )

                                    // Cài đặt CTA
                                    InstallCtaButton(
                                        isOperating = isOperating,
                                        enabled = isShizukuGranted && gameStatus.isInstalled && !isOperating,
                                        onClick = {
                                            isOperating = true
                                            progressPercent = 0f
                                            consoleLogs = "Bắt đầu quy trình nạp bản dịch..."
                                            coroutineScope.launch {
                                                PatchInstaller.installPatch(context, selectedNameStyle, selectedFont) { status, percent ->
                                                    consoleLogs = status
                                                    progressPercent = percent
                                                }
                                                isOperating = false
                                                onRefreshStatus()
                                            }
                                        }
                                    )

                                    // Khởi động Wuthering Waves
                                    if (gameStatus.isPatched && !isOperating) {
                                        LaunchGameButton(context = context)
                                    }

                                    // Cấu hình tóm tắt
                                    ConfigSummaryCard(
                                        selectedNameStyle = selectedNameStyle,
                                        selectedFont = selectedFont,
                                        expanded = showConfigSection,
                                        onToggleExpand = { showConfigSection = !showConfigSection }
                                    )

                                    // Cấu hình chi tiết
                                    AnimatedVisibility(visible = showConfigSection && !isCleanView) {
                                        DetailedConfigCard(
                                            selectedNameStyle = selectedNameStyle,
                                            onNameStyleChange = { selectedNameStyle = it },
                                            selectedFont = selectedFont,
                                            onFontChange = { selectedFont = it },
                                            enabled = !isOperating
                                        )
                                    }

                                    if (!isCleanView) {
                                        SystemStatusCards(
                                            isShizukuGranted = isShizukuGranted,
                                            gameStatus = gameStatus,
                                            onRequestShizuku = onRequestShizuku
                                        )

                                        ConsoleTerminalCard(
                                            log = consoleLogs,
                                            progress = progressPercent,
                                            isOperating = isOperating
                                        )

                                        OutlinedButton(
                                            onClick = {
                                                isOperating = true
                                                consoleLogs = "Đang gỡ bỏ bản Việt Hóa..."
                                                coroutineScope.launch {
                                                    PatchInstaller.uninstallPatch { status, percent ->
                                                        consoleLogs = status
                                                        progressPercent = percent
                                                    }
                                                    isOperating = false
                                                    onRefreshStatus()
                                                }
                                            },
                                            enabled = isShizukuGranted && gameStatus.isPatched && !isOperating,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gỡ Bỏ Việt Hóa (Về Bản Gốc)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppNavTab.INFO_GUIDE -> {
                        // TRANG 2: Thông Báo, Hướng Dẫn & Thông Tin Người Phát Triển
                        InfoAndGuideScreen()
                    }
                }
            }

            // Bottom Navigation Bar
            AppBottomNavBar(
                currentTab = currentTab,
                onTabSelect = { currentTab = it }
            )
        }
    }
}

@Composable
fun InfoAndGuideScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Ô 1: THÔNG BÁO TỪ ADMIN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassSurface)
                .border(BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.4f)), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(18.dp))
                        }
                        Text("1. THÔNG BÁO TỪ ADMIN", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryGold)
                    }
                    Text(
                        text = "MỚI NHẤT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(PrimaryGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Cập nhật Bản Dịch Tiếng Việt v3.7.x (Tương thích Wuthering Waves)",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Admin: ZDUONGZ • 04/11/2026",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x55000000))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✨ Nội dung cập nhật:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
                    Text("• Cập nhật cốt truyện chính tuyến và sự kiện mới nhất.", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                    Text("• Tối ưu font Lexend hiển thị sắc nét trên màn hình 120Hz.", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
                    Text("• Tốc độ nạp mod Shizuku siêu nhanh (chỉ 3 giây).", fontSize = 11.5.sp, color = Color(0xFFCBD5E1))

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Lưu ý: Khi game cập nhật qua Play Store, vui lòng bấm 'Gỡ Bỏ Việt Hóa' trước khi cập nhật game để tránh lỗi xung đột.",
                            fontSize = 11.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }

        // Ô 2: HƯỚNG DẪN KHỞI ĐỘNG VÀ SỬ DỤNG APP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassSurface)
                .border(BorderStroke(1.dp, PrimaryMint.copy(alpha = 0.4f)), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryMint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = PrimaryMint, modifier = Modifier.size(18.dp))
                        }
                        Text("2. HƯỚNG DẪN SỬ DỤNG", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryMint)
                    }
                    Text(
                        text = "4 BƯỚC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMint,
                        modifier = Modifier
                            .background(PrimaryMint.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                GuideStepRow(
                    stepNum = "1",
                    title = "Khởi động Shizuku trên điện thoại",
                    desc = "Mở app Shizuku → Bật Gỡ lỗi không dây (Wireless Debugging) trong Cài đặt nhà phát triển và ghép nối (Không cần Root)."
                )

                GuideStepRow(
                    stepNum = "2",
                    title = "Cấp quyền cho WuWa Việt Hóa Mobile",
                    desc = "Mở app này, nếu mục Shizuku báo 'Chưa cấp quyền', bấm nút Cấp quyền và chọn 'Cho phép luôn luôn'."
                )

                GuideStepRow(
                    stepNum = "3",
                    title = "Chọn cấu hình và bấm Cài Đặt",
                    desc = "Chọn kiểu tên (Tên Quốc Tế hoặc Hán Việt) và phông chữ, sau đó bấm nút CÀI ĐẶT / CẬP NHẬT VIỆT HÓA."
                )

                GuideStepRow(
                    stepNum = "4",
                    title = "Vào Game & Chọn Ngôn Ngữ English",
                    desc = "Mở Wuthering Waves → Cài đặt (Settings) → Ngôn ngữ (Language) → Chọn English để game hiển thị Tiếng Việt!"
                )
            }
        }

        // DƯỚI 2 Ô: PHẦN THÔNG TIN NGƯỜI PHÁT TRIỂN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xE60A0D14))
                .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text("THÔNG TIN NGƯỜI PHÁT TRIỂN", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                DevInfoRow(label = "Trưởng dự án & Đóng gói:", value = "ZDUONGZ", valueColor = PrimaryMint)
                DevInfoRow(label = "Biên dịch & Thuật ngữ:", value = "Wahu tool", valueColor = Color.White)
                DevInfoRow(label = "Cơ chế nạp Mod:", value = "Unreal Engine 4 (~WuWaMods) + Shizuku", valueColor = PrimaryGold)
                DevInfoRow(label = "Kho mã nguồn mở:", value = "github.com/ZDUONGZ", valueColor = PrimaryMint)

                Spacer(modifier = Modifier.height(4.dp))
                // Các nút liên kết Mạng xã hội
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nút Facebook
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x261877F2))
                            .border(BorderStroke(1.dp, Color(0x661877F2)), RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/nguyen.ai.duon/"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("Facebook", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                            Text("Cộng đồng & Support", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    // Nút TikTok
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x26FE2C55))
                            .border(BorderStroke(1.dp, Color(0x66FE2C55)), RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@ngduong.137"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text("TikTok", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                            Text("Video Hướng Dẫn", fontSize = 9.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryMint.copy(alpha = 0.08f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dự án phát triển phi lợi nhuận cho cộng đồng Wuthering Waves Việt Nam ❤️",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun GuideStepRow(stepNum: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x33FFFFFF))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PrimaryMint),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNum, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF081018))
        }
        Column {
            Text(title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
fun DevInfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(0.5.dp, Color(0x1AFFFFFF)))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.5.sp, color = TextSecondary)
        Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun AppBottomNavBar(
    currentTab: AppNavTab,
    onTabSelect: (AppNavTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99182334), // 60% liquid glass
                        Color(0x99090D15)  // 60% base
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x22FFFFFF),
                            Color(0x8000F5D4),
                            Color(0x99FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    )
                )
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Việt Hóa
            val isTab1 = currentTab == AppNavTab.VIET_HOA
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isTab1) Modifier
                            .background(Brush.horizontalGradient(listOf(Color(0x3300F5D4), Color(0x1800F5D4))))
                            .border(BorderStroke(1.dp, Color(0x6600F5D4)), RoundedCornerShape(14.dp))
                        else Modifier
                    )
                    .clickable { onTabSelect(AppNavTab.VIET_HOA) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = if (isTab1) PrimaryMint else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "1. Việt Hóa",
                    fontSize = 12.5.sp,
                    fontWeight = if (isTab1) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isTab1) PrimaryMint else TextSecondary
                )
            }

            // Tab 2: Thông Tin & Hướng Dẫn
            val isTab2 = currentTab == AppNavTab.INFO_GUIDE
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isTab2) Modifier
                            .background(Brush.horizontalGradient(listOf(Color(0x3300F5D4), Color(0x1800F5D4))))
                            .border(BorderStroke(1.dp, Color(0x6600F5D4)), RoundedCornerShape(14.dp))
                        else Modifier
                    )
                    .clickable { onTabSelect(AppNavTab.INFO_GUIDE) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isTab2) PrimaryMint else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "2. Hướng Dẫn",
                    fontSize = 12.5.sp,
                    fontWeight = if (isTab2) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isTab2) PrimaryMint else TextSecondary
                )
            }
        }
    }
}

@Composable
fun AppHeaderBar(
    isShizukuGranted: Boolean,
    isCleanView: Boolean,
    onToggleView: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(BorderStroke(1.5.dp, Color(0x40FFFFFF)), RoundedCornerShape(11.dp))
                    .shadow(8.dp, RoundedCornerShape(11.dp))
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "v3.7.x",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier
                            .background(PrimaryGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                    Text(
                        text = if (isShizukuGranted) "SHIZUKU ACTIVE" else "SHIZUKU CHƯA CẤP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShizukuGranted) SuccessGreen else DangerRed
                    )
                }
                Text(
                    text = "WuWa Việt Hóa Mobile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Button(
            onClick = onToggleView,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassSurface,
                contentColor = PrimaryMint
            ),
            border = BorderStroke(1.dp, PrimaryMint.copy(alpha = 0.4f)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = if (isCleanView) Icons.Default.Dashboard else Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isCleanView) "Mở Bảng" else "Toàn Cảnh",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AudioWaveIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(750, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x990E121B))
            .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            modifier = Modifier.height(16.dp)
        ) {
            Box(modifier = Modifier.width(2.5.dp).height(h1.dp).background(PrimaryMint, CircleShape))
            Box(modifier = Modifier.width(2.5.dp).height(h2.dp).background(PrimaryMint, CircleShape))
            Box(modifier = Modifier.width(2.5.dp).height(h3.dp).background(PrimaryGold, CircleShape))
        }
        Text(
            text = "Wuthering Waves · Live Mod Injector",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
        )
    }
}

@Composable
fun CyberParticlesMotion(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "particleY"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val p1Y = (h - ((animY * h) % h))
        val p2Y = (h - (((animY + 0.35f) * h) % h))
        val p3Y = (h - (((animY + 0.7f) * h) % h))

        drawCircle(
            color = Color(0x9900F5D4),
            radius = 3.dp.toPx(),
            center = Offset(w * 0.25f, p1Y)
        )
        drawCircle(
            color = Color(0x99FFC107),
            radius = 2.5.dp.toPx(),
            center = Offset(w * 0.75f, p2Y)
        )
        drawCircle(
            color = Color(0x6600F5D4),
            radius = 2.dp.toPx(),
            center = Offset(w * 0.5f, p3Y)
        )
    }
}

@Composable
fun InstallCtaButton(
    isOperating: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(12.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryMint,
            contentColor = Color(0xFF071018),
            disabledContainerColor = Color(0x3300F5D4),
            disabledContentColor = Color(0x66FFFFFF)
        )
    ) {
        if (isOperating) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF071018),
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ĐANG NẠP BẢN DỊCH...", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        } else {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CÀI ĐẶT / CẬP NHẬT VIỆT HÓA", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun LaunchGameButton(context: Context) {
    Button(
        onClick = {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(GameDetector.activePackage)
                ?: pm.getLaunchIntentForPackage("com.kurogame.wutheringwaves.global")
                ?: pm.getLaunchIntentForPackage("com.kurogame.wutheringwaves.bilibili")
                ?: pm.getLaunchIntentForPackage("com.kurogame.wutheringwaves")
            if (intent != null) {
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x991E2333),
            contentColor = PrimaryMint
        ),
        border = BorderStroke(1.dp, PrimaryMint.copy(alpha = 0.5f))
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("KHỞI ĐỘNG WUTHERING WAVES", fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun ConfigSummaryCard(
    selectedNameStyle: NameStyle,
    selectedFont: FontOption,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33FFFFFF))
            .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(14.dp))
            .clickable { onToggleExpand() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryMint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryMint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text("Cấu hình Việt Hóa:", fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = "${selectedNameStyle.displayName.substringBefore(" (")} · ${selectedFont.displayName.substringBefore(" (")}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (expanded) "Thu gọn" else "Tùy chỉnh", fontSize = 12.sp, color = PrimaryMint, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrimaryMint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun DetailedConfigCard(
    selectedNameStyle: NameStyle,
    onNameStyleChange: (NameStyle) -> Unit,
    selectedFont: FontOption,
    onFontChange: (FontOption) -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x660E121B))
            .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("1. Phong cách tên nhân vật:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
        NameStyle.entries.forEach { style ->
            val isSelected = selectedNameStyle == style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryMint.copy(alpha = 0.12f) else Color.Transparent)
                    .border(
                        BorderStroke(1.dp, if (isSelected) PrimaryMint else Color(0x22FFFFFF)),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enabled) { onNameStyleChange(style) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(style.displayName, fontSize = 12.5.sp, color = if (isSelected) Color.White else TextSecondary)
                RadioButton(
                    selected = isSelected,
                    onClick = { if (enabled) onNameStyleChange(style) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryMint)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33000000))
                .padding(8.dp)
        ) {
            Text(
                text = if (selectedNameStyle == NameStyle.ENGLISH)
                    "Changli: \"Thế giới này tựa như một bàn cờ, mỗi bước đi đều định đoạt tương lai.\""
                else
                    "Trường Ly: \"Thiên địa tựa như bàn cờ, mỗi một nước cờ đều định đoạt tương lai.\"",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFCBD5E1)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("2. Phông chữ tiếng Việt:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGold)
        FontOption.entries.forEach { font ->
            val isSelected = selectedFont == font
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryGold.copy(alpha = 0.12f) else Color.Transparent)
                    .border(
                        BorderStroke(1.dp, if (isSelected) PrimaryGold else Color(0x22FFFFFF)),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enabled) { onFontChange(font) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(font.displayName, fontSize = 12.5.sp, color = if (isSelected) Color.White else TextSecondary)
                RadioButton(
                    selected = isSelected,
                    onClick = { if (enabled) onFontChange(font) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                )
            }
        }
    }
}

@Composable
fun SystemStatusCards(
    isShizukuGranted: Boolean,
    gameStatus: GameStatus,
    onRequestShizuku: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text("Shizuku Binder", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isShizukuGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isShizukuGranted) SuccessGreen else DangerRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isShizukuGranted) "Đã kết nối" else "Chưa cấp quyền",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isShizukuGranted) SuccessGreen else DangerRed
                )
            }
            if (!isShizukuGranted) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onRequestShizuku,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Cấp quyền", fontSize = 10.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text("Thư mục Game", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (gameStatus.isInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (gameStatus.isInstalled) SuccessGreen else DangerRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (gameStatus.isInstalled) "~WuWaMods OK" else "Chưa thấy",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameStatus.isInstalled) SuccessGreen else DangerRed
                )
            }
        }
    }
}

@Composable
fun ConsoleTerminalCard(
    log: String,
    progress: Float,
    isOperating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x9906080D))
            .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SHIZUKU CONSOLE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PrimaryMint)
            Text("Priority _99_P", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
        }

        if (isOperating && progress >= 0f) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PrimaryMint,
                trackColor = Color(0x33FFFFFF)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = log,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 15.sp
        )
    }
}

@Composable
fun LoopingVideoBackground(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return
    val lifecycleOwner = LocalLifecycleOwner.current

    val mediaPlayer = remember {
        MediaPlayer().apply {
            isLooping = true
            setVolume(0f, 0f)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        mediaPlayer.start()
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                var surfaceObj: Surface? = null
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        try {
                            mediaPlayer.reset()
                            surfaceObj?.release()
                            val s = Surface(surface)
                            surfaceObj = s
                            mediaPlayer.setSurface(s)
                            val afd = ctx.resources.openRawResourceFd(R.raw.bg_video)
                            if (afd != null) {
                                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                                mediaPlayer.isLooping = true
                                mediaPlayer.setVolume(0f, 0f)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener { mp ->
                                    adjustVideoAspectRatio(this@apply, mp.videoWidth, mp.videoHeight, width, height)
                                    mp.start()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                        if (mediaPlayer.videoWidth > 0 && mediaPlayer.videoHeight > 0) {
                            adjustVideoAspectRatio(this@apply, mediaPlayer.videoWidth, mediaPlayer.videoHeight, width, height)
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        try {
                            if (mediaPlayer.isPlaying) mediaPlayer.pause()
                            mediaPlayer.setSurface(null)
                            surfaceObj?.release()
                            surfaceObj = null
                        } catch (_: Exception) {}
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
            }
        }
    )
}

private fun adjustVideoAspectRatio(textureView: TextureView, videoWidth: Int, videoHeight: Int, viewWidth: Int, viewHeight: Int) {
    if (videoWidth <= 0 || videoHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return

    val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
    val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
    val matrix = Matrix()

    if (viewRatio < videoRatio) {
        // Màn hình dọc điện thoại: video khớp theo chiều cao (fill height), crop ngang
        val scaleX = (viewHeight * videoRatio) / viewWidth
        val scaleY = 1.0f
        // Nhân vật Chisa ở khoảng vị trí ~46.5% chiều ngang video -> căn chính giữa màn hình
        val characterCenterX = 0.465f
        val dx = (viewWidth / 2f) - (characterCenterX * viewWidth * scaleX)
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(dx, 0f)
    } else {
        // Màn hình ngang hoặc rộng
        val scaleX = 1.0f
        val scaleY = (viewWidth / videoRatio) / viewHeight
        val dy = (viewHeight / 2f) - (viewHeight * scaleY / 2f)
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(0f, dy)
    }

    textureView.setTransform(matrix)
}

@Preview(name = "Trang 1: Liquid Glass Việt Hóa", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    WuWaVietHoaTheme {
        HomeScreen(
            isShizukuGranted = true,
            gameStatus = GameStatus(
                isInstalled = true,
                hasDownloadedData = true,
                basePath = "/storage/emulated/0/Android/data/com.kurogame.wutheringwaves.global/files/UE4Game/Client",
                isPatched = false
            ),
            onRequestShizuku = {},
            onRefreshStatus = {}
        )
    }
}

@Preview(name = "Trang 2: Hướng Dẫn & Thông Báo", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun InfoAndGuideScreenPreview() {
    WuWaVietHoaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_character),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                AppHeaderBar(
                    isShizukuGranted = true,
                    isCleanView = false,
                    onToggleView = {}
                )
                InfoAndGuideScreen()
            }
        }
    }
}
