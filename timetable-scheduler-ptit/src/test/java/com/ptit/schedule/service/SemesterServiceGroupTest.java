package com.ptit.schedule.service;

import com.ptit.schedule.repository.RoomOccupancyRepository;
import com.ptit.schedule.repository.ScheduleRepository;
import com.ptit.schedule.repository.SemesterRepository;
import com.ptit.schedule.repository.TKBTemplateRepository;
import com.ptit.schedule.service.impl.SemesterServiceImpl;
import com.ptit.schedule.entity.Semester;
import com.ptit.schedule.dto.SemesterResponse;
import com.ptit.schedule.dto.SemesterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Gộp các test Service thuộc module Học Kỳ (SemesterService).
 */
@ExtendWith(MockitoExtension.class)
public class SemesterServiceGroupTest {

    @Mock private SemesterRepository semesterRepository;
    @Mock private RoomOccupancyRepository roomOccupancyRepository;
    @Mock private TKBTemplateRepository tkbTemplateRepository;
    @Mock private ScheduleRepository scheduleRepository;

    @InjectMocks private SemesterServiceImpl semesterService;

    @Test
    void testSemesterService_Init() {
        // HK-31: Mục đích: Mock tiêm Service Implementation.
        assertNotNull(semesterService);
    }

    @Test
    void testSemesterService_GetAll() {
        // HK-32: Mục đích: Gọi phương thức GetAll trong service và mock Repository đúng method.
        Semester sem = new Semester(); sem.setId(1L); sem.setSemesterName("Test");
        lenient().when(semesterRepository.findAllOrderByYearAndSemesterDesc()).thenReturn(Collections.singletonList(sem));
        List<SemesterResponse> res = semesterService.getAllSemesters();
        assertEquals(1, res.size());
        assertEquals("Test", res.get(0).getSemesterName());
    }

    @Test
    void testSemesterService_GetById_Found() {
        // HK-33: Mục đích: Tìm kiếm object.
        Semester sem = new Semester(); sem.setId(11L);
        lenient().when(semesterRepository.findById(11L)).thenReturn(Optional.of(sem));
        SemesterResponse res = semesterService.getSemesterById(11L);
        assertEquals(11L, res.getId());
    }

    @Test
    void testSemesterService_GetById_NotFound() {
        // HK-34: Mục đích: NotFound throw RuntimeException.
        lenient().when(semesterRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> semesterService.getSemesterById(99L));
    }

    @Test
    void testSemesterService_GetByName_Found() {
        // HK-35: Mục đích: Tìm Name trả về đối tượng hợp lệ.
        Semester sem = new Semester(); sem.setSemesterName("Học kỳ phụ");
        lenient().when(semesterRepository.findBySemesterName("Học kỳ phụ")).thenReturn(Optional.of(sem));
        SemesterResponse resp = semesterService.getSemesterByName("Học kỳ phụ");
        assertNotNull(resp);
        assertEquals("Học kỳ phụ", resp.getSemesterName());
    }

    @Test
    void testSemesterService_GetByName_NotFound() {
        // HK-36: Mục đích: Tìm Name throw exception nếu Name = Trống.
        lenient().when(semesterRepository.findBySemesterName(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> semesterService.getSemesterByName("Khong co"));
    }

    @Test
    void testSemesterService_GetActive() {
        // HK-37: Mục đích: getActive() cho phép call repository (isActive=true).
        Semester sem = new Semester(); sem.setIsActive(true);
        lenient().when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(sem));
        SemesterResponse res = semesterService.getActiveSemester();
        assertTrue(res.getIsActive());
    }

