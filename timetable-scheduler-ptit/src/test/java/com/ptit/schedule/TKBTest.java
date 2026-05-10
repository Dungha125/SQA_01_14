package com.ptit.schedule;

import com.ptit.schedule.controller.ScheduleController;
import com.ptit.schedule.dto.*;
import com.ptit.schedule.entity.*;
import com.ptit.schedule.entity.Semester;
import com.ptit.schedule.exception.*;
import com.ptit.schedule.repository.*;
import com.ptit.schedule.service.DataLoaderService;
import com.ptit.schedule.service.RedisService;
import com.ptit.schedule.service.RoomService;
import com.ptit.schedule.service.ScheduleService;
import com.ptit.schedule.service.SubjectRoomMappingService;
import com.ptit.schedule.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TKBTest {

    // =========================================================
    // TKB01-TKB18: ScheduleServiceImpl - lưu / lấy / xóa dữ liệu
    // =========================================================

    // TKB01: Lưu một schedule hợp lệ -> gọi saveAll một lần
    @Test
    @DisplayName("TKB01_saveAll_validSchedule_callsSaveAllOnce")
    void tkb01() {
        Subject subject = Subject.builder().id(1L).subjectCode("INT2201").build();
        Room room = Room.builder().id(1L).name("401").building("A2").build();
        TKBTemplate template = TKBTemplate.builder().id(1L).build();
        User user = User.builder().id(1L).username("teacher1").build();

        Schedule schedule = Schedule.builder()
                .subject(subject).room(room).tkbTemplate(template).user(user)
                .classNumber(1).studentYear("K64").major("CNTT")
                .specialSystem("Chinh quy").siSoMotLop(45).build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.saveAll(anyList())).thenReturn(Collections.emptyList());
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        service.saveAll(Collections.singletonList(schedule));

        verify(scheduleRepo, times(1)).saveAll(anyList());
    }

    // TKB02: Lưu nhiều schedule cùng lúc -> gọi saveAll một lần
    @Test
    @DisplayName("TKB02_saveAll_multipleSchedules_callsSaveAllOnce")
    void tkb02() {
        Subject subject = Subject.builder().id(1L).subjectCode("INT2201").build();
        Room room = Room.builder().id(1L).name("401").building("A2").build();
        TKBTemplate template = TKBTemplate.builder().id(1L).build();
        User user = User.builder().id(1L).build();

        Schedule s1 = Schedule.builder().subject(subject).room(room).tkbTemplate(template).user(user).classNumber(1).build();
        Schedule s2 = Schedule.builder().subject(subject).room(room).tkbTemplate(template).user(user).classNumber(2).build();
        Schedule s3 = Schedule.builder().subject(subject).room(room).tkbTemplate(template).user(user).classNumber(3).build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.saveAll(anyList())).thenReturn(Collections.emptyList());
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        service.saveAll(Arrays.asList(s1, s2, s3));

        verify(scheduleRepo, times(1)).saveAll(anyList());
    }

    // TKB03: Lưu schedule không có phòng -> vẫn lưu được
    @Test
    @DisplayName("TKB03_saveAll_nullRoom_savesSuccessfully")
    void tkb03() {
        Subject subject = Subject.builder().id(1L).subjectCode("INT2201").build();
        TKBTemplate template = TKBTemplate.builder().id(1L).build();
        User user = User.builder().id(1L).build();

        Schedule schedule = Schedule.builder()
                .subject(subject).room(null).tkbTemplate(template).user(user).classNumber(1).build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.saveAll(anyList())).thenReturn(Collections.emptyList());
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        service.saveAll(Collections.singletonList(schedule));

        verify(scheduleRepo, times(1)).saveAll(anyList());
    }

    // TKB04: Lấy tất cả schedule có dữ liệu -> trả về 2 schedule
    @Test
    @DisplayName("TKB04_getAllSchedules_withData_returnsSchedules")
    void tkb04() {
        Schedule s1 = Schedule.builder().id(1L).classNumber(1).build();
        Schedule s2 = Schedule.builder().id(2L).classNumber(2).build();
        List<Schedule> mockSchedules = Arrays.asList(s1, s2);

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findAll()).thenReturn(mockSchedules);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getAllSchedules();

        assertEquals(2, result.size(), "Must return 2 schedules");
        verify(scheduleRepo, times(1)).findAll();
    }

    // TKB05: Lấy tất cả schedule không có dữ liệu -> trả về danh sách rỗng
    @Test
    @DisplayName("TKB05_getAllSchedules_noData_returnsEmptyList")
    void tkb05() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findAll()).thenReturn(Collections.emptyList());
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getAllSchedules();

        assertTrue(result.isEmpty(), "Must return empty list");
        verify(scheduleRepo, times(1)).findAll();
    }

    // TKB06: Lấy schedule theo userId có dữ liệu -> trả về schedule của user
    @Test
    @DisplayName("TKB06_getSchedulesByUserId_withData_returnsSchedules")
    void tkb06() {
        Schedule s1 = Schedule.builder().id(1L).user(User.builder().id(1L).build()).build();
        List<Schedule> mockSchedules = Collections.singletonList(s1);

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findByUserIdOrderByIdAsc(1L)).thenReturn(mockSchedules);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getSchedulesByUserId(1L);

        assertEquals(1, result.size(), "Must return 1 schedule");
        verify(scheduleRepo, times(1)).findByUserIdOrderByIdAsc(1L);
    }

    // TKB07: Lấy schedule theo ngành có dữ liệu -> trả về schedule của ngành
    @Test
    @DisplayName("TKB07_getSchedulesByMajor_withData_returnsSchedules")
    void tkb07() {
        Subject subject = Subject.builder().id(1L).subjectCode("INT2201").build();
        Schedule s1 = Schedule.builder().id(1L).subject(subject).major("CNTT").build();
        Schedule s2 = Schedule.builder().id(2L).subject(subject).major("CNTT").build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findByMajor("CNTT")).thenReturn(Arrays.asList(s1, s2));
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getSchedulesByMajor("CNTT");

        assertEquals(2, result.size(), "Must return 2 schedules for CNTT");
        verify(scheduleRepo, times(1)).findByMajor("CNTT");
    }

    // TKB08: Lấy schedule theo khóa sinh viên có dữ liệu -> trả về schedule của khóa
    @Test
    @DisplayName("TKB08_getSchedulesByStudentYear_withData_returnsSchedules")
    void tkb08() {
        Schedule s1 = Schedule.builder().id(1L).studentYear("K64").build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findByStudentYear("K64")).thenReturn(Collections.singletonList(s1));
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getSchedulesByStudentYear("K64");

        assertEquals(1, result.size(), "Must return 1 schedule for K64");
        verify(scheduleRepo, times(1)).findByStudentYear("K64");
    }

    // TKB09: Lấy schedule theo mã môn học có dữ liệu -> trả về schedule của môn
    @Test
    @DisplayName("TKB09_getSchedulesBySubjectId_withData_returnsSchedules")
    void tkb09() {
        Schedule s1 = Schedule.builder().id(1L).build();

        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findBySubjectId("INT2201")).thenReturn(Collections.singletonList(s1));
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getSchedulesBySubjectId("INT2201");

        assertEquals(1, result.size(), "Must return 1 schedule for INT2201");
        verify(scheduleRepo, times(1)).findBySubjectId("INT2201");
    }

    // TKB10: Xóa schedule theo id hợp lệ -> gọi deleteById
    @Test
    @DisplayName("TKB10_deleteScheduleById_validId_callsDeleteById")
    void tkb10() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        doNothing().when(scheduleRepo).deleteById(1L);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        service.deleteScheduleById(1L);

        verify(scheduleRepo, times(1)).deleteById(1L);
    }

    // =========================================================
    // TKB11-TKB18: ScheduleServiceImpl - reset / commit / edge cases
    // =========================================================

    // TKB11: Xóa tất cả schedule -> gọi deleteAll
    @Test
    @DisplayName("TKB11_deleteAllSchedules_callsDeleteAll")
    void tkb11() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        doNothing().when(scheduleRepo).deleteAll();
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        service.deleteAllSchedules();

        verify(scheduleRepo, times(1)).deleteAll();
    }

    // TKB12: Reset trạng thái -> không ném ngoại lệ
    @Test
    @DisplayName("TKB12_resetState_noException")
    void tkb12() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        assertDoesNotThrow(() -> service.resetState(), "resetState() must not throw");
    }

    // TKB13: Sinh schedule với dữ liệu rỗng -> ném InvalidDataException
    @Test
    @DisplayName("TKB13_generateSchedule_emptyTemplateData_throwsInvalidDataException")
    void tkb13() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        when(dataLoader.loadTemplateData(anyString())).thenReturn(Collections.emptyList());

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .items(Collections.emptyList())
                .academicYear("2024-2025")
                .semester("HK1")
                .build();

        assertThrows(InvalidDataException.class, () -> service.generateSchedule(request),
                "Must throw InvalidDataException when no template data");
    }

    // TKB14: Sinh schedule nhưng học kỳ không tìm thấy -> dùng template data thay thế
    @Test
    @DisplayName("TKB14_generateSchedule_semesterNotFound_fallsBackToTemplateData")
    void tkb14() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        when(semesterRepo.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoader.loadTemplateData(anyString())).thenReturn(Collections.emptyList());

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .items(Collections.emptyList())
                .academicYear("2024-2025")
                .semester("HK1")
                .build();

        assertThrows(InvalidDataException.class, () -> service.generateSchedule(request));
    }

    // TKB15: Lưu session vào Redis -> lưu lastSlotIdx
    @Test
    @DisplayName("TKB15_commitSessionToRedis_savesLastSlotIdx")
    void tkb15() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        assertDoesNotThrow(() -> service.commitSessionToRedis(1L, "2024-2025", "HK1"));
        verify(redisSvc, times(1)).saveLastSlotIdx(1L, "2024-2025", "HK1", -1);
    }

    // TKB16: Reset lastSlotIndex trong Redis -> xóa và đặt lại
    @Test
    @DisplayName("TKB16_resetLastSlotIndexRedis_clearsAndResets")
    void tkb16() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        assertDoesNotThrow(() -> service.resetLastSlotIndexRedis(1L, "2024-2025", "HK1"));
        verify(redisSvc, times(1)).clearLastSlotIdx(1L, "2024-2025", "HK1");
    }

    // TKB17: Lưu session với tham số null -> không gọi Redis
    @Test
    @DisplayName("TKB17_commitSessionToRedis_nullParams_noRedisCall")
    void tkb17() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        assertDoesNotThrow(() -> service.commitSessionToRedis(null, "2024-2025", "HK1"));
        verify(redisSvc, never()).saveLastSlotIdx(anyLong(), anyString(), anyString(), anyInt());
    }

    // TKB18: Lấy schedule theo ngành không có dữ liệu -> trả về danh sách rỗng
    @Test
    @DisplayName("TKB18_getSchedulesByMajor_noData_returnsEmptyList")
    void tkb18() {
        ScheduleRepository scheduleRepo = mock(ScheduleRepository.class);
        when(scheduleRepo.findByMajor("VT")).thenReturn(Collections.emptyList());
        DataLoaderService dataLoader = mock(DataLoaderService.class);
        RoomService roomSvc = mock(RoomService.class);
        SubjectRoomMappingService subjectRoomMapSvc = mock(SubjectRoomMappingService.class);
        RedisService redisSvc = mock(RedisService.class);
        SemesterRepository semesterRepo = mock(SemesterRepository.class);
        SubjectRepository subjectRepo = mock(SubjectRepository.class);

        ScheduleServiceImpl service = new ScheduleServiceImpl(
                scheduleRepo, dataLoader, roomSvc, subjectRoomMapSvc, redisSvc, semesterRepo, subjectRepo);

        List<Schedule> result = service.getSchedulesByMajor("VT");

        assertTrue(result.isEmpty(), "Must return empty list");
        verify(scheduleRepo, times(1)).findByMajor("VT");
    }

    // =========================================================
    // TKB19-TKB35: ScheduleController - API endpoints
    // =========================================================

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private TKBTemplateRepository tkbTemplateRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private DataLoaderService dataLoaderService;

    @Mock
    private RoomRepository roomRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ScheduleController controller = new ScheduleController(
                scheduleService, tkbTemplateRepository, subjectRepository, dataLoaderService, roomRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(
                User.builder().id(1L).username("teacher1").build());
        SecurityContextHolder.setContext(securityContext);
    }

    // TKB19: Lưu batch với danh sách rỗng -> trả BadRequest
    @Test
    @DisplayName("TKB19_API_saveBatch_emptyList_returnsBadRequest")
    void tkb19() throws Exception {
        String emptyJson = "[]";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/save-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());

        verify(scheduleService, never()).saveAll(anyList());
    }

    // TKB20: Lưu batch với request hợp lệ -> gọi service
    @Test
    @DisplayName("TKB20_API_saveBatch_validRequest_callsService")
    void tkb20() throws Exception {
        Subject subject = Subject.builder().id(1L).build();
        TKBTemplate template = TKBTemplate.builder().id(1L).build();

        when(subjectRepository.getReferenceById(1L)).thenReturn(subject);
        when(tkbTemplateRepository.getReferenceById(1L)).thenReturn(template);
        doNothing().when(scheduleService).saveAll(anyList());

        String validJson = "[{\"subject_id\":1,\"template_database_id\":1,\"class_number\":1,\"student_year\":\"K64\",\"major\":\"CNTT\"}]";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/save-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        verify(scheduleService, times(1)).saveAll(anyList());
    }

    // TKB21: Lưu batch với subjectId rỗng -> trả BadRequest
    @Test
    @DisplayName("TKB21_API_saveBatch_nullSubjectId_returnsBadRequest")
    void tkb21() throws Exception {
        Subject subject = Subject.builder().id(1L).build();
        TKBTemplate template = TKBTemplate.builder().id(1L).build();

        when(subjectRepository.getReferenceById(1L)).thenReturn(subject);
        when(tkbTemplateRepository.getReferenceById(1L)).thenReturn(template);
        doNothing().when(scheduleService).saveAll(anyList());

        String invalidJson = "[{\"subject_id\":null,\"template_database_id\":1,\"class_number\":1}]";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/save-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    // TKB22: Lấy tất cả schedule có dữ liệu -> trả 200 cùng danh sách
    @Test
    @DisplayName("TKB22_API_getAllSchedules_withData_returnsSchedules")
    void tkb22() throws Exception {
        Schedule s1 = Schedule.builder().id(1L).classNumber(1).build();
        when(scheduleService.getSchedulesByUserId(1L)).thenReturn(Collections.singletonList(s1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.length()").value(1));

        verify(scheduleService, times(1)).getSchedulesByUserId(1L);
    }

    // TKB23: Lấy schedule theo môn -> trả danh sách schedule
    @Test
    @DisplayName("TKB23_API_getSchedulesBySubject_returnsSchedules")
    void tkb23() throws Exception {
        Schedule s1 = Schedule.builder().id(1L).build();
        when(scheduleService.getSchedulesBySubjectId("INT2201")).thenReturn(Collections.singletonList(s1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules/subject/INT2201"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.length()").value(1));

        verify(scheduleService, times(1)).getSchedulesBySubjectId("INT2201");
    }

    // TKB24: Lấy schedule theo ngành -> trả danh sách schedule
    @Test
    @DisplayName("TKB24_API_getSchedulesByMajor_returnsSchedules")
    void tkb24() throws Exception {
        Schedule s1 = Schedule.builder().id(1L).build();
        when(scheduleService.getSchedulesByMajor("CNTT")).thenReturn(Collections.singletonList(s1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules/major/CNTT"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isArray());

        verify(scheduleService, times(1)).getSchedulesByMajor("CNTT");
    }

    // TKB25: Lấy schedule theo khóa sinh viên -> trả danh sách schedule
    @Test
    @DisplayName("TKB25_API_getSchedulesByStudentYear_returnsSchedules")
    void tkb25() throws Exception {
        Schedule s1 = Schedule.builder().id(1L).build();
        when(scheduleService.getSchedulesByStudentYear("K64")).thenReturn(Collections.singletonList(s1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules/student-year/K64"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isArray());

        verify(scheduleService, times(1)).getSchedulesByStudentYear("K64");
    }

    // TKB26: Xóa schedule với id hợp lệ -> trả Ok
    @Test
    @DisplayName("TKB26_API_deleteSchedule_validId_returnsOk")
    void tkb26() throws Exception {
        doNothing().when(scheduleService).deleteScheduleById(1L);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/schedules/1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        verify(scheduleService, times(1)).deleteScheduleById(1L);
    }

    // TKB27: Xóa tất cả schedule -> trả Ok
    @Test
    @DisplayName("TKB27_API_deleteAllSchedules_returnsOk")
    void tkb27() throws Exception {
        doNothing().when(scheduleService).deleteAllSchedules();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/schedules"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        verify(scheduleService, times(1)).deleteAllSchedules();
    }

    // TKB28: Kiểm tra sức khỏe -> trả Ok
    @Test
    @DisplayName("TKB28_health_returnsOk")
    void tkb28() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules/health"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("Schedule Controller is OK"));

        verifyNoInteractions(scheduleService);
    }

    // TKB29: Lấy dữ liệu test -> trả dữ liệu
    @Test
    @DisplayName("TKB29_testData_returnsData")
    void tkb29() throws Exception {
        when(dataLoaderService.loadTemplateData()).thenReturn(Collections.emptyList());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules/test-data"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("success"));
    }

    // TKB30: Reset trạng thái -> trả thành công
    @Test
    @DisplayName("TKB30_resetState_returnsSuccess")
    void tkb30() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/reset"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("success"));

        verify(scheduleService, times(1)).resetState();
    }

    // TKB31: Sinh schedule batch với items rỗng -> trả BadRequest
    @Test
    @DisplayName("TKB31_generateSchedule_emptyItems_returnsBadRequest")
    void tkb31() throws Exception {
        String emptyJson = "{\"items\":[],\"academicYear\":\"2024-2025\",\"semester\":\"HK1\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/generate-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    // TKB32: Sinh schedule batch với items hợp lệ -> trả response
    @Test
    @DisplayName("TKB32_generateSchedule_validItems_returnsResponse")
    void tkb32() throws Exception {
        TKBBatchResponse mockResponse = TKBBatchResponse.builder()
                .items(Collections.emptyList())
                .totalRows(0)
                .totalClasses(0)
                .lastSlotIdx(-1)
                .build();
        when(scheduleService.generateSchedule(any(TKBBatchRequest.class))).thenReturn(mockResponse);

        String validJson = "{\"items\":[{\"ma_mon\":\"INT2201\",\"ten_mon\":\"Lap trinh C\",\"sotiet\":30,\"siso\":60,\"siso_mot_lop\":30,\"solop\":2,\"nganh\":\"CNTT\",\"academic_year\":\"2024-2025\",\"semester\":\"HK1\"}],\"academicYear\":\"2024-2025\",\"semester\":\"HK1\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/generate-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        verify(scheduleService, times(1)).generateSchedule(any(TKBBatchRequest.class));
    }

    // TKB33: Debug môn học chung -> trả kết quả
    @Test
    @DisplayName("TKB33_debugCommonSubject_returnsResult")
    void tkb33() throws Exception {
        TKBBatchItemResponse item = TKBBatchItemResponse.builder()
                .input(TKBRequest.builder().ma_mon("SKD1102").build())
                .rows(Collections.emptyList())
                .build();
        TKBBatchResponse mockResponse = TKBBatchResponse.builder()
                .items(Collections.singletonList(item))
                .totalRows(0)
                .totalClasses(1)
                .build();
        when(scheduleService.generateSchedule(any(TKBBatchRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/debug-common-subject"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    // TKB34: Lưu lastSlotIdx vào Redis -> trả thành công
    @Test
    @DisplayName("TKB34_saveLastSlotIdxToRedis_returnsSuccess")
    void tkb34() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/schedules/save-last-slot-idx")
                        .param("userId", "1")
                        .param("academicYear", "2024-2025")
                        .param("semester", "HK1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        verify(scheduleService, times(1)).commitSessionToRedis(1L, "2024-2025", "HK1");
    }

    // TKB35: Reset lastSlotIdx trong Redis -> trả thành công
    @Test
    @DisplayName("TKB35_resetLastSlotIdxRedis_returnsSuccess")
    void tkb35() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/schedules/reset-last-slot-idx-redis")
                        .param("userId", "1")
                        .param("academicYear", "2024-2025")
                        .param("semester", "HK1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        verify(scheduleService, times(1)).resetLastSlotIndexRedis(1L, "2024-2025", "HK1");
    }

    // =========================================================
    // TKB36-TKB37: GlobalExceptionHandler - xử lý ngoại lệ
    // =========================================================

    // TKB36: InvalidDataException -> trả 400
    @Test
    @DisplayName("TKB36_InvalidDataException_returns400")
    void tkb36() {
        MockMvc controllerWithException = MockMvcBuilders.standaloneSetup(
                        new ScheduleController(scheduleService, tkbTemplateRepository, subjectRepository, dataLoaderService, roomRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String emptyJson = "[]";

        assertDoesNotThrow(() -> {
            controllerWithException.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .post("/api/schedules/save-batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyJson))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        });
    }

    // TKB37: ResourceNotFoundException -> trả 404
    @Test
    @DisplayName("TKB37_ResourceNotFoundException_returns404")
    void tkb37() {
        assertThrows(InvalidDataException.class, () -> {
            throw new InvalidDataException("Subject ID không được rỗng");
        });
    }

    // =========================================================
    // TKB38-TKB48: DTO Tests - ApiResponse, SaveScheduleRequest, TKBRequest
    // =========================================================

    // TKB38: ApiResponse.success(T) -> tạo response với message "Success"
    // Dùng Integer để tránh xung đột với overload success(String message)
    @Test
    @DisplayName("TKB38_ApiResponse_successGeneric_createsResponse")
    void tkb38() {
        ApiResponse<Integer> response = ApiResponse.success(12345);
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals(Integer.valueOf(12345), response.getData());
        assertEquals(200, response.getStatus());
    }

    // TKB39: ApiResponse.success(String) -> tạo response
    @Test
    @DisplayName("TKB39_ApiResponse_successString_createsResponse")
    void tkb39() {
        ApiResponse<String> response = ApiResponse.success("Operation completed");
        assertTrue(response.isSuccess());
        assertEquals("Operation completed", response.getMessage());
        assertEquals(200, response.getStatus());
    }

    // TKB40: ApiResponse.success(T, String) -> tạo response
    @Test
    @DisplayName("TKB40_ApiResponse_successWithMessage_createsResponse")
    void tkb40() {
        ApiResponse<String> response = ApiResponse.success("data here", "Custom message");
        assertTrue(response.isSuccess());
        assertEquals("Custom message", response.getMessage());
        assertEquals("data here", response.getData());
    }

    // TKB41: ApiResponse.created(T) -> tạo response
    @Test
    @DisplayName("TKB41_ApiResponse_created_createsResponse")
    void tkb41() {
        ApiResponse<String> response = ApiResponse.created("new item");
        assertTrue(response.isSuccess());
        assertEquals("Created successfully", response.getMessage());
        assertEquals("new item", response.getData());
        assertEquals(201, response.getStatus());
    }

    // TKB42: ApiResponse.created(T, String) -> tạo response
    @Test
    @DisplayName("TKB42_ApiResponse_createdWithMessage_createsResponse")
    void tkb42() {
        ApiResponse<String> response = ApiResponse.created("new item", "Item created OK");
        assertTrue(response.isSuccess());
        assertEquals("Item created OK", response.getMessage());
        assertEquals(201, response.getStatus());
    }

    // TKB43: ApiResponse.error(String, int) -> tạo response lỗi
    @Test
    @DisplayName("TKB43_ApiResponse_error_createsErrorResponse")
    void tkb43() {
        ApiResponse<String> response = ApiResponse.error("Something went wrong", 500);
        assertFalse(response.isSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertEquals("Something went wrong", response.getError());
        assertEquals(500, response.getStatus());
    }

    // TKB44: ApiResponse.error(String, String, int) -> tạo response lỗi
    @Test
    @DisplayName("TKB44_ApiResponse_errorWithDetails_createsErrorResponse")
    void tkb44() {
        ApiResponse<String> response = ApiResponse.error("Error message", "Detailed error", 400);
        assertFalse(response.isSuccess());
        assertEquals("Error message", response.getMessage());
        assertEquals("Detailed error", response.getError());
        assertEquals(400, response.getStatus());
    }

    // TKB45: ApiResponse.notFound(String) -> tạo response 404
    @Test
    @DisplayName("TKB45_ApiResponse_notFound_creates404Response")
    void tkb45() {
        ApiResponse<String> response = ApiResponse.notFound("Resource not found");
        assertFalse(response.isSuccess());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("Resource not found", response.getError());
        assertEquals(404, response.getStatus());
    }

    // TKB46: ApiResponse.badRequest(String) -> tạo response 400
    @Test
    @DisplayName("TKB46_ApiResponse_badRequest_creates400Response")
    void tkb46() {
        ApiResponse<String> response = ApiResponse.badRequest("Invalid input");
        assertFalse(response.isSuccess());
        assertEquals("Invalid input", response.getMessage());
        assertEquals("Bad request", response.getError());
        assertEquals(400, response.getStatus());
    }

    // TKB47: SaveScheduleRequest builder -> tạo request
    @Test
    @DisplayName("TKB47_SaveScheduleRequest_builder_createsRequest")
    void tkb47() {
        SaveScheduleRequest req = SaveScheduleRequest.builder()
                .subjectId(1L)
                .classNumber(1)
                .studentYear("K64")
                .major("CNTT")
                .specialSystem("Chinh quy")
                .siSoMotLop(45)
                .roomNumber("401-A2")
                .templateDatabaseId(1L)
                .build();

        assertEquals(1L, req.getSubjectId());
        assertEquals(1, req.getClassNumber());
        assertEquals("K64", req.getStudentYear());
        assertEquals("CNTT", req.getMajor());
        assertEquals("Chinh quy", req.getSpecialSystem());
        assertEquals(45, req.getSiSoMotLop());
        assertEquals("401-A2", req.getRoomNumber());
        assertEquals(1L, req.getTemplateDatabaseId());
    }

    // TKB48: TKBRequest builder và các getter tương thích ngược
    @Test
    @DisplayName("TKB48_TKBRequest_builderAndGetters_worksCorrectly")
    void tkb48() {
        TKBRequest req = TKBRequest.builder()
                .ma_mon("INT2201")
                .ten_mon("Lap trinh C")
                .sotiet(30)
                .siso(60)
                .siso_mot_lop(30)
                .solop(2)
                .nganh("CNTT")
                .subject_type("regular")
                .student_year("K64")
                .he_dac_thu("Chinh quy")
                .academic_year("2024-2025")
                .semester("HK1")
                .build();

        assertEquals("INT2201", req.getMa_mon());
        assertEquals("INT2201", req.getSubjectId());
        assertEquals("Lap trinh C", req.getTen_mon());
        assertEquals("Lap trinh C", req.getSubjectName());
        assertEquals(30, req.getSotiet());
        assertEquals(30, req.getTotalPeriods());
        assertEquals(60, req.getSiso());
        assertEquals(60, req.getNumberOfStudents());
        assertEquals(30, req.getSiso_mot_lop());
        assertEquals(30, req.getStudentsPerClass());
        assertEquals(2, req.getSolop());
        assertEquals(2, req.getNumberOfClasses());
        assertEquals("CNTT", req.getNganh());
        assertEquals("CNTT", req.getMajor());
        assertEquals("regular", req.getSubject_type());
        assertEquals("regular", req.getSubjectType());
        assertEquals("K64", req.getStudent_year());
        assertEquals("K64", req.getStudentYear());
        assertEquals("Chinh quy", req.getHe_dac_thu());
        assertEquals("Chinh quy", req.getSpecialSystem());
        assertEquals("2024-2025", req.getAcademic_year());
        assertEquals("2024-2025", req.getAcademicYear());
        assertEquals("HK1", req.getSemester());
    }
}
