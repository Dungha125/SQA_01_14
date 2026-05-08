package com.ptit.schedule.service;

import com.ptit.schedule.dto.ConflictResult;
import com.ptit.schedule.dto.ScheduleEntry;

import java.util.List;

public interface ScheduleConflictDetectionService {
    
    /**
     * Phát hiện xung đột trong danh sách thời khóa biểu
     * @param scheduleEntries danh sách thời khóa biểu
     * @return kết quả phát hiện xung đột
     */
    ConflictResult detectConflicts(List<ScheduleEntry> scheduleEntries);
    
    /**
     * Phát hiện xung đột phòng học
     * @param scheduleEntries danh sách thời khóa biểu
     * @return danh sách xung đột phòng
     */
    List<ConflictResult.RoomConflict> detectRoomConflicts(List<ScheduleEntry> scheduleEntries);
    
    /**
     * Phát hiện xung đột giảng viên
     * @param scheduleEntries danh sách thời khóa biểu  
     * @return danh sách xung đột giảng viên
     */
    List<ConflictResult.TeacherConflict> detectTeacherConflicts(List<ScheduleEntry> scheduleEntries);
}