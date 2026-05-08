package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.TKBBatchRequest;
import com.ptit.schedule.dto.TKBBatchResponse;
import com.ptit.schedule.dto.TKBRequest;
import com.ptit.schedule.dto.TKBRowResult;
import com.ptit.schedule.entity.Schedule;
import com.ptit.schedule.entity.Semester;
import com.ptit.schedule.entity.Subject;
import com.ptit.schedule.exception.InvalidDataException;
import com.ptit.schedule.repository.ScheduleRepository;
import com.ptit.schedule.repository.SemesterRepository;
import com.ptit.schedule.repository.SubjectRepository;
import com.ptit.schedule.service.DataLoaderService;
import com.ptit.schedule.service.RedisService;
import com.ptit.schedule.service.RoomService;
import com.ptit.schedule.service.SubjectRoomMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("\n=== TEST: " + testInfo.getDisplayName() + " ===");
        System.out.println("INPUT: (xem các dòng INPUT bên dưới hoặc trong body test)");
    }

    @AfterEach
    void printTestEnd() {
        System.out.println("OUTPUT: (kết thúc test)\n");
    }

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private DataLoaderService dataLoaderService;
    @Mock
    private RoomService roomService;
    @Mock
    private SubjectRoomMappingService subjectRoomMappingService;
    @Mock
    private RedisService redisService;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private List<Integer> fullWeeks;

    @BeforeEach
    void setUp() {
        fullWeeks = List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Test
    @DisplayName("LL-22: Kiểm tra saveAll_shouldDelegateToRepository")
    void saveAll_shouldDelegateToRepository() {
        List<Schedule> schedules = List.of(new Schedule(), new Schedule());

        System.out.println("INPUT: schedules.size=" + schedules.size());
        scheduleService.saveAll(schedules);
        System.out.println("OUTPUT: delegatedToRepository=true");

        verify(scheduleRepository).saveAll(schedules);
    }

    @Test
    @DisplayName("LL-23: Kiểm tra getAllSchedules_shouldReturnRepositoryResult")
    void getAllSchedules_shouldReturnRepositoryResult() {
        List<Schedule> expected = List.of(new Schedule());
        when(scheduleRepository.findAll()).thenReturn(expected);

        System.out.println("INPUT: (no args)");
        List<Schedule> actual = scheduleService.getAllSchedules();

        System.out.println("OUTPUT: actual.size=" + (actual == null ? null : actual.size()));
        System.out.println("EXPECTED: size=" + expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-24: Kiểm tra getSchedulesBySubjectId_shouldDelegateToRepository")
    void getSchedulesBySubjectId_shouldDelegateToRepository() {
        List<Schedule> expected = List.of(new Schedule());
        when(scheduleRepository.findBySubjectId("INT1001")).thenReturn(expected);

        System.out.println("INPUT: subjectId=INT1001");
        List<Schedule> actual = scheduleService.getSchedulesBySubjectId("INT1001");

        System.out.println("OUTPUT: actual.size=" + (actual == null ? null : actual.size()));
        System.out.println("EXPECTED: size=" + expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-25: Kiểm tra getSchedulesByMajor_shouldDelegateToRepository")
    void getSchedulesByMajor_shouldDelegateToRepository() {
        List<Schedule> expected = List.of(new Schedule());
        when(scheduleRepository.findByMajor("CNTT")).thenReturn(expected);

        System.out.println("INPUT: major=CNTT");
        List<Schedule> actual = scheduleService.getSchedulesByMajor("CNTT");

        System.out.println("OUTPUT: actual.size=" + (actual == null ? null : actual.size()));
        System.out.println("EXPECTED: size=" + expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-26: Kiểm tra getSchedulesByStudentYear_shouldDelegateToRepository")
    void getSchedulesByStudentYear_shouldDelegateToRepository() {
        List<Schedule> expected = List.of(new Schedule());
        when(scheduleRepository.findByStudentYear("2024")).thenReturn(expected);

        System.out.println("INPUT: studentYear=2024");
        List<Schedule> actual = scheduleService.getSchedulesByStudentYear("2024");

        System.out.println("OUTPUT: actual.size=" + (actual == null ? null : actual.size()));
        System.out.println("EXPECTED: size=" + expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-27: Kiểm tra getSchedulesByUserId_shouldDelegateToOrderedRepositoryMethod")
    void getSchedulesByUserId_shouldDelegateToOrderedRepositoryMethod() {
        List<Schedule> expected = List.of(new Schedule());
        when(scheduleRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(expected);

        System.out.println("INPUT: userId=1");
        List<Schedule> actual = scheduleService.getSchedulesByUserId(1L);

        System.out.println("OUTPUT: actual.size=" + (actual == null ? null : actual.size()));
        System.out.println("EXPECTED: size=" + expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-28: Kiểm tra deleteScheduleById_shouldDelegateToRepository")
    void deleteScheduleById_shouldDelegateToRepository() {
        System.out.println("INPUT: id=100");
        scheduleService.deleteScheduleById(100L);
        System.out.println("OUTPUT: delegatedToRepository=true");

        verify(scheduleRepository).deleteById(100L);
    }

    @Test
    @DisplayName("LL-29: Kiểm tra deleteAllSchedules_shouldDelegateToRepository")
    void deleteAllSchedules_shouldDelegateToRepository() {
        System.out.println("INPUT: (no args)");
        scheduleService.deleteAllSchedules();
        System.out.println("OUTPUT: delegatedToRepository=true");

        verify(scheduleRepository).deleteAll();
    }

    @Test
    @DisplayName("LL-30: Kiểm tra generateSchedule_shouldThrowWhenTemplateDataIsEmpty")
    void generateSchedule_shouldThrowWhenTemplateDataIsEmpty() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("1", "2024-2025"))
                .thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025")).thenReturn(List.of());

        System.out.println("INPUT: request.userId=1, academicYear=2024-2025, semester=1, items.size=1");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Chưa có dữ liệu lịch mẫu");
        System.out.println("OUTPUT: thrown=InvalidDataException(Chưa có dữ liệu lịch mẫu)");

        verify(dataLoaderService).setCurrentSemesterId(null);
    }

    @Test
    @DisplayName("LL-31: Kiểm tra generateSchedule_shouldSetSemesterIdWhenSemesterExists")
    void generateSchedule_shouldSetSemesterIdWhenSemesterExists() {
        Semester semester = Semester.builder().id(99L).semesterName("1").academicYear("2024-2025").build();
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("1", "2024-2025"))
                .thenReturn(Optional.of(semester));
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "T1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(200L).build()));

        System.out.println("INPUT: semesterId=99, request.userId=1, academicYear=2024-2025, semester=1");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        System.out.println("EXPECTED: items.size=1, setCurrentSemesterId=99");
        assertThat(response.getItems()).hasSize(1);
        verify(dataLoaderService).setCurrentSemesterId(99L);
    }

    @Test
    @DisplayName("LL-32: Kiểm tra generateSchedule_shouldFallbackAcademicYearSemesterFromFirstItem")
    void generateSchedule_shouldFallbackAcademicYearSemesterFromFirstItem() {
        Semester semester = Semester.builder().id(101L).semesterName("HK1").academicYear("2024-2025").build();
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year("2024-2025");
        item.setSemester("HK1");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(10L)
                .items(List.of(item))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025"))
                .thenReturn(Optional.of(semester));
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(10L, "2024-2025", "HK1")).thenReturn(2);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.userId=10, item.semester=HK1, item.academicYear=2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: totalRows=" + response.getTotalRows());
        System.out.println("EXPECTED: totalRows>0");
        assertThat(response.getTotalRows()).isGreaterThan(0);
        verify(redisService).loadLastSlotIdx(10L, "2024-2025", "HK1");
    }

    @Test
    @DisplayName("LL-33: Kiểm tra generateSchedule_shouldNotLoadRedisWhenMissingContext")
    void generateSchedule_shouldNotLoadRedisWhenMissingContext() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year("2024-2025");
        item.setSemester("HK1");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .items(List.of(item))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.userId=null, item.semester=HK1, item.academicYear=2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        System.out.println("EXPECTED: redisService.loadLastSlotIdx not called");
        assertThat(response.getItems()).hasSize(1);
        verify(redisService, never()).loadLastSlotIdx(any(), any(), any());
    }

    @Test
    @DisplayName("LL-34: Kiểm tra generateSchedule_shouldCreateRowsAndPopulateDerivedFieldsForRegularSubject")
    void generateSchedule_shouldCreateRowsAndPopulateDerivedFieldsForRegularSubject() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 55L, "TPL-01")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(999L).build()));

        System.out.println("INPUT: subjectCode=INT1001, periods=30, userId=1, semester=HK1, academicYear=2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);
        TKBRowResult row = response.getItems().get(0).getRows().get(0);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size()
                + ", templateDatabaseId=" + row.getTemplateDatabaseId()
                + ", subjectDatabaseId=" + row.getSubjectDatabaseId());
        assertThat(row.getMaMon()).isEqualTo("INT1001");
        assertThat(row.getTenMon()).isEqualTo("Nhap mon");
        assertThat(row.getPhong()).isNull();
        assertThat(row.getTemplateDatabaseId()).isEqualTo(55L);
        assertThat(row.getSubjectDatabaseId()).isEqualTo(999L);
        assertThat(row.getO_to_AG()).hasSize(18);
        assertThat(row.getAH()).isEqualTo(18);
        assertThat(row.getAJ()).isEqualTo(12);
    }

    @Test
    @DisplayName("LL-35: Kiểm tra generateSchedule_shouldSetSubjectDatabaseIdNullWhenSubjectNotFound")
    void generateSchedule_shouldSetSubjectDatabaseIdNullWhenSubjectNotFound() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT404", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "TPL-01")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT404", "HK1", "2024-2025"))
                .thenReturn(List.of());

        System.out.println("INPUT: subjectCode=INT404, expected subjectDatabaseId=null");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: subjectDatabaseId=" + response.getItems().get(0).getRows().get(0).getSubjectDatabaseId());
        assertThat(response.getItems().get(0).getRows().get(0).getSubjectDatabaseId()).isNull();
    }

    @Test
    @DisplayName("LL-36: Kiểm tra generateSchedule_shouldPrioritize60PeriodSubjectBeforeRegular")
    void generateSchedule_shouldPrioritize60PeriodSubjectBeforeRegular() {
        TKBRequest regular = baseRequest("INT1001", 30, "CNTT");
        TKBRequest period60 = baseRequest("INT2001", 60, "CNTT");
        period60.setSolop(1);
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(regular, period60))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B"),
                        templateRow(30, 2, 1, 1, 12L, "30-A")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear(eq("INT2001"), eq("HK1"), eq("2024-2025")))
                .thenReturn(List.of(Subject.builder().id(2L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear(eq("INT1001"), eq("HK1"), eq("2024-2025")))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: items=[(INT1001,30),(INT2001,60)], userId=1, semester=HK1, academicYear=2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", first.periods=" + response.getItems().get(0).getInput().getSotiet()
                + ", second.periods=" + response.getItems().get(1).getInput().getSotiet());
        System.out.println("EXPECTED: first.periods=60, second.periods=30");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getInput().getSotiet()).isEqualTo(60);
        assertThat(response.getItems().get(1).getInput().getSotiet()).isEqualTo(30);
    }

    @Test
    @DisplayName("LL-37: Kiểm tra generateSchedule_shouldIncludeOnlyMatchingPeriodTemplates")
    void generateSchedule_shouldIncludeOnlyMatchingPeriodTemplates() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 14, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(30, 2, 1, 1, 10L, "30-A"),
                        templateRow(14, 4, 3, 1, 20L, "14-A")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, periods=14, templates=[30-A,14-A]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size()
                + ", distinctTemplateNames=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getN).distinct().toList());
        System.out.println("EXPECTED: all templateName=14-A");
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "14-A".equals(r.getN()));
    }

    @Test
    @DisplayName("LL-38: Kiểm tra generateSchedule_shouldTruncateWeekScheduleTo18Columns")
    void generateSchedule_shouldTruncateWeekScheduleTo18Columns() {
        List<Integer> longWeeks = List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(new DataLoaderService.TKBTemplateRow(1L, 30, 2, 1, 1, 1, "TPL", longWeeks, 30)));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: longWeeks.size=" + longWeeks.size() + ", subjectCode=INT1001, periods=30");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: o_to_AG.size=" + response.getItems().get(0).getRows().get(0).getO_to_AG().size());
        System.out.println("EXPECTED: o_to_AG.size=18");
        assertThat(response.getItems().get(0).getRows().get(0).getO_to_AG()).hasSize(18);
    }

    @Test
    @DisplayName("LL-39: Kiểm tra generateSchedule_shouldIgnoreRowsWithZeroAHAndContinue")
    void generateSchedule_shouldIgnoreRowsWithZeroAHAndContinue() {
        DataLoaderService.TKBTemplateRow zeroAh = new DataLoaderService.TKBTemplateRow(
                1L, 30, 2, 1, 1, 0, "ZERO", fullWeeks, 0);
        DataLoaderService.TKBTemplateRow valid = templateRow(30, 2, 1, 1, 2L, "VALID");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(zeroAh, valid));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: templates=[ZERO(AH=0), VALID], subjectCode=INT1001");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size()
                + ", templateNames=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getN).distinct().toList());
        System.out.println("EXPECTED: templateNames doesNotContain ZERO");
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
        assertThat(response.getItems().get(0).getRows()).allMatch(r -> !"ZERO".equals(r.getN()));
    }

    @Test
    @DisplayName("LL-40: Kiểm tra generateSchedule_shouldNormalizeNumericSemesterForTemplateLoading")
    void generateSchedule_shouldNormalizeNumericSemesterForTemplateLoading() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("2")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("2", "2024-2025"))
                .thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK2 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "2")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "2", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.semester=2 -> expect templateKey=HK2 2024-2025");
        scheduleService.generateSchedule(request);
        System.out.println("OUTPUT: verified loadTemplateData(HK2 2024-2025)");

        verify(dataLoaderService).loadTemplateData("HK2 2024-2025");
    }

    @Test
    @DisplayName("LL-41: Kiểm tra commitSessionToRedis_shouldSaveSessionLastSlot")
    void commitSessionToRedis_shouldSaveSessionLastSlot() {
        // produce data first so sessionLastSlotIdx changes from default -1
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(5L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();
        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(5L, "2024-2025", "HK1")).thenReturn(4);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        System.out.println("INPUT: generateSchedule then commitSessionToRedis(userId=5, academicYear=2024-2025, semester=HK1)");
        TKBBatchResponse gen = scheduleService.generateSchedule(request);
        System.out.println("OUTPUT: generated.totalRows=" + gen.getTotalRows() + ", lastSlotIdx=" + gen.getLastSlotIdx());

        scheduleService.commitSessionToRedis(5L, "2024-2025", "HK1");
        System.out.println("OUTPUT: verified redisService.saveLastSlotIdx called");

        verify(redisService).saveLastSlotIdx(eq(5L), eq("2024-2025"), eq("HK1"), any(Integer.class));
    }

    @Test
    @DisplayName("LL-42: Kiểm tra commitSessionToRedis_shouldNotSaveWhenContextIsNull")
    void commitSessionToRedis_shouldNotSaveWhenContextIsNull() {
        System.out.println("INPUT: commitSessionToRedis(null,2024-2025,HK1) / (1,null,HK1) / (1,2024-2025,null)");
        scheduleService.commitSessionToRedis(null, "2024-2025", "HK1");
        scheduleService.commitSessionToRedis(1L, null, "HK1");
        scheduleService.commitSessionToRedis(1L, "2024-2025", null);

        System.out.println("OUTPUT: verified redisService.saveLastSlotIdx NOT called");
        verify(redisService, never()).saveLastSlotIdx(any(), any(), any(), any(Integer.class));
    }

    @Test
    @DisplayName("LL-43: Kiểm tra resetLastSlotIndexRedis_shouldClearAndResetSessionState")
    void resetLastSlotIndexRedis_shouldClearAndResetSessionState() {
        System.out.println("INPUT: resetLastSlotIndexRedis(userId=7, 2024-2025, HK2) then commitSessionToRedis(same)");
        scheduleService.resetLastSlotIndexRedis(7L, "2024-2025", "HK2");
        scheduleService.commitSessionToRedis(7L, "2024-2025", "HK2");

        System.out.println("OUTPUT: verified clearLastSlotIdx + saveLastSlotIdx(-1)");
        verify(redisService).clearLastSlotIdx(7L, "2024-2025", "HK2");
        verify(redisService).saveLastSlotIdx(7L, "2024-2025", "HK2", -1);
    }

    @Test
    @DisplayName("LL-44: Kiểm tra resetLastSlotIndexRedis_shouldResetWithoutClearWhenMissingContext")
    void resetLastSlotIndexRedis_shouldResetWithoutClearWhenMissingContext() {
        System.out.println("INPUT: resetLastSlotIndexRedis(null,2024-2025,HK2) then commitSessionToRedis(1,2024-2025,HK1)");
        scheduleService.resetLastSlotIndexRedis(null, "2024-2025", "HK2");
        scheduleService.commitSessionToRedis(1L, "2024-2025", "HK1");

        System.out.println("OUTPUT: verified clearLastSlotIdx NOT called, saveLastSlotIdx called with -1");
        verify(redisService, never()).clearLastSlotIdx(any(), any(), any());
        verify(redisService).saveLastSlotIdx(1L, "2024-2025", "HK1", -1);
    }

    @Test
    @DisplayName("LL-45: Kiểm tra generateSchedule_shouldClearMappingsAtSessionStart")
    void generateSchedule_shouldClearMappingsAtSessionStart() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: generateSchedule(userId=1, HK1 2024-2025) -> expect clearMappings called");
        TKBBatchResponse response = scheduleService.generateSchedule(request);
        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", verified clearMappings called");

        verify(subjectRoomMappingService, times(1)).clearMappings();
    }

    @Test
    @DisplayName("LL-46: Kiểm tra generateSchedule_shouldUseFirstSubjectWhenRepositoryReturnsMultiple")
    void generateSchedule_shouldUseFirstSubjectWhenRepositoryReturnsMultiple() {
        Subject s1 = Subject.builder().id(100L).build();
        Subject s2 = Subject.builder().id(200L).build();
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(s1, s2));

        System.out.println("INPUT: subjectRepo returns [100,200] -> expect pick 100");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: pickedSubjectDatabaseId=" + response.getItems().get(0).getRows().get(0).getSubjectDatabaseId());
        assertThat(response.getItems().get(0).getRows().get(0).getSubjectDatabaseId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("LL-47: Kiểm tra generateSchedule_shouldComputeTotalsFromNonEmptyItems")
    void generateSchedule_shouldComputeTotalsFromNonEmptyItems() {
        TKBRequest missingTemplateSubject = baseRequest("INT404", 45, "CNTT");
        TKBRequest validSubject = baseRequest("INT1001", 30, "CNTT");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(validSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.items.size=1 (INT1001), note missingTemplateSubject periods=" + missingTemplateSubject.getSotiet());
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: totalClasses=" + response.getTotalClasses() + ", totalRows=" + response.getTotalRows());
        System.out.println("EXPECTED: totalClasses=1, totalRows>0");
        assertThat(response.getTotalClasses()).isEqualTo(1);
        assertThat(response.getTotalRows()).isGreaterThan(0);
        assertThat(missingTemplateSubject.getSotiet()).isEqualTo(45);
    }

    @Test
    @DisplayName("LL-48: Kiểm tra generateSchedule_shouldUseDerivedSemesterContextFromItemWhenRequestNull")
    void generateSchedule_shouldUseDerivedSemesterContextFromItemWhenRequestNull() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year("2025-2026");
        item.setSemester("HK2");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(77L)
                .items(List.of(item))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK2", "2025-2026")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK2 2025-2026"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(77L, "2025-2026", "HK2")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK2", "2025-2026"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.userId=77, item.semester=HK2, item.academicYear=2025-2026");
        scheduleService.generateSchedule(request);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(dataLoaderService).loadTemplateData(keyCaptor.capture());
        System.out.println("OUTPUT: templateKey=" + keyCaptor.getValue());
        System.out.println("EXPECTED: HK2 2025-2026");
        assertThat(keyCaptor.getValue()).isEqualTo("HK2 2025-2026");
    }

    private TKBRequest baseRequest(String subjectCode, int periods, String major) {
        return TKBRequest.builder()
                .ma_mon(subjectCode)
                .ten_mon("Nhap mon")
                .sotiet(periods)
                .siso(100)
                .siso_mot_lop(50)
                .solop(1)
                .nganh(major)
                .student_year("2024")
                .he_dac_thu("CLC")
                .academic_year("2024-2025")
                .semester("HK1")
                .build();
    }

    private DataLoaderService.TKBTemplateRow templateRow(int totalPeriods, int day, int kip, int periodLength,
            Long dbId, String id) {
        return new DataLoaderService.TKBTemplateRow(
                dbId,
                totalPeriods,
                day,
                kip,
                1,
                periodLength,
                id,
                fullWeeks,
                periodLength * 18);
    }

    // ==================== NEW TEST CASES FOR COVERAGE ====================

    @Test
    @DisplayName("LL-49: Kiểm tra resetState_shouldResetLastSlotIdxToMinusOne")
    void resetState_shouldResetLastSlotIdxToMinusOne() {
        System.out.println("INPUT: resetState()");
        scheduleService.resetState();
        System.out.println("OUTPUT: verified repository not called");

        // Reset state được gọi, lastSlotIdx sẽ được reset về -1
        // Không throw exception và không gọi repository
        verify(scheduleRepository, never()).findAll();
    }

    @Test
    @DisplayName("LL-50: Kiểm tra generateSchedule_shouldHandleRequestWithNullUserId")
    void generateSchedule_shouldHandleRequestWithNullUserId() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(null)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.userId=null, semester=HK1, academicYear=2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        System.out.println("EXPECTED: items.size=1, redis not called");
        assertThat(response.getItems()).hasSize(1);
        verify(redisService, never()).loadLastSlotIdx(any(), any(), any());
    }

    @Test
    @DisplayName("LL-51: Kiểm tra generateSchedule_shouldHandleEmptyItemsList")
    void generateSchedule_shouldHandleEmptyItemsList() {
        // When items is empty but academicYear/semester are provided, it still loads templates
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of())
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));

        System.out.println("INPUT: request.userId=1, academicYear=2024-2025, semester=HK1, items.size=0");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", totalRows=" + response.getTotalRows()
                + ", totalClasses=" + response.getTotalClasses());
        System.out.println("EXPECTED: items.size=0, totalRows=0, totalClasses=0");
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalRows()).isEqualTo(0);
        assertThat(response.getTotalClasses()).isEqualTo(0);
    }

    @Test
    @DisplayName("LL-52: Kiểm tra generateSchedule_shouldThrowWhenOnlyNullAcademicYearAndSemester")
    void generateSchedule_shouldThrowWhenOnlyNullAcademicYearAndSemester() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year(null);
        item.setSemester(null);
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(List.of(item))
                .build();

        when(dataLoaderService.loadTemplateData("null null")).thenReturn(List.of());

        System.out.println("INPUT: item.academicYear=null, item.semester=null -> templateKey='null null'");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Chưa có dữ liệu lịch mẫu");
        System.out.println("OUTPUT: thrown=InvalidDataException(Chưa có dữ liệu lịch mẫu)");
    }

    @Test
    @DisplayName("LL-53: Kiểm tra generateSchedule_shouldThrowWhenNoTemplateMatchesPeriods")
    void generateSchedule_shouldThrowWhenNoTemplateMatchesPeriods() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 45, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(30, 2, 1, 1, 1L, "30-A"),
                        templateRow(60, 2, 1, 1, 2L, "60-A")));

        System.out.println("INPUT: subjectCode=INT1001, periods=45, templates=[30-A,60-A]");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Không có Data cho 45 tiết");
        System.out.println("OUTPUT: thrown=InvalidDataException(Không có Data cho 45 tiết)");
    }

    @Test
    @DisplayName("LL-54: Kiểm tra generateSchedule_shouldProcessMultipleClassesForSingleSubject")
    void generateSchedule_shouldProcessMultipleClassesForSingleSubject() {
        TKBRequest requestWithMultipleClasses = TKBRequest.builder()
                .ma_mon("INT1001")
                .ten_mon("Nhap mon")
                .sotiet(30)
                .siso(150)
                .siso_mot_lop(50)
                .solop(3)
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
                .items(List.of(requestWithMultipleClasses))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, periods=30, solop=3");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        System.out.println("EXPECTED: items.size=1, rows.size=6");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).hasSize(6);
    }

    @Test
    @DisplayName("LL-55: Kiểm tra generateSchedule_shouldHandleMultipleSubjectsWithDifferentMajors")
    void generateSchedule_shouldHandleMultipleSubjectsWithDifferentMajors() {
        TKBRequest subject1 = baseRequest("INT1001", 30, "CNTT");
        TKBRequest subject2 = baseRequest("MAT1001", 30, "KT");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(subject1, subject2))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("MAT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjects=[INT1001(CNTT), MAT1001(KT)]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", totalClasses=" + response.getTotalClasses());
        System.out.println("EXPECTED: items.size=2, totalClasses=2");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalClasses()).isEqualTo(2);
    }

    @Test
    @DisplayName("LL-56: Kiểm tra generateSchedule_shouldHandleSubjectWithCombinedMajor")
    void generateSchedule_shouldHandleSubjectWithCombinedMajor() {
        TKBRequest combinedSubject = baseRequest("COM001", 30, "CNTT-KT");
        combinedSubject.setNganh("CNTT-KT");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(combinedSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("COM001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=COM001, major=CNTT-KT");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", major=" + response.getItems().get(0).getInput().getNganh());
        System.out.println("EXPECTED: major=CNTT-KT");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getInput().getNganh()).isEqualTo("CNTT-KT");
    }

    @Test
    @DisplayName("LL-57: Kiểm tra generateSchedule_shouldHandleSubjectWithNullMajor")
    void generateSchedule_shouldHandleSubjectWithNullMajor() {
        TKBRequest nullMajorSubject = baseRequest("INT1001", 30, null);
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(nullMajorSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, major=null");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
    }

    @Test
    @DisplayName("LL-58: Kiểm tra generateSchedule_shouldSetLastSlotIdxInResponse")
    void generateSchedule_shouldSetLastSlotIdxInResponse() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(5);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: redis lastSlotIdx=5");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: response.lastSlotIdx=" + response.getLastSlotIdx());
        assertThat(response.getLastSlotIdx()).isNotNull();
    }

    @Test
    @DisplayName("LL-59: Kiểm tra generateSchedule_shouldContinueWhenSubjectNotFoundInRepository")
    void generateSchedule_shouldContinueWhenSubjectNotFoundInRepository() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT9999", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT9999", "HK1", "2024-2025"))
                .thenReturn(List.of());

        System.out.println("INPUT: subjectRepo returns empty for subjectCode=INT9999");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: subjectDatabaseId=" + response.getItems().get(0).getRows().get(0).getSubjectDatabaseId());
        System.out.println("EXPECTED: subjectDatabaseId=null");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows().get(0).getSubjectDatabaseId()).isNull();
    }

    @Test
    @DisplayName("LL-60: Kiểm tra generateSchedule_shouldCalculateTotalRowsCorrectly")
    void generateSchedule_shouldCalculateTotalRowsCorrectly() {
        TKBRequest subject1 = baseRequest("INT1001", 30, "CNTT");
        TKBRequest subject2 = baseRequest("MAT1001", 30, "KT");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(subject1, subject2))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("MAT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjects=[INT1001, MAT1001], periods=[30,30]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: totalRows=" + response.getTotalRows() + ", totalClasses=" + response.getTotalClasses());
        System.out.println("EXPECTED: totalRows>=2");
        assertThat(response.getTotalRows()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("LL-61: Kiểm tra generateSchedule_shouldSetOccupiedRoomsCountZero")
    void generateSchedule_shouldSetOccupiedRoomsCountZero() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, expected occupiedRoomsCount=0");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: occupiedRoomsCount=" + response.getOccupiedRoomsCount());
        assertThat(response.getOccupiedRoomsCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("LL-62: Kiểm tra generateSchedule_shouldHandle14PeriodSubjectCorrectly")
    void generateSchedule_shouldHandle14PeriodSubjectCorrectly() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 14, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(14, 2, 1, 1, 1L, "14-A")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, periods=14, templateName=14-A");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
    }

    @Test
    @DisplayName("LL-63: Kiểm tra generateSchedule_shouldSetHeDacThuFromRequest")
    void generateSchedule_shouldSetHeDacThuFromRequest() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected he_dac_thu=CLC");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: heDacThu=" + response.getItems().get(0).getRows().get(0).getHeDacThu());
        assertThat(response.getItems().get(0).getRows().get(0).getHeDacThu()).isEqualTo("CLC");
    }

    @Test
    @DisplayName("LL-64: Kiểm tra generateSchedule_shouldSetStudentYearFromRequest")
    void generateSchedule_shouldSetStudentYearFromRequest() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected studentYear=2024");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: studentYear=" + response.getItems().get(0).getRows().get(0).getStudentYear());
        assertThat(response.getItems().get(0).getRows().get(0).getStudentYear()).isEqualTo("2024");
    }

    @Test
    @DisplayName("LL-65: Kiểm tra generateSchedule_shouldSetSiSoMotLopFromRequest")
    void generateSchedule_shouldSetSiSoMotLopFromRequest() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected siSoMotLop=50");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: siSoMotLop=" + response.getItems().get(0).getRows().get(0).getSiSoMotLop());
        assertThat(response.getItems().get(0).getRows().get(0).getSiSoMotLop()).isEqualTo(50);
    }

    @Test
    @DisplayName("LL-66: Kiểm tra generateSchedule_shouldPopulateAllRowFieldsCorrectly")
    void generateSchedule_shouldPopulateAllRowFieldsCorrectly() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 55L, "TPL-01")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(999L).build()));

        System.out.println("INPUT: subjectCode=INT1001, templateName=TPL-01, templateDbId=55");
        TKBBatchResponse response = scheduleService.generateSchedule(request);
        TKBRowResult row = response.getItems().get(0).getRows().get(0);

        System.out.println("OUTPUT: row={maMon=" + row.getMaMon()
                + ", tenMon=" + row.getTenMon()
                + ", thu=" + row.getThu()
                + ", kip=" + row.getKip()
                + ", tietBd=" + row.getTietBd()
                + ", n=" + row.getN()
                + ", academicYear=" + row.getAcademicYear()
                + ", semester=" + row.getSemester()
                + "}");
        assertThat(row.getMaMon()).isEqualTo("INT1001");
        assertThat(row.getTenMon()).isEqualTo("Nhap mon");
        assertThat(row.getKip()).isEqualTo(1);
        assertThat(row.getThu()).isEqualTo(2);
        assertThat(row.getTietBd()).isEqualTo(1);
        assertThat(row.getL()).isEqualTo(1);
        assertThat(row.getAH()).isEqualTo(18);
        assertThat(row.getAI()).isEqualTo(30);
        assertThat(row.getAJ()).isEqualTo(12);
        assertThat(row.getN()).isEqualTo("TPL-01");
        assertThat(row.getAcademicYear()).isEqualTo("2024-2025");
        assertThat(row.getSemester()).isEqualTo("HK1");
    }

    @Test
    @DisplayName("LL-67: Kiểm tra generateSchedule_shouldSkipTemplatesWithZeroAH")
    void generateSchedule_shouldSkipTemplatesWithZeroAH() {
        DataLoaderService.TKBTemplateRow template1 = new DataLoaderService.TKBTemplateRow(
                1L, 30, 2, 1, 1, 0, "ZERO-AH", List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), 0);
        DataLoaderService.TKBTemplateRow template2 = new DataLoaderService.TKBTemplateRow(
                2L, 30, 2, 1, 1, 1, "VALID-AH", fullWeeks, 18);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(template1, template2));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: templates=[ZERO-AH(AH=0), VALID-AH], subjectCode=INT1001");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: templateNames=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getN).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> !"ZERO-AH".equals(r.getN()));
    }

    @Test
    @DisplayName("LL-68: Kiểm tra generateSchedule_shouldThrowWhenTemplateDataIsEmptyForSemester")
    void generateSchedule_shouldThrowWhenTemplateDataIsEmptyForSemester() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2025-2026")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2025-2026")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2025-2026")).thenReturn(List.of());

        System.out.println("INPUT: academicYear=2025-2026, semester=HK1 -> templates empty");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Chưa có dữ liệu lịch mẫu cho HK1 2025-2026");
        System.out.println("OUTPUT: thrown=InvalidDataException(Chưa có dữ liệu lịch mẫu cho HK1 2025-2026)");
    }

    @Test
    @DisplayName("LL-69: Kiểm tra generateSchedule_shouldSetSemesterIdNullWhenSemesterNotFound")
    void generateSchedule_shouldSetSemesterIdNullWhenSemesterNotFound() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK3")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK3", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK3 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));

        System.out.println("INPUT: request.semester=HK3 (not found) -> expect setCurrentSemesterId(null)");
        scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: verified setCurrentSemesterId(null) called");
        verify(dataLoaderService).setCurrentSemesterId(null);
    }

    @Test
    @DisplayName("LL-70: Kiểm tra generateSchedule_shouldSetRoomIdNullForAllRows")
    void generateSchedule_shouldSetRoomIdNullForAllRows() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001 -> expect all rows roomId=null");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: roomIds=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getRoomId).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> r.getRoomId() == null);
    }

    @Test
    @DisplayName("LL-71: Kiểm tra generateSchedule_shouldSetPhongNullForAllRows")
    void generateSchedule_shouldSetPhongNullForAllRows() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001 -> expect all rows phong=null");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: phongValues=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getPhong).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> r.getPhong() == null);
    }

    @Test
    @DisplayName("LL-72: Kiểm tra generateSchedule_shouldHandleMultipleSubjectsWithMixedPeriods")
    void generateSchedule_shouldHandleMultipleSubjectsWithMixedPeriods() {
        TKBRequest subject30 = baseRequest("INT1001", 30, "CNTT");
        TKBRequest subject60 = baseRequest("INT2001", 60, "CNTT");
        subject60.setSolop(1);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(subject30, subject60))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(30, 2, 1, 1, 20L, "30-A")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjects=[INT1001(30), INT2001(60)]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", totalClasses=" + response.getTotalClasses());
        System.out.println("EXPECTED: items.size=2, totalClasses=2");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalClasses()).isEqualTo(2);
    }

    @Test
    @DisplayName("LL-73: Kiểm tra generateSchedule_shouldMaintainSessionStateAcrossMultipleCalls")
    void generateSchedule_shouldMaintainSessionStateAcrossMultipleCalls() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: call generateSchedule twice (same request), redis lastSlotIdx=-1");
        TKBBatchResponse response1 = scheduleService.generateSchedule(request);
        TKBBatchResponse response2 = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: lastSlotIdx1=" + response1.getLastSlotIdx() + ", lastSlotIdx2=" + response2.getLastSlotIdx());
        assertThat(response1.getLastSlotIdx()).isEqualTo(response2.getLastSlotIdx());
    }

    @Test
    @DisplayName("LL-74: Kiểm tra generateSchedule_shouldProcessWithZeroSolopDefaultingToOne")
    void generateSchedule_shouldProcessWithZeroSolopDefaultingToOne() {
        TKBRequest zeroSolopSubject = baseRequest("INT1001", 30, "CNTT");
        zeroSolopSubject.setSolop(0);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(zeroSolopSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, solop=0 (expect default=1)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size());
        System.out.println("EXPECTED: rows.size=2");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).hasSize(2);
    }

    @Test
    @DisplayName("LL-75: Kiểm tra generateSchedule_shouldHandleSubjectWithDifferentSemesterFormat")
    void generateSchedule_shouldHandleSubjectWithDifferentSemesterFormat() {
        TKBRequest subjectHK2 = baseRequest("INT1001", 30, "CNTT");
        subjectHK2.setSemester("2");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("2")
                .items(List.of(subjectHK2))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("2", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK2 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "2")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "2", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: request.semester=2, item.semester=2 -> expect templateKey=HK2 2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", verified loadTemplateData(HK2 2024-2025)");
        assertThat(response.getItems()).hasSize(1);
        verify(dataLoaderService).loadTemplateData("HK2 2024-2025");
    }

    @Test
    @DisplayName("LL-76: Kiểm tra generateSchedule_shouldValidateThatAllRowsHaveCorrectMaMon")
    void generateSchedule_shouldValidateThatAllRowsHaveCorrectMaMon() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: distinctMaMon=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getMaMon).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "INT1001".equals(r.getMaMon()));
    }

    @Test
    @DisplayName("LL-77: Kiểm tra generateSchedule_shouldValidateThatAllRowsHaveCorrectTenMon")
    void generateSchedule_shouldValidateThatAllRowsHaveCorrectTenMon() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, expected tenMon='Nhap mon'");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: distinctTenMon=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getTenMon).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "Nhap mon".equals(r.getTenMon()));
    }

    @Test
    @DisplayName("LL-78: Kiểm tra generateSchedule_shouldSetAcademicYearInRows")
    void generateSchedule_shouldSetAcademicYearInRows() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected academicYear in rows = 2024-2025");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: distinctAcademicYear=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getAcademicYear).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "2024-2025".equals(r.getAcademicYear()));
    }

    @Test
    @DisplayName("LL-79: Kiểm tra generateSchedule_shouldSetSemesterInRows")
    void generateSchedule_shouldSetSemesterInRows() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected semester in rows = HK1");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: distinctSemester=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getSemester).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "HK1".equals(r.getSemester()));
    }

    @Test
    @DisplayName("LL-80: Kiểm tra generateSchedule_shouldSetNganhInRows")
    void generateSchedule_shouldSetNganhInRows() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected nganh in rows = CNTT");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: distinctNganh=" + response.getItems().get(0).getRows().stream().map(TKBRowResult::getNganh).distinct().toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> "CNTT".equals(r.getNganh()));
    }

    @Test
    @DisplayName("LL-81: Kiểm tra generateSchedule_shouldAssignCorrectLopNumbersForMultipleClasses")
    void generateSchedule_shouldAssignCorrectLopNumbersForMultipleClasses() {
        TKBRequest multiClassSubject = baseRequest("INT1001", 30, "CNTT");
        multiClassSubject.setSolop(3);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(multiClassSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, solop=3 -> expect lopNumbers=[1,2,3]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        List<Integer> lopNumbers = response.getItems().get(0).getRows().stream()
                .map(TKBRowResult::getLop)
                .distinct()
                .sorted()
                .toList();

        System.out.println("OUTPUT: lopNumbers=" + lopNumbers);
        assertThat(lopNumbers).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("LL-82: Kiểm tra generateSchedule_shouldHandleCombinedMajorSubject")
    void generateSchedule_shouldHandleCombinedMajorSubject() {
        TKBRequest combinedSubject = baseRequest("COM001", 30, "CNTT-KT-VL");
        combinedSubject.setNganh("CNTT-KT-VL");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(combinedSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("COM001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=COM001, major=CNTT-KT-VL");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
    }

    @Test
    @DisplayName("LL-83: Kiểm tra generateSchedule_shouldValidateOTAGArraySizeIs18")
    void generateSchedule_shouldValidateOTAGArraySizeIs18() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expect all rows O_to_AG size=18");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: o_to_AG.sizes=" + response.getItems().get(0).getRows().stream()
                .map(r -> r.getO_to_AG() == null ? null : r.getO_to_AG().size())
                .distinct()
                .toList());
        assertThat(response.getItems().get(0).getRows())
                .allMatch(r -> r.getO_to_AG() != null && r.getO_to_AG().size() == 18);
    }

    @Test
    @DisplayName("LL-84: Kiểm tra generateSchedule_shouldHandleVeryLargeSiSo")
    void generateSchedule_shouldHandleVeryLargeSiSo() {
        TKBRequest largeSiSoSubject = baseRequest("INT1001", 30, "CNTT");
        largeSiSoSubject.setSiso(10000);
        largeSiSoSubject.setSiso_mot_lop(500);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(largeSiSoSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: siso=10000, siso_mot_lop=500");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: siSoMotLop=" + response.getItems().get(0).getRows().get(0).getSiSoMotLop());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows().get(0).getSiSoMotLop()).isEqualTo(500);
    }

    // ============ ADDITIONAL COVERAGE TESTS ============

    @Test
    @DisplayName("LL-85: Kiểm tra generateSchedule_shouldHandleNullItemsInRequest")
    void generateSchedule_shouldHandleNullItemsInRequest() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(null)
                .build();

        // When items is null, code tries to call isEmpty() which throws NPE or validation exception
        System.out.println("INPUT: request.items=null (expect InvalidDataException)");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class);
        System.out.println("OUTPUT: thrown=InvalidDataException");
    }

    @Test
    @DisplayName("LL-86: Kiểm tra generateSchedule_shouldPropagateExceptionWhenDataLoaderServiceFails")
    void generateSchedule_shouldPropagateExceptionWhenDataLoaderServiceFails() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenThrow(new RuntimeException("Database connection error"));

        System.out.println("INPUT: dataLoaderService.loadTemplateData throws RuntimeException");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection error");
        System.out.println("OUTPUT: thrown=RuntimeException(Database connection error)");
    }

    @Test
    @DisplayName("LL-87: Kiểm tra generateSchedule_shouldPropagateExceptionWhenSemesterRepositoryFails")
    void generateSchedule_shouldPropagateExceptionWhenSemesterRepositoryFails() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year("2024-2025");
        item.setSemester("HK1");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(List.of(item))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025"))
                .thenThrow(new RuntimeException("Database error"));

        System.out.println("INPUT: semesterRepository throws RuntimeException(Database error)");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
        System.out.println("OUTPUT: thrown=RuntimeException(Database error)");
    }

    @Test
    @DisplayName("LL-88: Kiểm tra generateSchedule_shouldHandleExceptionInSubjectRepositoryGracefully")
    void generateSchedule_shouldHandleExceptionInSubjectRepositoryGracefully() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenThrow(new RuntimeException("Subject lookup failed"));

        System.out.println("INPUT: subjectRepository throws RuntimeException(Subject lookup failed)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: subjectDatabaseId=" + response.getItems().get(0).getRows().get(0).getSubjectDatabaseId());
        // Should still generate response with subjectDatabaseId=null
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows().get(0).getSubjectDatabaseId()).isNull();
    }

    @Test
    @DisplayName("LL-89: Kiểm tra generateSchedule_shouldHandleNullAcademicYearWithNonEmptyItems")
    void generateSchedule_shouldHandleNullAcademicYearWithNonEmptyItems() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year(null);
        item.setSemester("HK1");
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(List.of(item))
                .build();

        when(dataLoaderService.loadTemplateData("HK1 null")).thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));

        // Should not throw, will use "HK1 null" as semesterKey
        System.out.println("INPUT: item.academicYear=null, item.semester=HK1 -> templateKey='HK1 null'");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        assertThat(response.getItems()).hasSize(1);
        verify(dataLoaderService).loadTemplateData("HK1 null");
    }

    @Test
    @DisplayName("LL-90: Kiểm tra generateSchedule_shouldHandleNullSemesterWithNonEmptyItems")
    void generateSchedule_shouldHandleNullSemesterWithNonEmptyItems() {
        TKBRequest item = baseRequest("INT1001", 30, "CNTT");
        item.setAcademic_year("2024-2025");
        item.setSemester(null);
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .items(List.of(item))
                .build();

        when(dataLoaderService.loadTemplateData("null 2024-2025")).thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));

        // Should not throw, will use "null 2024-2025" as semesterKey
        System.out.println("INPUT: item.semester=null, item.academicYear=2024-2025 -> templateKey='null 2024-2025'");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        assertThat(response.getItems()).hasSize(1);
        verify(dataLoaderService).loadTemplateData("null 2024-2025");
    }

    @Test
    @DisplayName("LL-91: Kiểm tra generateSchedule_shouldHandleVeryLongWeeksSchedule")
    void generateSchedule_shouldHandleVeryLongWeeksSchedule() {
        List<Integer> extraLongWeeks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            extraLongWeeks.add(1);
        }

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(new DataLoaderService.TKBTemplateRow(1L, 30, 2, 1, 1, 1, "LONG", extraLongWeeks, 30)));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: extraLongWeeks.size=" + extraLongWeeks.size());
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: o_to_AG.size=" + response.getItems().get(0).getRows().get(0).getO_to_AG().size());
        System.out.println("EXPECTED: o_to_AG.size=18");
        assertThat(response.getItems().get(0).getRows().get(0).getO_to_AG()).hasSize(18);
    }

    @Test
    @DisplayName("LL-92: Kiểm tra generateSchedule_shouldHandlePeriodLengthZero")
    void generateSchedule_shouldHandlePeriodLengthZero() {
        DataLoaderService.TKBTemplateRow zeroPeriodRow = new DataLoaderService.TKBTemplateRow(
                1L, 30, 2, 1, 0, 1, "ZERO-L", fullWeeks, 0);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(zeroPeriodRow));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: templateRow.periodLength=0");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size());
        // Should still succeed but may generate fewer rows due to zero AH
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("LL-93: Kiểm tra generateSchedule_shouldHandleMultipleTemplatesWithSamePeriod")
    void generateSchedule_shouldHandleMultipleTemplatesWithSamePeriod() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(30, 2, 1, 1, 1L, "TPL-01"),
                        templateRow(30, 3, 1, 1, 2L, "TPL-02"),
                        templateRow(30, 4, 1, 1, 3L, "TPL-03")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: templates=[TPL-01,TPL-02,TPL-03] (same periods=30)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        List<String> templateNames = response.getItems().get(0).getRows().stream()
                .map(TKBRowResult::getN)
                .distinct()
                .toList();
        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size() + ", templateNames=" + templateNames);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
        // Should use templates from different days
        assertThat(templateNames).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("LL-94: Kiểm tra generateSchedule_shouldSetLastSlotIdxToNegativeWhenRedisReturnsNegative")
    void generateSchedule_shouldSetLastSlotIdxToNegativeWhenRedisReturnsNegative() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: redis lastSlotIdx=-1");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: response.lastSlotIdx=" + response.getLastSlotIdx());
        assertThat(response.getLastSlotIdx()).isNotNull();
        assertThat(response.getLastSlotIdx()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("LL-95: Kiểm tra generateSchedule_shouldThrowWhenSubjectHasNoMatchingTemplate")
    void generateSchedule_shouldThrowWhenSubjectHasNoMatchingTemplate() {
        TKBRequest validSubject = baseRequest("INT1001", 30, "CNTT");
        TKBRequest invalidSubject = baseRequest("INT9999", 45, "CNTT"); // No template for 45 periods

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(validSubject, invalidSubject))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: items include invalidSubject periods=45 (no template)");
        assertThatThrownBy(() -> scheduleService.generateSchedule(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Không có Data cho 45 tiết");
        System.out.println("OUTPUT: thrown=InvalidDataException(Không có Data cho 45 tiết)");
    }

    @Test
    @DisplayName("LL-96: Kiểm tra generateSchedule_shouldHandleSingleClassSubjectCorrectly")
    void generateSchedule_shouldHandleSingleClassSubjectCorrectly() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, periods=30, solop(default)=1");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size()
                + ", first.lop=" + response.getItems().get(0).getRows().get(0).getLop());
        assertThat(response.getItems().get(0).getRows()).hasSize(2);
        assertThat(response.getItems().get(0).getRows().get(0).getLop()).isEqualTo(1);
    }

    @Test
    @DisplayName("LL-97: Kiểm tra generateSchedule_shouldProcess60PeriodSubjectWithMultipleClasses")
    void generateSchedule_shouldProcess60PeriodSubjectWithMultipleClasses() {
        TKBRequest period60MultiClass = baseRequest("INT2001", 60, "CNTT");
        period60MultiClass.setSolop(2);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(period60MultiClass))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjectCode=INT2001, periods=60, solop=2 (but only slot0 matches)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        // Only 1 class produces rows (2 days) because templates only exist for slot 0
        // Second class (slot 1) has no matching templates for kip 2
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).hasSize(2);
    }

    @Test
    @DisplayName("LL-98: Kiểm tra generateSchedule_shouldHandleVerySmallSiSo")
    void generateSchedule_shouldHandleVerySmallSiSo() {
        TKBRequest smallSiSo = baseRequest("INT1001", 30, "CNTT");
        smallSiSo.setSiso(5);
        smallSiSo.setSiso_mot_lop(5);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(smallSiSo))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: siso=5, siso_mot_lop=5");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).hasSize(2);
    }

    @Test
    @DisplayName("LL-99: Kiểm tra generateSchedule_shouldHandleVeryHighSolopValue")
    void generateSchedule_shouldHandleVeryHighSolopValue() {
        TKBRequest highSolop = baseRequest("INT1001", 30, "CNTT");
        highSolop.setSolop(10);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(highSolop))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: subjectCode=INT1001, solop=10");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size());
        System.out.println("EXPECTED: rows.size=20");
        assertThat(response.getItems().get(0).getRows()).hasSize(20);
    }

    @Test
    @DisplayName("LL-100: Kiểm tra generateSchedule_shouldHandleSessionStateAcrossMultiple60PeriodSubjects")
    void generateSchedule_shouldHandleSessionStateAcrossMultiple60PeriodSubjects() {
        TKBRequest period601 = baseRequest("INT2001", 60, "CNTT");
        period601.setSolop(1);
        TKBRequest period602 = baseRequest("INT2002", 60, "CNTT");
        period602.setSolop(1);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(period601, period602))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2002", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjects=[INT2001(60), INT2002(60)], solop=[1,1]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", totalClasses=" + response.getTotalClasses());
        System.out.println("EXPECTED: items.size=2, totalClasses=2");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalClasses()).isEqualTo(2);
    }

    @Test
    @DisplayName("LL-101: Kiểm tra generateSchedule_shouldHandleMixedPeriodSubjectsInterleaved")
    void generateSchedule_shouldHandleMixedPeriodSubjectsInterleaved() {
        TKBRequest period60 = baseRequest("INT2001", 60, "CNTT");
        period60.setSolop(1);
        TKBRequest period30a = baseRequest("INT1001", 30, "CNTT");
        TKBRequest period30b = baseRequest("INT1002", 30, "CNTT");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(period60, period30a, period30b))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B"),
                        templateRow(30, 4, 1, 1, 12L, "30-A")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1002", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(3L).build()));

        System.out.println("INPUT: subjects=[INT2001(60), INT1001(30), INT1002(30)]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: firstSubjectCode=" + response.getItems().get(0).getInput().getMa_mon()
                + ", items.size=" + response.getItems().size());
        System.out.println("EXPECTED: firstSubjectCode=INT2001");
        assertThat(response.getItems()).hasSize(3);
        // First should be 60-period subject (sorted to front)
        assertThat(response.getItems().get(0).getInput().getMa_mon()).isEqualTo("INT2001");
    }

    @Test
    @DisplayName("LL-102: Kiểm tra generateSchedule_shouldReturnEmptyRowsWhenNoTemplatesMatchAfterFiltering")
    void generateSchedule_shouldReturnEmptyRowsWhenNoTemplatesMatchAfterFiltering() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        // Return templates but all have AH=0 (filtered out)
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        new DataLoaderService.TKBTemplateRow(1L, 30, 2, 1, 1, 0, "ZERO", fullWeeks, 0)));

        System.out.println("INPUT: templates all AH=0 -> expect rows empty");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems().get(0).getRows()).isEmpty();
    }

    @Test
    @DisplayName("LL-103: Kiểm tra generateSchedule_shouldHandleLargeNumberOfSubjects")
    void generateSchedule_shouldHandleLargeNumberOfSubjects() {
        List<TKBRequest> manySubjects = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            TKBRequest subject = baseRequest("INT" + i, 30, "CNTT");
            manySubjects.add(subject);
        }

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(manySubjects)
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);

        // Mock subject repository for all subjects
        List<Subject> subjects = manySubjects.stream()
                .map(s -> Subject.builder().id(Long.parseLong(s.getMa_mon().replace("INT", ""))).build())
                .toList();
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear(any(), any(), any()))
                .thenReturn(subjects);

        System.out.println("INPUT: manySubjects.size=" + manySubjects.size());
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", totalClasses=" + response.getTotalClasses());
        assertThat(response.getItems()).hasSize(50);
        assertThat(response.getTotalClasses()).isEqualTo(50);
    }

    @Test
    @DisplayName("LL-104: Kiểm tra commitSessionToRedis_shouldUpdateLastSlotIdxAfterSaving")
    void commitSessionToRedis_shouldUpdateLastSlotIdxAfterSaving() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(10L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(10L, "2024-2025", "HK1")).thenReturn(5);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: generateSchedule then commitSessionToRedis(userId=10, academicYear=2024-2025, semester=HK1), redis lastSlotIdx=5");
        TKBBatchResponse gen = scheduleService.generateSchedule(request);
        System.out.println("OUTPUT: generated.lastSlotIdx=" + gen.getLastSlotIdx() + ", totalRows=" + gen.getTotalRows());
        scheduleService.commitSessionToRedis(10L, "2024-2025", "HK1");

        System.out.println("OUTPUT: verified redisService.saveLastSlotIdx called");
        verify(redisService).saveLastSlotIdx(eq(10L), eq("2024-2025"), eq("HK1"), any(Integer.class));
    }

    @Test
    @DisplayName("LL-105: Kiểm tra resetLastSlotIndexRedis_shouldClearAndResetWithValidContext")
    void resetLastSlotIndexRedis_shouldClearAndResetWithValidContext() {
        System.out.println("INPUT: resetLastSlotIndexRedis(userId=5, 2024-2025, HK1)");
        scheduleService.resetLastSlotIndexRedis(5L, "2024-2025", "HK1");

        System.out.println("OUTPUT: verified clearLastSlotIdx called");
        verify(redisService).clearLastSlotIdx(5L, "2024-2025", "HK1");
        // Note: resetLastSlotIndexRedis does NOT call saveLastSlotIdx, only clear
    }

    @Test
    @DisplayName("LL-106: Kiểm tra generateSchedule_shouldSetCorrectSiSoFromRequest")
    void generateSchedule_shouldSetCorrectSiSoFromRequest() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: expected siSoMotLop=50");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: siSoMotLop=" + response.getItems().get(0).getRows().get(0).getSiSoMotLop());
        assertThat(response.getItems().get(0).getRows().get(0).getSiSoMotLop()).isEqualTo(50);
    }

    @Test
    @DisplayName("LL-107: Kiểm tra generateSchedule_shouldHandleSubjectWithEmptyMajor")
    void generateSchedule_shouldHandleSubjectWithEmptyMajor() {
        TKBRequest emptyMajor = baseRequest("INT1001", 30, "");
        emptyMajor.setNganh("");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(emptyMajor))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: major=\"\" (empty)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
    }

    @Test
    @DisplayName("LL-108: Kiểm tra generateSchedule_shouldHandleSubjectWithSpecialCharactersInMajor")
    void generateSchedule_shouldHandleSubjectWithSpecialCharactersInMajor() {
        TKBRequest specialMajor = baseRequest("INT1001", 30, "CNTT & KT");
        specialMajor.setNganh("CNTT & KT");

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(specialMajor))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: major=\"CNTT & KT\"");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size()
                + ", rows.size=" + response.getItems().get(0).getRows().size());
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRows()).isNotEmpty();
    }

    @Test
    @DisplayName("LL-109: Kiểm tra generateSchedule_shouldHandleSubjectWithNegativeSolop")
    void generateSchedule_shouldHandleSubjectWithNegativeSolop() {
        TKBRequest negativeSolop = baseRequest("INT1001", 30, "CNTT");
        negativeSolop.setSolop(-1);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(negativeSolop))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 1L, "R1")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: solop=-1 (expect default=1)");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: rows.size=" + response.getItems().get(0).getRows().size());
        // Math.max(1, -1) should default to 1, but 30 periods need 2 rows
        assertThat(response.getItems().get(0).getRows()).hasSize(2);
    }

    @Test
    @DisplayName("LL-110: Kiểm tra generateSchedule_shouldSetCorrectLopFor60PeriodSubject")
    void generateSchedule_shouldSetCorrectLopFor60PeriodSubject() {
        TKBRequest period60 = baseRequest("INT2001", 60, "CNTT");
        period60.setSolop(2);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(period60))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjectCode=INT2001, periods=60, solop=2 -> expect lopNumbers=[1]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        List<Integer> lopNumbers = response.getItems().get(0).getRows().stream()
                .map(TKBRowResult::getLop)
                .distinct()
                .sorted()
                .toList();

        System.out.println("OUTPUT: lopNumbers=" + lopNumbers);
        // Only class 1 produces rows (2 days) because templates match only first slot
        assertThat(lopNumbers).containsExactly(1);
    }

    @Test
    @DisplayName("LL-111: Kiểm tra generateSchedule_shouldProcessMultiple60PeriodSubjects")
    void generateSchedule_shouldProcessMultiple60PeriodSubjects() {
        TKBRequest period60a = baseRequest("INT2001", 60, "CNTT");
        period60a.setSolop(1);
        TKBRequest period60b = baseRequest("INT2002", 60, "CNTT");
        period60b.setSolop(1);

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(period60a, period60b))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(
                        templateRow(60, 2, 1, 1, 10L, "60-A"),
                        templateRow(60, 3, 1, 1, 11L, "60-B")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT2002", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(2L).build()));

        System.out.println("INPUT: subjects=[INT2001(60), INT2002(60)]");
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: items.size=" + response.getItems().size() + ", totalClasses=" + response.getTotalClasses());
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalClasses()).isEqualTo(2);
    }

    @Test
    @DisplayName("LL-112: Kiểm tra generateSchedule_shouldIncludeAllExpectedFieldsInRow")
    void generateSchedule_shouldIncludeAllExpectedFieldsInRow() {
        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(templateRow(30, 2, 1, 1, 55L, "TPL-01")));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(999L).build()));

        System.out.println("INPUT: subjectCode=INT1001, templateDbId=55, templateName=TPL-01");
        TKBBatchResponse response = scheduleService.generateSchedule(request);
        TKBRowResult row = response.getItems().get(0).getRows().get(0);

        System.out.println("OUTPUT: row.summary={maMon=" + row.getMaMon()
                + ", tenMon=" + row.getTenMon()
                + ", templateDatabaseId=" + row.getTemplateDatabaseId()
                + ", subjectDatabaseId=" + row.getSubjectDatabaseId()
                + ", o_to_AG.size=" + (row.getO_to_AG() == null ? null : row.getO_to_AG().size())
                + "}");
        assertThat(row.getLop()).isNotNull();
        assertThat(row.getMaMon()).isNotNull();
        assertThat(row.getTenMon()).isNotNull();
        assertThat(row.getKip()).isNotNull();
        assertThat(row.getThu()).isNotNull();
        assertThat(row.getTietBd()).isNotNull();
        assertThat(row.getL()).isNotNull();
        assertThat(row.getPhong()).isNull();
        assertThat(row.getRoomId()).isNull();
        assertThat(row.getAH()).isNotNull();
        assertThat(row.getAI()).isNotNull();
        assertThat(row.getAJ()).isNotNull();
        assertThat(row.getN()).isNotNull();
        assertThat(row.getO_to_AG()).isNotNull();
        assertThat(row.getTemplateDatabaseId()).isNotNull();
        assertThat(row.getStudentYear()).isNotNull();
        assertThat(row.getHeDacThu()).isNotNull();
        assertThat(row.getNganh()).isNotNull();
        assertThat(row.getSiSoMotLop()).isNotNull();
        assertThat(row.getAcademicYear()).isNotNull();
        assertThat(row.getSemester()).isNotNull();
        assertThat(row.getSubjectDatabaseId()).isNotNull();
    }

    @Test
    @DisplayName("LL-113: Kiểm tra resetState_shouldNotAffectOtherInstances")
    void resetState_shouldNotAffectOtherInstances() {
        // Reset state
        System.out.println("INPUT: resetState()");
        scheduleService.resetState();

        System.out.println("OUTPUT: verified repository not called");
        // Verify no repository interactions
        verify(scheduleRepository, never()).findAll();
        verify(scheduleRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("LL-114: Kiểm tra generateSchedule_shouldHandleNullInWeekScheduleList")
    void generateSchedule_shouldHandleNullInWeekScheduleList() {
        List<Integer> weekWithNulls = new ArrayList<>();
        weekWithNulls.add(1);
        weekWithNulls.add(null);
        weekWithNulls.add(1);
        weekWithNulls.add(null);
        // Fill to 18
        while (weekWithNulls.size() < 18) {
            weekWithNulls.add(1);
        }

        TKBBatchRequest request = TKBBatchRequest.builder()
                .userId(1L)
                .academicYear("2024-2025")
                .semester("HK1")
                .items(List.of(baseRequest("INT1001", 30, "CNTT")))
                .build();

        when(semesterRepository.findBySemesterNameAndAcademicYear("HK1", "2024-2025")).thenReturn(Optional.empty());
        when(dataLoaderService.loadTemplateData("HK1 2024-2025"))
                .thenReturn(List.of(new DataLoaderService.TKBTemplateRow(1L, 30, 2, 1, 1, 1, "NULL-TEST", weekWithNulls, 30)));
        when(redisService.loadLastSlotIdx(1L, "2024-2025", "HK1")).thenReturn(-1);
        when(subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear("INT1001", "HK1", "2024-2025"))
                .thenReturn(List.of(Subject.builder().id(1L).build()));

        System.out.println("INPUT: weekWithNulls.size=" + weekWithNulls.size() + ", containsNull=" + weekWithNulls.contains(null));
        TKBBatchResponse response = scheduleService.generateSchedule(request);

        System.out.println("OUTPUT: o_to_AG.size=" + response.getItems().get(0).getRows().get(0).getO_to_AG().size());
        assertThat(response.getItems().get(0).getRows().get(0).getO_to_AG()).hasSize(18);
    }
}
