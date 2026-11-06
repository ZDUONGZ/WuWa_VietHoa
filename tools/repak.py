#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
repak / repack CLI tool for Unreal Engine .pak files (WuWa compatible)
Supports unpacking, packing, listing, and inspecting .pak archives.
"""

import sys
import os
import argparse
from pathlib import Path

# Force UTF-8 encoding for standard I/O
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass

try:
    from pyuepak.pak import PakFile
    from pyuepak.version import PakVersion
except ImportError:
    print("[ERROR] pyuepak is not installed. Run: pip install pyuepak")
    sys.exit(1)


def cmd_unpack(args):
    pak_path = Path(args.input)
    if not pak_path.exists():
        print(f"[ERROR] File không tồn tại: {pak_path}")
        return 1

    out_dir = Path(args.output) if args.output else pak_path.with_suffix("")
    out_dir.mkdir(parents=True, exist_ok=True)

    print(f"📦 Đang giải nén: {pak_path.name} -> {out_dir}")
    try:
        pak = PakFile()
        pak.read(str(pak_path))
        files = pak.list_files()

        count = 0
        for rel_path in files:
            data = pak.read_file(rel_path)
            target = out_dir / rel_path
            target.parent.mkdir(parents=True, exist_ok=True)
            with open(target, "wb") as f:
                f.write(data)
            count += 1

        print(f"✅ Đã giải nén thành công {count} files vào '{out_dir}'")
        return 0
    except Exception as e:
        print(f"❌ Lỗi giải nén: {e}")
        return 1


def cmd_pack(args):
    in_dir = Path(args.input)
    if not in_dir.is_dir():
        print(f"[ERROR] Thư mục đầu vào không tồn tại: {in_dir}")
        return 1

    out_pak = Path(args.output) if args.output else in_dir.with_suffix(".pak")
    out_pak.parent.mkdir(parents=True, exist_ok=True)

    ver_str = (args.version or "V11").upper()
    if not ver_str.startswith("V") and ver_str.isdigit():
        ver_str = f"V{ver_str}"

    try:
        ver = PakVersion[ver_str]
    except KeyError:
        print(f"[WARN] Phiên bản {ver_str} không hợp lệ, mặc định dùng V11")
        ver = PakVersion.V11

    mount = args.mount_point or "../../../"

    print(f"📦 Đang đóng gói: {in_dir} -> {out_pak.name} (Version: {ver.name}, Mount: {mount})")
    try:
        pak = PakFile()
        pak.set_version(ver)
        pak.set_mount_point(mount)

        files_to_pack = [p for p in in_dir.rglob("*") if p.is_file()]
        for fpath in files_to_pack:
            rel = fpath.relative_to(in_dir).as_posix()
            with open(fpath, "rb") as f:
                pak.add_file(rel, f.read())

        pak.write(str(out_pak))
        print(f"✅ Đã đóng gói thành công {len(files_to_pack)} files vào '{out_pak}'")
        return 0
    except Exception as e:
        print(f"❌ Lỗi đóng gói: {e}")
        return 1


def cmd_list(args):
    pak_path = Path(args.input)
    if not pak_path.exists():
        print(f"[ERROR] File không tồn tại: {pak_path}")
        return 1

    try:
        pak = PakFile()
        pak.read(str(pak_path))
        files = pak.list_files()
        for f in files:
            print(f)
        print(f"\nTổng cộng: {len(files)} files")
        return 0
    except Exception as e:
        print(f"❌ Lỗi đọc danh sách: {e}")
        return 1


def cmd_info(args):
    pak_path = Path(args.input)
    if not pak_path.exists():
        print(f"[ERROR] File không tồn tại: {pak_path}")
        return 1

    try:
        pak = PakFile()
        pak.read(str(pak_path))
        files = pak.list_files()
        size_mb = pak_path.stat().st_size / (1024 * 1024)

        print(f"--- THÔNG TIN PAK: {pak_path.name} ---")
        print(f"Dung lượng: {size_mb:.2f} MB ({pak_path.stat().st_size:,} bytes)")
        print(f"Pak Version: {pak.version.name if hasattr(pak, 'version') and pak.version else 'N/A'}")
        print(f"Mount Point: {pak.mount_point if hasattr(pak, 'mount_point') else 'N/A'}")
        print(f"Số lượng file: {len(files)}")
        return 0
    except Exception as e:
        print(f"❌ Lỗi đọc thông tin: {e}")
        return 1


def main():
    parser = argparse.ArgumentParser(
        prog="repak",
        description="Unreal Engine .pak tool (trumank / pyuepak compatible) cho Wuthering Waves"
    )
    subparsers = parser.add_subparsers(dest="command", help="Lệnh thực thi")

    # unpack
    p_unpack = subparsers.add_parser("unpack", help="Giải nén file .pak")
    p_unpack.add_argument("input", help="Đường dẫn file .pak cần giải nén")
    p_unpack.add_argument("-o", "--output", help="Thư mục xuất kết quả (mặc định trùng tên file)")

    # pack
    p_pack = subparsers.add_parser("pack", help="Đóng gói thư mục thành file .pak")
    p_pack.add_argument("input", help="Thư mục chứa files cần đóng gói")
    p_pack.add_argument("output", nargs="?", help="Đường dẫn file .pak đầu ra (tùy chọn)")
    p_pack.add_argument("-o", "--out", dest="out_alt", help="Đường dẫn file .pak đầu ra (cú pháp -o)")
    p_pack.add_argument("-v", "--version", default="V11", help="Phiên bản Unreal PAK (mặc định: V11)")
    p_pack.add_argument("-m", "--mount-point", default="../../../", help="Mount point (mặc định: ../../../)")

    # list
    p_list = subparsers.add_parser("list", help="Liệt kê danh sách file trong .pak")
    p_list.add_argument("input", help="Đường dẫn file .pak")

    # info
    p_info = subparsers.add_parser("info", help="Xem thông tin file .pak")
    p_info.add_argument("input", help="Đường dẫn file .pak")

    args = parser.parse_args()

    if not args.command:
        parser.print_help()
        return 0

    if args.command == "unpack":
        return cmd_unpack(args)
    elif args.command == "pack":
        if not args.output and hasattr(args, "out_alt") and args.out_alt:
            args.output = args.out_alt
        return cmd_pack(args)
    elif args.command == "list":
        return cmd_list(args)
    elif args.command == "info":
        return cmd_info(args)


if __name__ == "__main__":
    sys.exit(main() or 0)
