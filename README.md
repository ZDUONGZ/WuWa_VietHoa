# WuWa Việt Hóa Mobile (Android)

<p align="center">
  <b>Ứng dụng cài đặt & quản lý bản dịch Tiếng Việt cho Wuthering Waves trên Android (Không cần Root)</b>
  <br />
  <i>Sử dụng công nghệ Shizuku Binder và cơ chế nạp Mod tự nhiên của Unreal Engine 4</i>
</p>

---

## 📖 Giới thiệu

**WuWa Việt Hóa Mobile** là ứng dụng Android mã nguồn mở được phát triển nhằm tự động hóa quy trình cài đặt, cập nhật và quản lý các bản dịch Tiếng Việt cho tựa game **Wuthering Waves** (Kuro Games).

Trước đây, việc cài đặt bản dịch trên Android 11+ gặp nhiều rào cản do hệ điều hành hạn chế truy cập thư mục `/Android/data`. Ứng dụng này giải quyết triệt để vấn đề bằng cách tích hợp **Shizuku API**, cho phép thao tác tệp tin hệ thống an toàn ở cấp người dùng mà **không cần mở khóa Bootloader hay Root thiết bị**.

---

## ✨ Tính năng nổi bật

### 🛡️ An toàn & Độc lập

- **Không cần Root**: Hoạt động mượt mà trên Android 10 đến Android 14+ (API 29–34+) thông qua Binder IPC của Shizuku.
- **Không can thiệp file game gốc**: Tận dụng cơ chế nạp Mod theo thứ tự ưu tiên của Unreal Engine 4 (`~WuWaMods`). Gói dịch được nạp qua các file `WuWaVH_99_P.pak` và `WahuFont_100_P.pak`, tuyệt đối không ghi đè bất kỳ file gốc nào.
- **Hỗ trợ đa phiên bản game**: Tự động nhận diện bản Global (`com.kurogame.wutheringwaves.global`), Bilibili (`com.kurogame.wutheringwaves.bilibili`) và bản nội địa Trung Quốc.
- **Gỡ bỏ 1-Click (Uninstall)**: Làm sạch thư mục mod chỉ với một thao tác, đưa game trở về nguyên bản ngay lập tức khi cần cập nhật bản vá từ Google Play Store.

### 🎨 Tùy biến trải nghiệm

- **Tùy chọn phong cách dịch thuật**:
  - **Tên quốc tế (EN)**: Giữ nguyên tên nhân vật và địa danh tiếng Anh chuẩn (Jinhsi, Changli, Rover, Huanglong...).
  - **Tên Hán Việt**: Chuyển đổi tên nhân vật và địa danh quen thuộc (Kim Tịch, Trường Ly, Phiêu Bạc Giả...).
- **Tùy chọn phông chữ tiếng Việt**:
  - **Lexend**: Tối ưu hiển thị dấu tiếng Việt sắc nét, mượt mà trên màn hình tần số quét cao (120Hz).
  - **Segoe UI**: Phong cách thanh lịch, hiện đại.
  - **Phông mặc định**: Giữ nguyên phông gốc của game.

### ⚡ Giao diện hiện đại & Trải nghiệm tiện lợi (Jetpack Compose)

- **Giao diện Liquid Glass (Kính mờ)**: Phản chiếu neon cyber cùng hình nền video loop động mượt mà.
- **Chế độ Clean View**: Rút gọn các bảng điều khiển chỉ với một chạm để ngắm hình nền nhân vật.
- **Console Terminal thời gian thực**: Theo dõi chi tiết từng bước tải dữ liệu và nạp file vào hệ thống.
- **Shortcut khởi chạy game trực tiếp**: Mở ngay Wuthering Waves từ ứng dụng sau khi cài đặt hoàn tất.
- **Bảng Thông Báo & Hướng Dẫn tích hợp**: Cập nhật tin tức phiên bản mới nhất từ đội ngũ phát triển và liên kết cộng đồng.

---

## 🛠️ Cấu trúc dự án

