# WuWa Việt Hóa Mobile (Android)

Ứng dụng mã nguồn mở hỗ trợ cài đặt và quản lý bản dịch Tiếng Việt cho **Wuthering Waves (Android)** sử dụng **Shizuku** (không cần Root).

## ✨ Tính năng chính

- **Không cần Root**: Hoạt động mượt mà trên Android 10+ (API 29–35) thông qua Shizuku Binder.
- **Không ghi đè file gốc**: Áp dụng cơ chế nạp Mod của Unreal Engine (`~WuWaMods`) với độ ưu tiên `_99_P` và `_100_P`.
- **Tùy chọn phong cách**:
  - Tên quốc tế (Jinhsi, Changli...)
  - Tên Hán Việt (Kim Tịch, Trường Ly...)
- **Tùy chọn phông chữ**: Hỗ trợ phông chữ Lexend tiếng Việt và Segoe UI.
- **Hoàn tác 1-Click**: Xóa mod sạch sẽ để game quay về dữ liệu tiếng Anh gốc khi cập nhật.

---

## 🛠️ Cấu trúc dự án

```
d:\Dự án Việt Hóa WuWa/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Khai báo quyền & ShizukuProvider
│   │   └── java/com/wuwa/viethoa/
│   │       ├── MainActivity.kt          # Vòng đời kết nối Shizuku & UI State
│   │       ├── shizuku/
│   │       │   └── ShizukuManager.kt    # Quản lý quyền & thực thi shell Shizuku
│   │       ├── installer/
│   │       │   ├── GameDetector.kt      # Nhận diện đường dẫn & trạng thái game
│   │       │   └── PatchInstaller.kt    # Nạp/gỡ file .pak vào ~WuWaMods
│   │       └── ui/
│   │           ├── HomeScreen.kt        # Giao diện Jetpack Compose Material 3
│   │           └── theme/               # Dark mode theme & bảng màu
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📱 Cách sử dụng trên điện thoại

1. Mở app **Shizuku** trên điện thoại và khởi chạy (qua Ghép nối Wifi / USB Debugging).
2. Mở app **WuWa Việt Hóa**.
3. Bấm **Cấp quyền** cho Shizuku.
4. Chọn phong cách tên nhân vật và phông chữ mong muốn.
5. Bấm **Cài Đặt / Cập Nhật Việt Hóa**.
6. Vào game Wuthering Waves -> **Cài đặt** -> **Ngôn ngữ** -> Chọn **English**.
