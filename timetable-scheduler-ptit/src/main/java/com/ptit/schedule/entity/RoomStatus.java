package com.ptit.schedule.entity;

public enum RoomStatus {
    AVAILABLE("Trống"),
    OCCUPIED("Đang sử dụng"),
    UNAVAILABLE("Không sử dụng được");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