Dự án được tổ chức khoa học theo mô hình module hóa hiện đại:

```
d:\Dự án Việt Hóa WuWa/
├── app/                                       # Module mã nguồn Android chính
│   ├── src/main/
│   │   ├── AndroidManifest.xml                # Khai báo cấu hình, quyền & ShizukuProvider
│   │   ├── java/com/wuwa/viethoa/
│   │   │   ├── MainActivity.kt                # Vòng đời app, kết nối Shizuku Binder & Compose root
│   │   │   ├── shizuku/
│   │   │   │   └── ShizukuManager.kt          # Quản lý quyền Binder & thực thi lệnh Shell hệ thống
│   │   │   ├── installer/
│   │   │   │   ├── GameDetector.kt            # Nhận diện package, trạng thái cài đặt & mod
│   │   │   │   └── PatchInstaller.kt          # Xử lý tải GitHub Releases, nạp/gỡ .pak vào ~WuWaMods
│   │   │   └── ui/
│   │   │       ├── HomeScreen.kt              # Màn hình chính (Tab Việt Hóa, Tab Hướng Dẫn, Video/Particle Bg)
│   │   │       └── theme/                     # Dark theme, Liquid Glass palette & Typography
│   │   └── res/
│   │       ├── raw/bg_video.mp4               # Video background chuyển động lặp vô tận
│   │       ├── drawable/                      # Vector icons, app icon & hình nền tĩnh
│   │       └── values/                        # Strings, colors, XML themes
│   ├── src/test/java/com/wuwa/viethoa/installer/
│   │   └── InstallerLogicTest.kt              # Unit test kiểm thử parser và logic chọn gói mod
│   └── build.gradle.kts                       # Cấu hình build module app (Compose, Shizuku, Coroutines)
├── scripts/
│   ├── test.ps1                               # Script chạy Unit test an toàn trên Windows
│   └── check_pak.py                           # Script kiểm tra cấu trúc header của file PAK Unreal Engine
├── tools/
│   ├── repak.py / repak.cmd                   # Công cụ đóng gói / giải nén tệp PAK Unreal Engine 4
│   └── wahu/                                  # Cơ sở dữ liệu và công cụ dịch thuật WAHU Community
├── designs/                                   # Bản vẽ giao diện & tài liệu thiết kế canvas
├── build.gradle.kts                           # Cấu hình Gradle cấp root
└── settings.gradle.kts                        # Khai báo cấu hình repository & plugin
```

---

## ⚙️ Cơ chế hoạt động kỹ thuật

