package com.ptit.schedule.controller;

import com.ptit.schedule.dto.*;
import com.ptit.schedule.entity.Schedule;
import com.ptit.schedule.entity.User;
import com.ptit.schedule.entity.Subject;
import com.ptit.schedule.entity.TKBTemplate;
import com.ptit.schedule.entity.Room;
import com.ptit.schedule.repository.SubjectRepository;
import com.ptit.schedule.repository.TKBTemplateRepository;
import com.ptit.schedule.repository.RoomRepository;
import com.ptit.schedule.service.ScheduleService;
import com.ptit.schedule.service.DataLoaderService;
import com.ptit.schedule.exception.InvalidDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private DataLoaderService dataLoaderService;

    @Mock
    private Authentication authentication;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TKBTemplateRepository tkbTemplateRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ScheduleController scheduleController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .build();
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("\n=== TEST: " + testInfo.getDisplayName() + " ===");
    }

    private void setupSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @Test
    @DisplayName("LL-01: Lưu lịch (batch) thành công")
    void saveSchedule_shouldReturnSuccessMessage() {
        setupSecurityContext();

        // Mock repository responses
        Subject subject = Subject.builder().id(100L).subjectCode("INT1001").subjectName("Nhap mon").build();
        TKBTemplate template = TKBTemplate.builder().id(200L).kip(30).build();
        Room room = Room.builder().id(1L).name("401").building("A1").build();

        when(subjectRepository.getReferenceById(100L)).thenReturn(subject);
        when(tkbTemplateRepository.getReferenceById(200L)).thenReturn(template);
        when(roomRepository.findByNameAndBuilding("401", "A1")).thenReturn(Optional.of(room));

        SaveScheduleRequest request1 = SaveScheduleRequest.builder()
                .subjectId(100L)
                .templateDatabaseId(200L)
                .roomNumber("401-A1")
                .classNumber(1)
                .studentYear("2024")
                .major("CNTT")
                .specialSystem("CLC")
                .siSoMotLop(50)
                .build();

        List<SaveScheduleRequest> requests = List.of(request1);

        System.out.println("INPUT: requests.size=" + requests.size() + ", request1=" + request1);
        ResponseEntity<String> response = scheduleController.saveSchedule(requests);

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body=" + response.getBody());
        System.out.println("EXPECTED: status=200, body=Đã lưu TKB vào database!");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Đã lưu TKB vào database!");
        verify(scheduleService).saveAll(anyList());
    }

    @Test
    @DisplayName("LL-02: Lưu lịch: ném lỗi khi danh sách request rỗng")
    void saveSchedule_shouldThrowWhenRequestListIsEmpty() {
        List<SaveScheduleRequest> emptyList = Collections.emptyList();

        System.out.println("INPUT: requests.size=0");
        Throwable thrown = catchThrowable(() -> scheduleController.saveSchedule(emptyList));
        System.out.println("OUTPUT: thrown=" + thrown);
        System.out.println("EXPECTED: InvalidDataException contains 'Danh sách lịch học không được rỗng'");

        assertThat(thrown)
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Danh sách lịch học không được rỗng");
    }

    @Test
    @DisplayName("LL-03: Lưu lịch: ném lỗi khi thiếu Subject ID")
    void saveSchedule_shouldThrowWhenSubjectIdIsNull() {
        SaveScheduleRequest request = SaveScheduleRequest.builder()
                .subjectId(null)
                .templateDatabaseId(200L)
                .roomNumber("401-A1")
                .build();

        System.out.println("INPUT: request=" + request);
        Throwable thrown = catchThrowable(() -> scheduleController.saveSchedule(List.of(request)));
        System.out.println("OUTPUT: thrown=" + thrown);
        System.out.println("EXPECTED: InvalidDataException contains 'Subject ID không được rỗng'");

        assertThat(thrown)
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Subject ID không được rỗng");
    }

    @Test
    @DisplayName("LL-04: Lưu lịch: ném lỗi khi thiếu Template ID")
    void saveSchedule_shouldThrowWhenTemplateIdIsNull() {
        SaveScheduleRequest request = SaveScheduleRequest.builder()
                .subjectId(100L)
                .templateDatabaseId(null)
                .roomNumber("401-A1")
                .build();

        System.out.println("INPUT: request=" + request);
        Throwable thrown = catchThrowable(() -> scheduleController.saveSchedule(List.of(request)));
        System.out.println("OUTPUT: thrown=" + thrown);
        System.out.println("EXPECTED: InvalidDataException contains 'Template ID không được rỗng'");

        assertThat(thrown)
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Template ID không được rỗng");
    }

    @Test
    @DisplayName("LL-05: Lấy toàn bộ lịch theo user")
    void getAllSchedules_shouldReturnUserSchedules() {
        setupSecurityContext();

        Schedule schedule = Schedule.builder().id(1L).build();
        List<Schedule> schedules = List.of(schedule);
        when(scheduleService.getSchedulesByUserId(1L)).thenReturn(schedules);

        System.out.println("INPUT: userId=1");
        ResponseEntity<List<Schedule>> response = scheduleController.getAllSchedules();

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body.size=" + (response.getBody() == null ? null : response.getBody().size()));
        System.out.println("EXPECTED: status=200, body.size=1");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("LL-06: Lấy lịch theo mã môn")
    void getSchedulesBySubject_shouldReturnSchedules() {
        Schedule schedule = Schedule.builder().id(1L).build();
        List<Schedule> schedules = List.of(schedule);
        when(scheduleService.getSchedulesBySubjectId("INT1001")).thenReturn(schedules);

        System.out.println("INPUT: subjectId=INT1001");
        ResponseEntity<List<Schedule>> response = scheduleController.getSchedulesBySubject("INT1001");

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body.size=" + (response.getBody() == null ? null : response.getBody().size()));
        System.out.println("EXPECTED: status=200, body.size=1");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("LL-07: Lấy lịch theo ngành")
    void getSchedulesByMajor_shouldReturnSchedules() {
        Schedule schedule = Schedule.builder().id(1L).build();
        List<Schedule> schedules = List.of(schedule);
        when(scheduleService.getSchedulesByMajor("CNTT")).thenReturn(schedules);

        System.out.println("INPUT: major=CNTT");
        ResponseEntity<List<Schedule>> response = scheduleController.getSchedulesByMajor("CNTT");

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body.size=" + (response.getBody() == null ? null : response.getBody().size()));
        System.out.println("EXPECTED: status=200, body.size=1");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("LL-08: Lấy lịch theo khóa (student year)")
    void getSchedulesByStudentYear_shouldReturnSchedules() {
        Schedule schedule = Schedule.builder().id(1L).build();
        List<Schedule> schedules = List.of(schedule);
        when(scheduleService.getSchedulesByStudentYear("2024")).thenReturn(schedules);

        System.out.println("INPUT: studentYear=2024");
        ResponseEntity<List<Schedule>> response = scheduleController.getSchedulesByStudentYear("2024");

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body.size=" + (response.getBody() == null ? null : response.getBody().size()));
        System.out.println("EXPECTED: status=200, body.size=1");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("LL-09: Xóa lịch theo ID thành công")
    void deleteSchedule_shouldReturnSuccessMessage() {
        doNothing().when(scheduleService).deleteScheduleById(100L);

        System.out.println("INPUT: id=100");
        ResponseEntity<String> response = scheduleController.deleteSchedule(100L);

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body=" + response.getBody());
        System.out.println("EXPECTED: status=200, body=Đã xóa lịch học!");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Đã xóa lịch học!");
        verify(scheduleService).deleteScheduleById(100L);
    }

    @Test
    @DisplayName("LL-10: Xóa lịch: ném lỗi khi ID không hợp lệ")
    void deleteSchedule_shouldThrowWhenIdIsInvalid() {
        System.out.println("INPUT: id=0");
        Throwable thrown1 = catchThrowable(() -> scheduleController.deleteSchedule(0L));
        System.out.println("OUTPUT: thrown=" + thrown1);
        System.out.println("EXPECTED: InvalidDataException contains 'ID lịch học không hợp lệ'");
        assertThat(thrown1)
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("ID lịch học không hợp lệ");

        System.out.println("INPUT: id=-1");
        Throwable thrown2 = catchThrowable(() -> scheduleController.deleteSchedule(-1L));
        System.out.println("OUTPUT: thrown=" + thrown2);
        System.out.println("EXPECTED: InvalidDataException contains 'ID lịch học không hợp lệ'");
        assertThat(thrown2)
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("ID lịch học không hợp lệ");
    }

    @Test
    @DisplayName("LL-11: Xóa toàn bộ lịch thành công")
    void deleteAllSchedules_shouldReturnSuccessMessage() {
        doNothing().when(scheduleService).deleteAllSchedules();

        System.out.println("INPUT: (no args)");
        ResponseEntity<String> response = scheduleController.deleteAllSchedules();

        System.out.println("OUTPUT: status=" + response.getStatusCode() + ", body=" + response.getBody());
        System.out.println("EXPECTED: status=200, body=Đã xóa toàn bộ lịch học!");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Đã xóa toàn bộ lịch học!");
        verify(scheduleService).deleteAllSchedules();
    }

    @Test
    @DisplayName("LL-12: Generate TKB batch: trả về response")
    void generateSchedule_shouldReturnBatchResponse() {
        TKBRequest item = TKBRequest.builder()
                .ma_mon("INT1001")
                .ten_mon("Nhap mon")
                .sotiet(30)
                .siso(100)
                .siso_mot_lop(50)
                .solop(1)
                .nganh("CNTT")
                .student_year("2024")
                .he_dac_thu("CLC")
                .academic_year("2024-2025")
                .semester("HK1")
                .build();

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(item))
                .build();

        TKBBatchResponse batchResponse = new TKBBatchResponse();
        batchResponse.setItems(List.of());
        batchResponse.setTotalClasses(1);
        batchResponse.setTotalRows(2);

        when(scheduleService.generateSchedule(any(TKBBatchRequest.class))).thenReturn(batchResponse);

        ResponseEntity<TKBBatchResponse> response = scheduleController.generateSchedule(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(batchResponse);
    }

    @Test
    @DisplayName("LL-13: Generate TKB batch: ném lỗi khi request null")
    void generateSchedule_shouldThrowWhenRequestIsNull() {
        assertThatThrownBy(() -> scheduleController.generateSchedule(null))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Danh sách môn học không được rỗng");
    }

    @Test
    @DisplayName("LL-14: Generate TKB batch: ném lỗi khi items null")
    void generateSchedule_shouldThrowWhenItemsIsNull() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(null)
                .build();

        assertThatThrownBy(() -> scheduleController.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Danh sách môn học không được rỗng");
    }

    @Test
    @DisplayName("LL-15: Generate TKB batch: ném lỗi khi items rỗng")
    void generateSchedule_shouldThrowWhenItemsIsEmpty() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(Collections.emptyList())
                .build();

        assertThatThrownBy(() -> scheduleController.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Danh sách môn học không được rỗng");
    }

    @Test
    @DisplayName("LL-16: Health check trả OK")
    void health_shouldReturnOk() {
        ResponseEntity<String> response = scheduleController.health();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Schedule Controller is OK");
    }

    @Test
    @DisplayName("LL-17: Test-data trả số lượng template")
    void testData_shouldReturnTemplateCount() {
        DataLoaderService.TKBTemplateRow row = new DataLoaderService.TKBTemplateRow(
                1L, 30, 2, 1, 1, 1, "TPL", List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), 30);
        List<DataLoaderService.TKBTemplateRow> templateData = List.of(row);
        when(dataLoaderService.loadTemplateData()).thenReturn(templateData);

        ResponseEntity<Map<String, Object>> response = scheduleController.testData();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("template_rows_count")).isEqualTo(1);
        assertThat(body.get("status")).isEqualTo("success");
    }

    @Test
    @DisplayName("LL-18: Reset state gọi service và trả success")
    void resetState_shouldCallServiceAndReturnSuccess() {
        doNothing().when(scheduleService).resetState();

        ResponseEntity<Map<String, Object>> response = scheduleController.resetState();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("status")).isEqualTo("success");
        assertThat(body.get("message")).isEqualTo("TKB state reset successfully");
        verify(scheduleService).resetState();
    }

    @Test
    @DisplayName("LL-19: Debug common subject trả thông tin debug")
    void debugCommonSubject_shouldReturnDebugInfo() {
        TKBRequest commonSubjectRequest = TKBRequest.builder()
                .ma_mon("SKD1102")
                .ten_mon("Kỹ năng làm việc nhóm")
                .sotiet(30)
                .siso(100)
                .siso_mot_lop(50)
                .solop(2)
                .nganh("Chung")
                .subject_type("general")
                .student_year("2024")
                .he_dac_thu("")
                .build();

        TKBBatchResponse batchResponse = new TKBBatchResponse();
        batchResponse.setItems(List.of());
        batchResponse.setTotalRows(2);
        batchResponse.setTotalClasses(1);

        when(scheduleService.generateSchedule(any(TKBBatchRequest.class))).thenReturn(batchResponse);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = scheduleController.debugCommonSubject();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Map<String, Object>> body = response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getMessage()).isEqualTo("Debug common subject completed");
    }

    @Test
    @DisplayName("LL-20: Lưu lastSlotIdx vào Redis: gọi service")
    void saveLastSlotIdxToRedis_shouldCallService() {
        doNothing().when(scheduleService).commitSessionToRedis(10L, "2024-2025", "HK1");

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                scheduleController.saveLastSlotIdxToRedis(10L, "2024-2025", "HK1");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Map<String, Object>> body = response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getMessage()).isEqualTo("Lưu lastSlotIdx thành công");
        verify(scheduleService).commitSessionToRedis(10L, "2024-2025", "HK1");
    }

    @Test
    @DisplayName("LL-21: Reset lastSlotIdx Redis: gọi service")
    void resetLastSlotIdxRedis_shouldCallService() {
        doNothing().when(scheduleService).resetLastSlotIndexRedis(10L, "2024-2025", "HK1");

        ResponseEntity<ApiResponse<Map<String, Object>>> response =
                scheduleController.resetLastSlotIdxRedis(10L, "2024-2025", "HK1");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Map<String, Object>> body = response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getMessage()).isEqualTo("Reset lastSlotIdx thành công");
        verify(scheduleService).resetLastSlotIndexRedis(10L, "2024-2025", "HK1");
    }

}
