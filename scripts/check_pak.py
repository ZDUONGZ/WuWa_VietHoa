import sys
import os
import struct

def check_wuwa_pak(pak_path):
    if not os.path.exists(pak_path):
        print(f"[ERROR] Không tìm thấy file: {pak_path}")
        return False

    size = os.path.getsize(pak_path)
    size_mb = size / (1024 * 1024)
    print(f"--- KIỂM TRA FILE PAK: {os.path.basename(pak_path)} ---")
    print(f"Dung lượng: {size:,} bytes ({size_mb:.2f} MB)")

    with open(pak_path, "rb") as f:
        # 1. Kiểm tra Footer 221 bytes của Unreal Engine V12
        if size < 221:
            print("[FAIL] File quá nhỏ, không phải PAK hợp lệ.")
            return False

        f.seek(size - 221)
        footer = f.read(221)

        magic = b"\xE1\x12\x6F\x5A"
        idx = footer.rfind(magic)
        if idx == -1:
            print("[FAIL] Không tìm thấy Magic Number 0x5A6F12E1 của Unreal PAK.")
            return False

        version = struct.unpack_from("<I", footer, idx + 4)[0]
        index_offset = struct.unpack_from("<Q", footer, idx + 8)[0]
        index_size = struct.unpack_from("<Q", footer, idx + 16)[0]

        print(f"Unreal Pak Magic: HỢP LỆ (0x5A6F12E1)")
        print(f"Pak Version: {version}")
        print(f"Index Offset: {index_offset}, Index Size: {index_size}")

        # 2. Đọc Index để kiểm tra danh sách file
        f.seek(index_offset)
        index_data = f.read(min(index_size, 1024 * 1024)) # Đọc phần đầu index

        # Tìm chuỗi mount point và đếm file
        f.seek(0)
        # Quét nhanh nội dung dạng chuỗi DB
        f.seek(0)
        data_preview = f.read(min(size, 50 * 1024 * 1024))
        
        has_total_topup = b"db_TotalTopUp.db" in data_preview or b"db_TotalTopUp.db" in index_data
        has_lang_multi = b"lang_multi_text.db" in data_preview or b"lang_multi_text.db" in index_data
        has_zh_hans = b"zh-Hans/lang_multi_text.db" in data_preview or b"zh-Hans/lang_multi_text.db" in index_data

    print("\n--- KẾT QUẢ ĐỐI SOÁT TIÊU CHUẨN ANDROID ---")
    print(f"1. Có cơ sở dữ liệu ngôn ngữ chính (lang_multi_text.db): {'CÓ' if has_lang_multi else 'KHÔNG'}")
    print(f"2. Có DB nạp tiền riêng của Android (db_TotalTopUp.db): {'CÓ (Đạt)' if has_total_topup else 'THIẾU (Dấu hiệu bản PC)'}")
    print(f"3. Có DB zh-Hans phụ trợ của Android: {'CÓ (Đạt)' if has_zh_hans else 'THIẾU'}")
    
    if size_mb >= 90:
        print(f"4. Dung lượng payload: {size_mb:.2f} MB -> ĐẠT TIÊU CHUẨN ANDROID FULL-BASE (>= 90 MB)")
    elif 60 <= size_mb <= 75:
        print(f"4. Dung lượng payload: {size_mb:.2f} MB -> CẢNH BÁO: Đây là kích thước payload của bản PC (~67 MB), dễ lỗi text trống trên Android!")
    else:
        print(f"4. Dung lượng payload: {size_mb:.2f} MB")

    is_android_standard = has_total_topup and has_lang_multi and size_mb >= 85
    if is_android_standard:
        print("\n=> KẾT LUẬN: ĐẠT CHUẨN ANDROID (Không lo lỗi mất chữ / text trống)")
    else:
        print("\n=> KẾT LUẬN: CHƯA ĐẠT CHUẨN ANDROID HOÀN CHỈNH (Cần kiểm tra kỹ các key mobile)")
    return is_android_standard

if __name__ == "__main__":
    if len(sys.argv) > 1:
        check_wuwa_pak(sys.argv[1])
    else:
        print("Sử dụng: python check_pak.py <duong_dan_file.pak>")