1. **Ủy quyền Shizuku Binder**:
   Android 11+ chặn quyền truy cập vào đường dẫn `/storage/emulated/0/Android/data/`. [`ShizukuManager.kt`](file:///d:/D%E1%BB%B1%20%C3%A1n%20Vi%E1%BB%87t%20H%C3%B3a%20WuWa/app/src/main/java/com/wuwa/viethoa/shizuku/ShizukuManager.kt) giao tiếp trực tiếp với tiến trình dịch vụ Shizuku qua Android Binder IPC để thực thi các lệnh hệ thống cấp cao (`mkdir`, `cp`, `rm`, `chmod`) với quyền ADB/User-0.
2. **Mounting Order của Unreal Engine 4**:
   Unreal Engine đọc các file `.pak` trong thư mục `Content/Paks` theo thứ tự chữ cái và hậu tố nạp. Bằng cách đặt file vào thư mục con `~WuWaMods/` với tên `WuWaVH_99_P.pak` và `WahuFont_100_P.pak`, game sẽ ưu tiên tải đè tài nguyên dịch và font chữ tiếng Việt mà không cần thay đổi bất kỳ file nguyên bản nào.
3. **Phân phối bản dịch tự động**:
   [`PatchInstaller.kt`](file:///d:/D%E1%BB%B1%20%C3%A1n%20Vi%E1%BB%87t%20H%C3%B3a%20WuWa/app/src/main/java/com/wuwa/viethoa/installer/PatchInstaller.kt) truy vấn GitHub Releases API (`ZDUONGZ/WuWa_VietHoa`) để lấy URL tải trực tiếp tệp `.pak` mới nhất, tự động xử lý chuyển hướng liên kết HTTP (301/302 Redirect) và có cơ chế Fallback CDN ổn định.

---

## 📱 Hướng dẫn sử dụng cho người chơi

### Điều kiện tiên quyết:

- Điện thoại chạy **Android 10** trở lên.
- Đã cài đặt và kích hoạt ứng dụng **Shizuku** (khuyến nghị kích hoạt qua _Ghép nối Wifi / Wireless Debugging_, không cần máy tính sau lần đầu).
- Đã tải đầy đủ tài nguyên trong game Wuthering Waves.

### Các bước thực hiện:

1. **Khởi động Shizuku**: Mở ứng dụng Shizuku trên điện thoại và xác nhận trạng thái **"Shizuku đang chạy"**.
2. **Cấp quyền**: Mở app **WuWa Việt Hóa**, bấm nút **"Cấp Quyền"** Shizuku và chọn _Cho phép luôn luôn_.
3. **Tùy chỉnh cấu hình**: Chọn phong cách tên nhân vật (Quốc tế hoặc Hán Việt) và phông chữ mong muốn.
4. **Cài đặt**: Bấm **"CÀI ĐẶT / CẬP NHẬT VIỆT HÓA"** và chờ thanh tiến trình chạy hoàn tất 100%.
5. **Kích hoạt trong Game**:
   - Mở game Wuthering Waves (có thể bấm nút mở game trực tiếp từ app).
   - Vào mục **Cài đặt (Settings)** -> **Ngôn ngữ (Language)** -> Chọn ngôn ngữ hiển thị là **English**.
   - Thưởng thức cốt truyện và giao diện hoàn toàn bằng Tiếng Việt!

> ⚠️ **Lưu ý quan trọng**: Khi game có bản cập nhật mới trên Google Play Store / TapTap, hãy vào app WuWa Việt Hóa và bấm **"Gỡ Bỏ Việt Hóa (Về Bản Gốc)"** trước khi cập nhật game để tránh xung đột dữ liệu.

---

## 💻 Hướng dẫn dành cho lập trình viên

### 1. Môi trường phát triển

- **Android Studio**: Iguana (2023.2.1) trở lên (khuyên dùng Jellyfish hoặc Ladybug).
- **JDK**: Java 17.
- **Android SDK**: Compile SDK 34, Min SDK 29.

### 2. Biên dịch và cài đặt

```bash
# Clone dự án
git clone https://github.com/ZDUONGZ/WuWa_VietHoa_Mobile.git

# Biên dịch bản Debug APK
./gradlew assembleDebug

# Cài đặt trực tiếp lên thiết bị đã kết nối ADB
./gradlew installDebug
```

### 3. Kiểm thử (Unit Testing)

Dự án bao gồm bộ kiểm thử unit test cho logic nhận diện game và phân giải gói mod tại [`InstallerLogicTest.kt`](file:///d:/D%E1%BB%B1%20%C3%A1n%20Vi%E1%BB%87t%20H%C3%B3a%20WuWa/app/src/test/java/com/wuwa/viethoa/installer/InstallerLogicTest.kt).

Trên môi trường Windows, chạy kiểm thử bằng script tiện ích:

```powershell
.\scripts\test.ps1
```

---

## 👥 Đóng góp & Bản quyền

- **Trưởng dự án & Phát triển Mobile App**: [ZDUONGZ](https://github.com/ZDUONGZ)
- **Công cụ dịch thuật & Thuật ngữ**: WAHU Community
- **Cộng đồng hỗ trợ**:
  - [Facebook Cộng đồng](https://www.facebook.com/nguyen.ai.duon/)
  - [Kênh TikTok Hướng dẫn](https://www.tiktok.com/@ngduong.137)

_Dự án phát triển phi lợi nhuận phục vụ cộng đồng người chơi Wuthering Waves tại Việt Nam._
