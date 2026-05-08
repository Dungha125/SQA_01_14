package com.ptit.schedule.service;

import com.ptit.schedule.dto.FacultyRequest;
import com.ptit.schedule.dto.FacultyResponse;

import java.util.List;

public interface FacultyService {
    
    /**
     * Lấy tất cả faculties
     */
    List<FacultyResponse> getAllFaculties();
    
    /**
     * Lấy faculty theo ID
     */
    FacultyResponse getFacultyById(String id);

    /**
     * Tạo faculty mới
     */
    FacultyResponse createFaculty(FacultyRequest request);
    
    /**
     * Cập nhật faculty
     */
    FacultyResponse updateFaculty(String id, FacultyRequest request);
    
    /**
     * Xóa faculty
     */
    void deleteFaculty(String id);
}
