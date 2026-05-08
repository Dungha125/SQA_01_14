package com.ptit.schedule.controller;

import com.ptit.schedule.dto.ApiResponse;
import com.ptit.schedule.dto.SemesterRequest;
import com.ptit.schedule.dto.SemesterResponse;
import com.ptit.schedule.service.SemesterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Controller test của module Học Kỳ.
 */
@ExtendWith(MockitoExtension.class)
public class SemesterControllerGroupTest {

    @Mock private SemesterService semesterService;
    @InjectMocks private SemesterController semesterController;

    @Test
    void testGetAllSemesters_Success() {
        // HK-44: Mục đích: Lấy danh sách Semester thành công.
        SemesterResponse r = new SemesterResponse(); r.setSemesterName("Kỳ 1");
        when(semesterService.getAllSemesters()).thenReturn(Collections.singletonList(r));
        ResponseEntity<ApiResponse<List<SemesterResponse>>> response = semesterController.getAllSemesters();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Kỳ 1", response.getBody().getData().get(0).getSemesterName());
    }

    @Test
    void testGetAllSemesters_Exception() {
        // HK-45: Mục đích: Bắt Exception 500 khi GetAll ném lỗi.
        when(semesterService.getAllSemesters()).thenThrow(new RuntimeException("DB Error"));
        ResponseEntity<ApiResponse<List<SemesterResponse>>> response = semesterController.getAllSemesters();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetSemesterById_Success() {
        // HK-46: Mục đích: Tìm Semester theo ID 200 OK.
        SemesterResponse r = new SemesterResponse(); r.setId(1L);
        when(semesterService.getSemesterById(1L)).thenReturn(r);
        ResponseEntity<ApiResponse<SemesterResponse>> response = semesterController.getSemesterById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetSemesterById_NotFound() {
        // HK-47: Mục đích: Tìm theo ID nhưng quăng RuntimeException, trả 404.
        when(semesterService.getSemesterById(99L)).thenThrow(new RuntimeException("Not found"));
        ResponseEntity<ApiResponse<SemesterResponse>> response = semesterController.getSemesterById(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetSemesterByName_Success() {
        // HK-48: Mục đích: Tìm bằng tên thành công.
        when(semesterService.getSemesterByName("Kỳ Hè")).thenReturn(new SemesterResponse());
        ResponseEntity<ApiResponse<SemesterResponse>> response = semesterController.getSemesterByName("Kỳ Hè");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetSemesterByName_Error() {
        // HK-49: Mục đích: Lỗi RuntimeException khi trùng tên hoặc không thấy.
        when(semesterService.getSemesterByName("Loi")).thenThrow(new RuntimeException("Loi"));
        assertEquals(HttpStatus.NOT_FOUND, semesterController.getSemesterByName("Loi").getStatusCode());
    }

    @Test
    void testGetActiveSemester_Success() {
        // HK-50: Mục đích: Lấy Default/Active Semester.
        when(semesterService.getActiveSemester()).thenReturn(new SemesterResponse());
        assertEquals(HttpStatus.OK, semesterController.getActiveSemester().getStatusCode());
    }

    @Test
    void testGetAllSemesterNames_Success() {
        // HK-51: Mục đích: Lấy mảng String Names.
        when(semesterService.getAllSemesterNames()).thenReturn(Arrays.asList("A", "B"));
        ResponseEntity<ApiResponse<List<String>>> response = semesterController.getAllSemesterNames();
        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    void testCreateSemester_Success() {
        // HK-52: Mục đích: Post tạo mới.
        when(semesterService.createSemester(any())).thenReturn(new SemesterResponse());
        assertEquals(HttpStatus.CREATED, semesterController.createSemester(new SemesterRequest()).getStatusCode());
    }

    @Test
    void testDeleteSemester_Success() {
        // HK-53: Mục đích: Xóa theo ID.
        doNothing().when(semesterService).deleteSemester(1L);
        ResponseEntity<ApiResponse<Void>> response = semesterController.deleteSemester(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