    @Test
    void testSemesterService_Delete_Fail() {
        // HK-38: Mục đích: Delete if ko tồn tại.
        lenient().when(semesterRepository.findById(50L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> semesterService.deleteSemester(50L));
    }

    @Test
    void testSemesterService_Delete_Success() {
        // HK-39: Mục đích: Delete verify successful execution and dependencies.
        Semester sem = new Semester(); sem.setId(10L); sem.setSemesterName("K1"); sem.setAcademicYear("20-21");
        lenient().when(semesterRepository.findById(10L)).thenReturn(Optional.of(sem));
        lenient().when(scheduleRepository.findBySemesterNameAndAcademicYear("K1", "20-21")).thenReturn(Collections.emptyList());
        
        // Mock void methods
        doNothing().when(tkbTemplateRepository).deleteBySemester(sem);
        doNothing().when(roomOccupancyRepository).deleteBySemesterId(10L);
        doNothing().when(semesterRepository).deleteById(10L);
        
        semesterService.deleteSemester(10L);
        verify(semesterRepository, times(1)).deleteById(10L);
    }
    
    @Test
    void testSemesterService_Activate() {
        // HK-40: Mục đích: Cập nhật active Semester.
        Semester target = new Semester(); target.setId(5L); target.setIsActive(false);
        lenient().when(semesterRepository.findById(5L)).thenReturn(Optional.of(target));
        
        // Mock deactivateAllSemesters
        lenient().when(semesterRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(semesterRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        
        // Mock save for activated semester
        lenient().when(semesterRepository.save(any())).thenReturn(target); 
        
        SemesterResponse resp = semesterService.activateSemester(5L);
        assertNotNull(resp);
        verify(semesterRepository, times(1)).save(target);
    }
    
    @Test
    void testSemesterService_Create_Success() {
        // HK-41: Mục đích: Tạo mới Học Kỳ thành công.
        SemesterRequest request = new SemesterRequest();
        request.setSemesterName("Kỳ 1");
        request.setAcademicYear("2024 - 2025");
        request.setIsActive(true);
        
        lenient().when(semesterRepository.existsBySemesterNameAndAcademicYear("Kỳ 1", "2024-2025")).thenReturn(false);
        lenient().when(semesterRepository.findAll()).thenReturn(Collections.emptyList());
        Semester saved = new Semester(); saved.setId(10L);
        lenient().when(semesterRepository.save(any())).thenReturn(saved);
        
        SemesterResponse response = semesterService.createSemester(request);
        assertNotNull(response);
    }

    // ==========================================
    // NHÓM TEST CASE PHÁT HIỆN LỖI BLACK-BOX TỪ USER
    // ==========================================

    @Test
    void testSemesterService_CreateSemester_InvalidYearFormat() {
        // HK-42: Mục đích: Test định dạng năm học là chữ (vd: abcd-xyz).
        // EXPECTED: Tuỳ theo mong muốn chuẩn, hàm này phải bắt lỗi định dạng thay vì cho qua.
        // Thực tế: Hiện tại hệ thống sẽ cho qua vì String.split("-") vẫn nhận abcd và xyz -> Lỗi Logic.
        SemesterRequest request = new SemesterRequest();
        request.setSemesterName("Kỳ 1");
        request.setAcademicYear("abcd-xyz");
        request.setIsActive(true);

        lenient().when(semesterRepository.existsBySemesterNameAndAcademicYear(anyString(), anyString())).thenReturn(false);
        Semester saved = new Semester(); saved.setId(99L);
        lenient().when(semesterRepository.save(any())).thenReturn(saved);

        // Kịch bản TDD: Chúng ta kỳ vọng nó NÊN ném ra IllegalArgumentException, nhưng hiện tại chạy sẽ bị FAIL (Fail to Pass)
        // do source code chưa được sửa. Bỏ comment dòng dưới để thấy test fail.
        assertThrows(IllegalArgumentException.class, () -> semesterService.createSemester(request));
    }

    @Test
    void testSemesterService_CreateSemester_YearReversed() {
        // HK-43: Mục đích: Test logic năm bắt đầu lớn hơn năm kết thúc (vd: 2023-2022).
        // EXPECTED: Logic phải chặn lại báo lỗi. Thực tế: Chạy qua bình thường -> Bug nghiệp vụ.
        SemesterRequest request = new SemesterRequest();
        request.setSemesterName("Kỳ 1");
        request.setAcademicYear("2023-2022");
        request.setIsActive(true);

        lenient().when(semesterRepository.existsBySemesterNameAndAcademicYear(anyString(), anyString())).thenReturn(false);
        Semester saved = new Semester(); saved.setId(100L);
        lenient().when(semesterRepository.save(any())).thenReturn(saved);

        // Kịch bản TDD: Kỳ vọng code phải ném Exception, nếu không ném -> Test Fall, chứng tỏ lỗi chưa fix.
        assertThrows(RuntimeException.class, () -> semesterService.createSemester(request));
    }
}
