package com.ptit.schedule.service;

import com.ptit.schedule.dto.ScheduleEntry;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScheduleExcelReaderService {
    
    /**
     * Đọc file Excel thời khóa biểu và trích xuất dữ liệu
     * @param file file Excel
     * @return danh sách ScheduleEntry
     */
    List<ScheduleEntry> readScheduleFromExcel(MultipartFile file);
    
    /**
     * Validate file Excel có đúng định dạng thời khóa biểu không
     * @param file file Excel
     * @return true nếu hợp lệ
     */
    boolean validateScheduleExcelFormat(MultipartFile file);
}