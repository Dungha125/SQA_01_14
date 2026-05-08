package com.ptit.schedule.entity;

public enum RoomType {
    CLC("Lớp chất lượng cao"),
    ENGLISH_CLASS("Phòng học tiếng Anh"),
    GENERAL("Phòng học thông thường"),
    KHOA_2024("Phòng dành cho khóa 2024"),
    NGOC_TRUC("Tòa nhà Ngọc Trúc");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
