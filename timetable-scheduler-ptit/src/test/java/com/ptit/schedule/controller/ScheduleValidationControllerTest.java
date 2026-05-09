package com.ptit.schedule.controller;

import com.ptit.schedule.dto.ApiResponse;
import com.ptit.schedule.dto.ConflictResult;
import com.ptit.schedule.dto.ScheduleEntry;
import com.ptit.schedule.dto.ScheduleValidationResult;
import com.ptit.schedule.service.ScheduleConflictDetectionService;
import com.ptit.schedule.service.ScheduleExcelReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleValidationControllerTest {

    @Mock
    private ScheduleExcelReaderService excelReaderService;

    @Mock
    private ScheduleConflictDetectionService conflictDetectionService;

    @InjectMocks
    private ScheduleValidationController controller;

    private static MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    }

    @Test
    @DisplayName("LL-SVC-01: validateExcelFormat returns badRequest when file empty")
    void validateExcelFormat_shouldReturnBadRequestWhenEmpty() {
        MockMultipartFile empty = file("tkb.xlsx", new byte[0]);
        ApiResponse<Boolean> res = controller.validateExcelFormat(empty);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Vui lòng chọn file Excel");
        verifyNoInteractions(excelReaderService);
    }

    @Test
    @DisplayName("LL-SVC-02: validateExcelFormat returns badRequest when invalid format")
    void validateExcelFormat_shouldReturnBadRequestWhenInvalid() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1, 2, 3});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(false);

        ApiResponse<Boolean> res = controller.validateExcelFormat(f);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("không đúng định dạng");
    }

    @Test
    @DisplayName("LL-SVC-03: validateExcelFormat returns success when valid")
    void validateExcelFormat_shouldReturnSuccessWhenValid() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(true);

        ApiResponse<Boolean> res = controller.validateExcelFormat(f);
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getData()).isTrue();
    }

    @Test
    @DisplayName("LL-SVC-03b: validateExcelFormat returns error when service throws")
    void validateExcelFormat_shouldReturnErrorWhenServiceThrows() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenThrow(new RuntimeException("boom"));

        ApiResponse<Boolean> res = controller.validateExcelFormat(f);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Lỗi khi kiểm tra");
    }

    @Test
    @DisplayName("LL-SVC-04a: analyzeSchedule returns badRequest when file empty")
    void analyzeSchedule_shouldReturnBadRequestWhenFileEmpty() {
        MockMultipartFile empty = file("tkb.xlsx", new byte[0]);
        ApiResponse<ScheduleValidationResult> res = controller.analyzeSchedule(empty);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Vui lòng chọn file Excel");
        verifyNoInteractions(excelReaderService, conflictDetectionService);
    }

    @Test
    @DisplayName("LL-SVC-04b: analyzeSchedule returns badRequest when invalid format")
    void analyzeSchedule_shouldReturnBadRequestWhenInvalidFormat() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(false);

        ApiResponse<ScheduleValidationResult> res = controller.analyzeSchedule(f);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("không đúng định dạng");
        verify(excelReaderService, never()).readScheduleFromExcel(any());
        verifyNoInteractions(conflictDetectionService);
    }

    @Test
    @DisplayName("LL-SVC-04: analyzeSchedule happy path")
    void analyzeSchedule_shouldReturnValidationResult() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(true);

        ScheduleEntry.TimeSlot slot = ScheduleEntry.TimeSlot.builder()
                .date("Tuần 1").dayOfWeek("Thứ 2").shift("1").startPeriod("1").numberOfPeriods("3")
                .build();
        List<ScheduleEntry> entries = List.of(ScheduleEntry.builder()
                .subjectCode("INT1001")
                .subjectName("Nhap mon")
                .teacherId("GV01")
                .teacherName("Teacher 01")
                .room("401")
                .building("A1")
                .timeSlots(List.of(slot))
                .build());

        when(excelReaderService.readScheduleFromExcel(f)).thenReturn(entries);
        when(conflictDetectionService.detectConflicts(entries)).thenReturn(ConflictResult.builder()
                .roomConflicts(List.of())
                .teacherConflicts(List.of())
                .totalConflicts(0)
                .build());

        ApiResponse<ScheduleValidationResult> res = controller.analyzeSchedule(f);
        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getData().getTotalEntries()).isEqualTo(1);
        assertThat(res.getData().getFileName()).isEqualTo("tkb.xlsx");
        assertThat(res.getData().getConflictResult()).isNotNull();
    }

    @Test
    @DisplayName("LL-SVC-05: analyzeSchedule returns badRequest when entries empty")
    void analyzeSchedule_shouldReturnBadRequestWhenNoEntries() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(f)).thenReturn(List.of());

        ApiResponse<ScheduleValidationResult> res = controller.analyzeSchedule(f);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Không tìm thấy dữ liệu");
        verify(conflictDetectionService, never()).detectConflicts(any());
    }

    @Test
    @DisplayName("LL-SVC-06: analyzeSchedule returns error when unexpected exception")
    void analyzeSchedule_shouldReturnErrorWhenException() {
        MockMultipartFile f = file("tkb.xlsx", new byte[]{1});
        when(excelReaderService.validateScheduleExcelFormat(f)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(f)).thenThrow(new RuntimeException("read-fail"));

        ApiResponse<ScheduleValidationResult> res = controller.analyzeSchedule(f);
        assertThat(res.isSuccess()).isFalse();
        assertThat(res.getMessage()).contains("Lỗi khi xử lý file");
    }
}

