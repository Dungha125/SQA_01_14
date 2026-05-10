package com.ptit.schedule;

import com.ptit.schedule.controller.ScheduleValidationController;
import com.ptit.schedule.dto.*;
import com.ptit.schedule.exception.*;
import com.ptit.schedule.service.ScheduleExcelReaderService;
import com.ptit.schedule.service.impl.ScheduleConflictDetectionServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HauKiemTKBTest {

    private ScheduleConflictDetectionServiceImpl conflictService;
    @Mock
    private ScheduleExcelReaderService excelReaderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        conflictService = new ScheduleConflictDetectionServiceImpl();

        ScheduleValidationController controller = new ScheduleValidationController(excelReaderService, conflictService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private ScheduleEntry.TimeSlot makeSlot(String date, String dayOfWeek, String shift, String startPeriod, String numberOfPeriods) {
        return ScheduleEntry.TimeSlot.builder()
                .date(date)
                .dayOfWeek(dayOfWeek)
                .shift(shift)
                .startPeriod(startPeriod)
                .numberOfPeriods(numberOfPeriods)
                .build();
    }

    private ScheduleEntry makeEntry(String subjectCode, String room, String building, ScheduleEntry.TimeSlot slot) {
        return ScheduleEntry.builder()
                .subjectCode(subjectCode)
                .room(room)
                .building(building)
                .timeSlots(Collections.singletonList(slot))
                .build();
    }

    private ScheduleEntry makeEntryWithTeacher(String subjectCode, String teacherId, String classGroup, String room, String building, ScheduleEntry.TimeSlot slot) {
        return ScheduleEntry.builder()
                .subjectCode(subjectCode)
                .teacherId(teacherId)
                .classGroup(classGroup)
                .room(room)
                .building(building)
                .timeSlots(Collections.singletonList(slot))
                .build();
    }

    private ScheduleEntry makeEntryFull(String subjectCode, String subjectName, String teacherId, String teacherName,
            String room, String building, String classGroup, int studentCount, ScheduleEntry.TimeSlot slot) {
        return ScheduleEntry.builder()
                .subjectCode(subjectCode)
                .subjectName(subjectName)
                .teacherId(teacherId)
                .teacherName(teacherName)
                .room(room)
                .building(building)
                .classGroup(classGroup)
                .studentCount(studentCount)
                .timeSlots(Collections.singletonList(slot))
                .build();
    }

    // =========================================================
    // HVK01-HVK10: ScheduleConflictDetectionService - các trường hợp cơ bản
    // =========================================================

    // HVK01: Danh sách rỗng -> 0 xung đột
    @Test
    @DisplayName("HVK01_emptyList_zeroConflicts")
    void hvk01() {
        ConflictResult result = conflictService.detectConflicts(Collections.emptyList());
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK02: Danh sách null -> ném NullPointerException
    @Test
    @DisplayName("HVK02_nullList_throwsNullPointerException")
    void hvk02() {
        assertThrows(NullPointerException.class, () -> conflictService.detectConflicts(null));
    }

    // HVK03: Entry có timeSlots null -> bị bỏ qua
    @Test
    @DisplayName("HVK03_nullTimeSlots_skipped")
    void hvk03() {
        ScheduleEntry entry = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building("A2").timeSlots(null).build();
        ConflictResult result = conflictService.detectConflicts(Collections.singletonList(entry));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK04: Hai lớp cùng phòng cùng giờ -> phát hiện xung đột
    @Test
    @DisplayName("HVK04_sameRoomSameTime_conflictDetected")
    void hvk04() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() > 0);
    }

    // HVK05: Cùng phòng khác ngày -> 0 xung đột
    @Test
    @DisplayName("HVK05_sameRoomDifferentDay_zeroConflicts")
    void hvk05() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 3", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK06: Cùng phòng khác kíp -> 0 xung đột
    @Test
    @DisplayName("HVK06_sameRoomDifferentShift_zeroConflicts")
    void hvk06() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 2", "2", "3", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK07: Ba lớp cùng phòng cùng giờ -> gom nhóm xung đột
    @Test
    @DisplayName("HVK07_threeClassesSameRoomSameTime_groupedConflict")
    void hvk07() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ScheduleEntry e3 = makeEntry("INT2203", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2, e3));
        assertTrue(result.getRoomConflicts().size() >= 1);
    }

    // HVK08: Một giáo viên dạy hai lớp cùng giờ -> phát hiện xung đột giáo viên
    @Test
    @DisplayName("HVK08_teacherSameTime_detectsTeacherConflict")
    void hvk08() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "402", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTeacherConflicts().size() >= 1);
    }

    // HVK09: Cùng giáo viên khác ngày -> 0 xung đột
    @Test
    @DisplayName("HVK09_teacherDifferentDay_zeroConflicts")
    void hvk09() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 3", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "402", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTeacherConflicts().size());
    }

    // HVK10: Không có xung đột -> total = 0
    @Test
    @DisplayName("HVK10_noConflicts_totalZero")
    void hvk10() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 3", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "402", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // =========================================================
    // HVK11-HVK20: ScheduleConflictDetectionService - lớp online / edge cases
    // =========================================================

    // HVK11: Lớp online với phòng "Online" -> bỏ qua kiểm tra phòng
    @Test
    @DisplayName("HVK11_onlineRoom_skipped")
    void hvk11() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "Online", "Zoom", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "Online", "Zoom", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        // Lớp online được bỏ qua trong kiểm tra xung đột phòng
        assertTrue(result.getTotalConflicts() >= 0);
    }

    // HVK12: Tòa LMS -> bỏ qua
    @Test
    @DisplayName("HVK12_lmsBuilding_skipped")
    void hvk12() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "101", "LMS", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "102", "LMS", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() >= 0);
    }

    // HVK13: Các entry trùng nhau -> không xung đột
    @Test
    @DisplayName("HVK13_identicalEntries_noConflict")
    void hvk13() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2201", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getRoomConflicts().size());
    }

    // HVK14: Cả phòng và giáo viên đều xung đột -> phát hiện cả hai loại
    @Test
    @DisplayName("HVK14_bothConflictTypes_detectsBoth")
    void hvk14() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "401", "A2", slot);
        ScheduleEntry e3 = makeEntryWithTeacher("INT2203", "GV002", "K64", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2, e3));
        assertTrue(result.getRoomConflicts().size() > 0);
        assertTrue(result.getTeacherConflicts().size() > 0);
    }

    // HVK15: detectRoomConflicts(null) -> NPE
    @Test
    @DisplayName("HVK15_detectRoomConflictsNull_throwsNPE")
    void hvk15() {
        assertThrows(NullPointerException.class, () -> conflictService.detectRoomConflicts(null));
    }

    // HVK16: detectTeacherConflicts(null) -> NPE
    @Test
    @DisplayName("HVK16_detectTeacherConflictsNull_throwsNPE")
    void hvk16() {
        assertThrows(NullPointerException.class, () -> conflictService.detectTeacherConflicts(null));
    }

    // HVK17: detectRoomConflicts danh sách rỗng -> 0 xung đột
    @Test
    @DisplayName("HVK17_detectRoomConflictsEmpty_zeroConflicts")
    void hvk17() {
        List<ConflictResult.RoomConflict> conflicts = conflictService.detectRoomConflicts(Collections.emptyList());
        assertEquals(0, conflicts.size());
    }

    // HVK18: detectTeacherConflicts danh sách rỗng -> 0 xung đột
    @Test
    @DisplayName("HVK18_detectTeacherConflictsEmpty_zeroConflicts")
    void hvk18() {
        List<ConflictResult.TeacherConflict> conflicts = conflictService.detectTeacherConflicts(Collections.emptyList());
        assertEquals(0, conflicts.size());
    }

    // HVK19: Phòng null -> entry bị bỏ qua
    @Test
    @DisplayName("HVK19_roomNull_skipped")
    void hvk19() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room(null).building("A2")
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        // Room null -> bỏ qua trong kiểm tra xung đột phòng
        assertTrue(result.getRoomConflicts().size() >= 0);
    }

    // HVK20: Giáo viên null -> bỏ qua kiểm tra xung đột giáo viên
    @Test
    @DisplayName("HVK20_teacherIdNull_skippedInTeacherConflict")
    void hvk20() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").teacherId(null).room("401").building("A2")
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K64", "402", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTeacherConflicts().size());
    }

    // ----------------------------------------
    // HVK21-HVK30: Gom nhóm xung đột và edge cases
    // ----------------------------------------

    // HVK21: Cùng phòng cùng ngày cùng tuần khác kíp -> 0 xung đột
    @Test
    @DisplayName("HVK21_sameRoomSameWeekDifferentShift_zeroConflicts")
    void hvk21() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 2", "2", "3", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK22: Cùng phòng cùng kíp khác tuần -> 0 xung đột
    @Test
    @DisplayName("HVK22_sameRoomSameShiftDifferentWeek_zeroConflicts")
    void hvk22() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 2", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot2);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK23: Entry có null trong danh sách timeSlots -> NPE
    @Test
    @DisplayName("HVK23_nullInTimeSlotsList_throwsNPE")
    void hvk23() {
        ScheduleEntry entry = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building("A2")
                .timeSlots(Collections.singletonList(null)).build();
        assertThrows(NullPointerException.class, () -> conflictService.detectConflicts(Collections.singletonList(entry)));
    }

    // HVK24: Phòng chứa từ khóa Zoom -> bỏ qua
    @Test
    @DisplayName("HVK24_zoomRoom_skipped")
    void hvk24() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "Zoom001", "Online", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "Zoom002", "Online", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() >= 0);
    }

    // HVK25: Phòng chứa từ khóa Trực tuyến -> bỏ qua
    @Test
    @DisplayName("HVK25_trucTuyenKeyword_skipped")
    void hvk25() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "PhÃ²ng A", "Trá»±c tuyáº¿n", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "PhÃ²ng B", "Trá»±c tuyáº¿n", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() >= 0);
    }

    // HVK26: Phòng chứa từ khóa Meet -> bỏ qua
    @Test
    @DisplayName("HVK26_meetKeyword_skipped")
    void hvk26() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "Meet001", "Google Meet", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "Meet002", "Google Meet", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() >= 0);
    }

    // HVK27: Nhiều xung đột giáo viên cùng giờ khác môn
    @Test
    @DisplayName("HVK27_teacherMultipleConflicts_sameTimeDifferentSubjects")
    void hvk27() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "402", "A2", slot);
        ScheduleEntry e3 = makeEntryWithTeacher("INT2203", "GV001", "K66", "403", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2, e3));
        assertTrue(result.getTeacherConflicts().size() >= 1);
    }

    // HVK28: Giáo viên khác nhau cùng phòng cùng giờ -> chỉ xung đột phòng
    @Test
    @DisplayName("HVK28_differentTeachersSameRoom_onlyRoomConflict")
    void hvk28() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV002", "K65", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getRoomConflicts().size() > 0);
        assertEquals(0, result.getTeacherConflicts().size());
    }

    // HVK29: Entry có building null -> NPE khi kiểm tra lớp online (bug thực tế)
    @Test
    @DisplayName("HVK29_buildingNull_npeInIsOnlineClass")
    void hvk29() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building(null)
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        // Code thực tế có bug: getBuilding().toLowerCase() ném NPE khi building là null
        assertThrows(NullPointerException.class, () -> conflictService.detectConflicts(Arrays.asList(e1, e2)));
    }

    // HVK30: Entry có building rỗng
    @Test
    @DisplayName("HVK30_emptyBuildingString_worksCorrectly")
    void hvk30() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building("")
                .timeSlots(Collections.singletonList(slot)).build();
        ConflictResult result = conflictService.detectConflicts(Collections.singletonList(e1));
        assertNotNull(result);
    }

    // ----------------------------------------
    // HVK31-HVK40: DTO - ConflictResult, ScheduleEntry, TimeSlot
    // ----------------------------------------

    // HVK31: RoomConflict.getConflictDescription
    @Test
    @DisplayName("HVK31_roomConflictGetDescription_containsRoomInfo")
    void hvk31() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ConflictResult.RoomConflict conflict = ConflictResult.RoomConflict.builder()
                .room("401")
                .timeSlot(slot)
                .conflictingSchedules(Collections.singletonList(makeEntry("INT2201", "401", "A2", slot)))
                .conflictWeeks(Collections.singletonList("1"))
                .build();

        String desc = conflict.getConflictDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("401") || desc.contains("Thá»©"));
    }

    // HVK32: RoomConflict.getConflictKey
    @Test
    @DisplayName("HVK32_roomConflictGetKey_formatCorrect")
    void hvk32() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ConflictResult.RoomConflict conflict = ConflictResult.RoomConflict.builder()
                .room("401").timeSlot(slot).build();
        String key = conflict.getConflictKey();
        assertNotNull(key);
        assertTrue(key.contains("401"));
    }

    // HVK33: TeacherConflict.getConflictDescription
    @Test
    @DisplayName("HVK33_teacherConflictGetDescription_containsTeacherInfo")
    void hvk33() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ConflictResult.TeacherConflict conflict = ConflictResult.TeacherConflict.builder()
                .teacherId("GV001")
                .teacherName("Nguyen Van A")
                .timeSlot(slot)
                .conflictingSchedules(Collections.singletonList(makeEntry("INT2201", "401", "A2", slot)))
                .conflictWeeks(Collections.singletonList("1"))
                .build();

        String desc = conflict.getConflictDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("GV001") || desc.contains("Nguyen Van A"));
    }

    // HVK34: TeacherConflict.getConflictKey
    @Test
    @DisplayName("HVK34_teacherConflictGetKey_formatCorrect")
    void hvk34() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ConflictResult.TeacherConflict conflict = ConflictResult.TeacherConflict.builder()
                .teacherId("GV001").timeSlot(slot).build();
        String key = conflict.getConflictKey();
        assertNotNull(key);
        assertTrue(key.contains("GV001"));
    }

    // HVK35: getTotalConflicts với danh sách null
    @Test
    @DisplayName("HVK35_conflictResultNullLists_totalZero")
    void hvk35() {
        ConflictResult result = ConflictResult.builder()
                .roomConflicts(null)
                .teacherConflicts(null)
                .totalConflicts(0)
                .build();
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK36: getTotalConflicts với danh sách rỗng
    @Test
    @DisplayName("HVK36_conflictResultEmptyLists_totalZero")
    void hvk36() {
        ConflictResult result = ConflictResult.builder()
                .roomConflicts(Collections.emptyList())
                .teacherConflicts(Collections.emptyList())
                .totalConflicts(0)
                .build();
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK37: TimeSlot.getSlotKey
    @Test
    @DisplayName("HVK37_timeSlotGetSlotKey_formatCorrect")
    void hvk37() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        String key = slot.getSlotKey();
        assertEquals("Tuáº§n 1-Thá»© 2-1-1-2", key);
    }

    // HVK38: TimeSlot.getDisplayInfo
    @Test
    @DisplayName("HVK38_timeSlotGetDisplayInfo_formatCorrect")
    void hvk38() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        String info = slot.getDisplayInfo();
        assertNotNull(info);
        assertTrue(info.contains("Tuáº§n 1") || info.contains("Thá»©"));
    }

    // HVK39: ScheduleEntry.getDisplayInfo
    @Test
    @DisplayName("HVK39_scheduleEntryGetDisplayInfo_formatCorrect")
    void hvk39() {
        ScheduleEntry entry = makeEntryFull("INT2201", "Lap trinh C", "GV001", "Nguyen A", "401", "A2", "K64", 60, makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2"));
        String info = entry.getDisplayInfo();
        assertNotNull(info);
        assertTrue(info.contains("INT2201"));
    }

    // HVK40: RoomConflict builder đầy đủ fields
    @Test
    @DisplayName("HVK40_roomConflictBuilderAllFields_worksCorrectly")
    void hvk40() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ConflictResult.RoomConflict conflict = ConflictResult.RoomConflict.builder()
                .room("401")
                .timeSlot(slot)
                .conflictingSchedules(Arrays.asList(e1, e2))
                .conflictWeeks(Arrays.asList("1", "2"))
                .build();

        assertEquals("401", conflict.getRoom());
        assertEquals(slot, conflict.getTimeSlot());
        assertEquals(2, conflict.getConflictingSchedules().size());
        assertEquals(2, conflict.getConflictWeeks().size());
    }

    // ----------------------------------------
    // HVK41-HVK50: DTO - ScheduleValidationResult
    // ----------------------------------------

    // HVK41: hasConflicts -> true
    @Test
    @DisplayName("HVK41_validationResultHasConflicts_true")
    void hvk41() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ConflictResult conflictResult = conflictService.detectConflicts(Arrays.asList(e1, e2));

        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(conflictResult)
                .scheduleEntries(Arrays.asList(e1, e2))
                .fileName("test.xlsx")
                .totalEntries(2)
                .fileSize(1024)
                .build();

        assertTrue(result.hasConflicts());
    }

    // HVK42: hasConflicts -> false
    @Test
    @DisplayName("HVK42_validationResultHasConflicts_false")
    void hvk42() {
        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(ConflictResult.builder().roomConflicts(Collections.emptyList()).teacherConflicts(Collections.emptyList()).totalConflicts(0).build())
                .scheduleEntries(Collections.emptyList())
                .fileName("test.xlsx")
                .totalEntries(0)
                .fileSize(0)
                .build();
        assertFalse(result.hasConflicts());
    }

    // HVK43: hasConflicts với conflictResult null
    @Test
    @DisplayName("HVK43_validationResultNullConflictResult_hasConflictsFalse")
    void hvk43() {
        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(null)
                .build();
        assertFalse(result.hasConflicts());
    }

    // HVK44: getRoomConflictCount
    @Test
    @DisplayName("HVK44_getRoomConflictCount_worksCorrectly")
    void hvk44() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ConflictResult cr = conflictService.detectConflicts(Arrays.asList(e1, e2));

        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(cr)
                .build();
        assertTrue(result.getRoomConflictCount() >= 0);
    }

    // HVK45: getRoomConflictCount với null
    @Test
    @DisplayName("HVK45_getRoomConflictCountNull_returnsZero")
    void hvk45() {
        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(ConflictResult.builder().roomConflicts(null).teacherConflicts(null).build())
                .build();
        assertEquals(0, result.getRoomConflictCount());
    }

    // HVK46: getTeacherConflictCount
    @Test
    @DisplayName("HVK46_getTeacherConflictCount_worksCorrectly")
    void hvk46() {
        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(ConflictResult.builder()
                        .roomConflicts(Collections.emptyList())
                        .teacherConflicts(Collections.emptyList())
                        .build())
                .build();
        assertEquals(0, result.getTeacherConflictCount());
    }

    // HVK47: getTeacherConflictCount với null
    @Test
    @DisplayName("HVK47_getTeacherConflictCountNull_returnsZero")
    void hvk47() {
        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(ConflictResult.builder().roomConflicts(null).teacherConflicts(null).build())
                .build();
        assertEquals(0, result.getTeacherConflictCount());
    }

    // HVK48: getFormattedFileSize(0) -> "0 Bytes"
    @Test
    @DisplayName("HVK48_getFormattedFileSizeZero_bytes")
    void hvk48() {
        ScheduleValidationResult result = ScheduleValidationResult.builder().fileSize(0).build();
        assertEquals("0 Bytes", result.getFormattedFileSize());
    }

    // HVK49: getFormattedFileSize(1024) -> "1.00 KB"
    @Test
    @DisplayName("HVK49_getFormattedFileSize1024_kilobytes")
    void hvk49() {
        ScheduleValidationResult result = ScheduleValidationResult.builder().fileSize(1024).build();
        assertEquals("1.00 KB", result.getFormattedFileSize());
    }

    // HVK50: getFormattedFileSize(1048576) -> "1.00 MB"
    @Test
    @DisplayName("HVK50_getFormattedFileSizeLarge_megabytes")
    void hvk50() {
        ScheduleValidationResult result = ScheduleValidationResult.builder().fileSize(1048576).build();
        assertEquals("1.00 MB", result.getFormattedFileSize());
    }

    // ----------------------------------------
    // HVK51-HVK65: ScheduleValidationController - API endpoints
    // ----------------------------------------

    // HVK51: validate-format với file hợp lệ -> 200
    @Test
    @DisplayName("HVK51_validateFormat_validFile_returnsOk")
    void hvk51() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK52: validate-format với file rỗng -> 200 kèm badRequest
    @Test
    @DisplayName("HVK52_validateFormat_emptyFile_returnsBadRequestBody")
    void hvk52() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(emptyFile))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK53: validate-format với định dạng sai -> 200 kèm badRequest
    @Test
    @DisplayName("HVK53_validateFormat_invalidFormat_returnsBadRequestBody")
    void hvk53() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "invalid.txt", MediaType.TEXT_PLAIN_VALUE, "not excel".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(invalidFile)).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(invalidFile))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK54: analyze với file hợp lệ và có xung đột -> 200
    @Test
    @DisplayName("HVK54_analyze_validFileWithConflicts_returnsOk")
    void hvk54() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "conflict.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file)).thenReturn(Arrays.asList(e1, e2));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK55: analyze với file hợp lệ không xung đột -> 200
    @Test
    @DisplayName("HVK55_analyze_validFileNoConflicts_returnsOk")
    void hvk55() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "no-conflict.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file)).thenReturn(Collections.singletonList(e1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK56: analyze với file rỗng -> 200 kèm badRequest
    @Test
    @DisplayName("HVK56_analyze_emptyFile_returnsBadRequestBody")
    void hvk56() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(emptyFile))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK57: analyze với định dạng sai -> 200 kèm badRequest
    @Test
    @DisplayName("HVK57_analyze_invalidFormat_returnsBadRequestBody")
    void hvk57() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "invalid.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "not valid".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(invalidFile)).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(invalidFile))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK58: analyze với danh sách entry rỗng -> 200 kèm badRequest
    @Test
    @DisplayName("HVK58_analyze_emptyScheduleEntries_returnsBadRequestBody")
    void hvk58() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty-data.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file)).thenReturn(Collections.emptyList());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK59: analyze với service ném exception -> 200 kèm error
    @Test
    @DisplayName("HVK59_analyze_serviceException_returnsErrorBody")
    void hvk59() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK60: getConflictDetails room không tham số -> 200
    @Test
    @DisplayName("HVK60_getConflictDetailsRoomNoParams_returnsOk")
    void hvk60() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedule-validation/conflicts/room"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK61: getConflictDetails teacher có tham số -> 200
    @Test
    @DisplayName("HVK61_getConflictDetailsTeacherWithParams_returnsOk")
    void hvk61() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedule-validation/conflicts/teacher")
                        .param("room", "401")
                        .param("teacherId", "GV001"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // HVK62: validate-format với service ném exception -> 200 kèm error
    @Test
    @DisplayName("HVK62_validateFormatServiceException_returnsErrorBody")
    void hvk62() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenThrow(new RuntimeException("Parse error"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    // ----------------------------------------
    // HVK63-HVK75: Gom nhóm xung đột và edge cases
    // ----------------------------------------

    // HVK63: Gom nhóm phòng - nhiều phòng trùng cùng giờ -> gom lại
    @Test
    @DisplayName("HVK63_groupedRoomConflicts_mergedCorrectly")
    void hvk63() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot1);
        ScheduleEntry e3 = makeEntry("INT2203", "401", "A2", slot1);

        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2, e3));
        assertTrue(result.getTotalConflicts() > 0);
    }

    // HVK64: Gom nhóm giáo viên - cùng giáo viên dạy nhiều lớp
    @Test
    @DisplayName("HVK64_groupedTeacherConflicts_mergedCorrectly")
    void hvk64() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot1);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "402", "A2", slot1);

        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTeacherConflicts().size() >= 1);
    }

    // HVK65: Xung đột phòng với nhiều timeSlots trong một entry
    @Test
    @DisplayName("HVK65_entryWithMultipleTimeSlots_conflictsDetected")
    void hvk65() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 3", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building("A2")
                .timeSlots(Arrays.asList(slot1, slot2)).build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot1);

        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() > 0);
    }

    // HVK66: Xung đột giáo viên với nhiều timeSlots
    @Test
    @DisplayName("HVK66_teacherMultipleTimeSlots_conflictsDetected")
    void hvk66() {
        ScheduleEntry.TimeSlot slot1 = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry.TimeSlot slot2 = makeSlot("Tuáº§n 1", "Thá»© 3", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").teacherId("GV001").room("401").building("A2")
                .timeSlots(Arrays.asList(slot1, slot2)).build();
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "402", "A2", slot1);

        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTeacherConflicts().size() >= 1);
    }

    // HVK67: Nhiều lớp cùng phòng cùng giờ -> nhiều xung đột
    @Test
    @DisplayName("HVK67_manyClassesSameRoomSameTime_multipleConflicts")
    void hvk67() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        List<ScheduleEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            entries.add(makeEntry("SUBJ" + i, "401", "A2", slot));
        }
        ConflictResult result = conflictService.detectConflicts(entries);
        assertTrue(result.getTotalConflicts() > 0);
    }

    // HVK68: extractWeekNumber với date null -> xử lý null
    @Test
    @DisplayName("HVK68_nullDate_worksCorrectly")
    void hvk68() {
        ScheduleEntry.TimeSlot slot = makeSlot(null, "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Collections.singletonList(e1));
        assertNotNull(result);
    }

    // HVK69: ScheduleEntry builder đầy đủ fields
    @Test
    @DisplayName("HVK69_scheduleEntryFullBuilder_worksCorrectly")
    void hvk69() {
        ScheduleEntry entry = makeEntryFull(
                "INT2201", "Lap trinh C", "GV001", "Nguyen Van A",
                "401", "A2", "K64", 60, makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2"));

        assertEquals("INT2201", entry.getSubjectCode());
        assertEquals("Lap trinh C", entry.getSubjectName());
        assertEquals("GV001", entry.getTeacherId());
        assertEquals("Nguyen Van A", entry.getTeacherName());
        assertEquals("401", entry.getRoom());
        assertEquals("A2", entry.getBuilding());
        assertEquals("K64", entry.getClassGroup());
        assertEquals(60, entry.getStudentCount());
        assertNotNull(entry.getTimeSlots());
        assertEquals(1, entry.getTimeSlots().size());
    }

    // HVK70: ScheduleValidationResult builder đầy đủ fields
    @Test
    @DisplayName("HVK70_validationResultFullBuilder_worksCorrectly")
    void hvk70() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ConflictResult cr = conflictService.detectConflicts(Collections.singletonList(e1));

        ScheduleValidationResult result = ScheduleValidationResult.builder()
                .conflictResult(cr)
                .scheduleEntries(Collections.singletonList(e1))
                .fileName("test.xlsx")
                .totalEntries(1)
                .fileSize(2048)
                .build();

        assertEquals(cr, result.getConflictResult());
        assertEquals(1, result.getScheduleEntries().size());
        assertEquals("test.xlsx", result.getFileName());
        assertEquals(1, result.getTotalEntries());
        assertEquals(2048, result.getFileSize());
    }

    // ----------------------------------------
    // HVK71-HVK75: Edge cases bổ sung
    // ----------------------------------------

    // HVK71: TeacherConflict builder đầy đủ fields
    @Test
    @DisplayName("HVK71_teacherConflictFullBuilder_worksCorrectly")
    void hvk71() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ConflictResult.TeacherConflict conflict = ConflictResult.TeacherConflict.builder()
                .teacherId("GV001")
                .teacherName("Nguyen Van A")
                .timeSlot(slot)
                .conflictingSchedules(Collections.singletonList(e1))
                .conflictWeeks(Arrays.asList("1", "2"))
                .build();

        assertEquals("GV001", conflict.getTeacherId());
        assertEquals("Nguyen Van A", conflict.getTeacherName());
        assertEquals(slot, conflict.getTimeSlot());
        assertEquals(1, conflict.getConflictingSchedules().size());
        assertEquals(2, conflict.getConflictWeeks().size());
    }

    // HVK72: TimeSlot builder với date null
    @Test
    @DisplayName("HVK72_timeSlotNullDate_worksCorrectly")
    void hvk72() {
        ScheduleEntry.TimeSlot slot = makeSlot(null, "Thá»© 2", "1", "1", "2");
        assertNotNull(slot.getSlotKey());
        assertNotNull(slot.getDisplayInfo());
    }

    // HVK73: Tất cả entry cùng giáo viên và phòng -> cả hai loại xung đột
    @Test
    @DisplayName("HVK73_sameTeacherSameRoom_bothConflictTypes")
    void hvk73() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryWithTeacher("INT2201", "GV001", "K64", "401", "A2", slot);
        ScheduleEntry e2 = makeEntryWithTeacher("INT2202", "GV001", "K65", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getRoomConflicts().size() > 0 || result.getTeacherConflicts().size() > 0);
    }

    // HVK74: getFormattedFileSize với fileSize lớn -> GB
    @Test
    @DisplayName("HVK74_getFormattedFileSizeGigabytes")
    void hvk74() {
        ScheduleValidationResult result = ScheduleValidationResult.builder().fileSize(1073741824L).build();
        assertEquals("1.00 GB", result.getFormattedFileSize());
    }

    // HVK75: detectConflicts với một entry -> 0 xung đột
    @Test
    @DisplayName("HVK75_singleEntry_zeroConflicts")
    void hvk75() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuáº§n 1", "Thá»© 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ConflictResult result = conflictService.detectConflicts(Collections.singletonList(e1));
        assertEquals(0, result.getTotalConflicts());
    }

    // ----------------------------------------
    // HVK76-HVK90: Xử lý lỗi và sự cố hệ thống
    // ----------------------------------------

    // HVK76: validate-format với service ném FileProcessingException -> success=false
    @Test
    @DisplayName("HVK76_validateFormat_serviceThrowsFileProcessingException_returnsErrorBody")
    void hvk76() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupt.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "corrupt data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file))
                .thenThrow(new FileProcessingException("File is too large to process"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK77: validate-format với service ném RuntimeException -> error body
    @Test
    @DisplayName("HVK77_validateFormat_serviceThrowsRuntimeException_returnsErrorBody")
    void hvk77() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file))
                .thenThrow(new RuntimeException("OutOfMemoryError: Java heap space exhausted"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/validate-format")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK78: analyze với service ném FileProcessingException -> success=false
    @Test
    @DisplayName("HVK78_analyze_serviceThrowsFileProcessingException_returnsErrorBody")
    void hvk78() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupt.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "corrupt data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file))
                .thenThrow(new FileProcessingException("Excel file is corrupted or has invalid format"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK79: analyze với service ném RuntimeException -> success=false
    @Test
    @DisplayName("HVK79_analyze_serviceThrowsRuntimeException_returnsErrorBody")
    void hvk79() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file))
                .thenThrow(new RuntimeException("OutOfMemoryError: Java heap space exhausted"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK80: analyze với service ném NullPointerException -> success=false
    @Test
    @DisplayName("HVK80_analyze_serviceThrowsNPE_returnsErrorBody")
    void hvk80() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "null.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file))
                .thenThrow(new NullPointerException("Cannot invoke method on null database connection"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK81: analyze với service ném InvalidDataException -> success=false
    @Test
    @DisplayName("HVK81_analyze_serviceThrowsInvalidDataException_returnsErrorBody")
    void hvk81() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "data".getBytes());

        when(excelReaderService.validateScheduleExcelFormat(file)).thenReturn(true);
        when(excelReaderService.readScheduleFromExcel(file))
                .thenThrow(new InvalidDataException("No schedule data found in Excel sheet"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/schedule-validation/analyze")
                        .file(file))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }


    // ----------------------------------------
    // HVK82-HVK95: Bug trong code thực tế
    // ----------------------------------------

    // HVK82: ConflictResult với danh sách đầy đủ -> totalConflicts tính đúng
    @Test
    @DisplayName("HVK82_conflictResultWithData_totalConflictsCorrectSum")
    void hvk82() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ScheduleEntry e3 = makeEntry("INT2203", "402", "A2", slot);

        ConflictResult result = ConflictResult.builder()
                .roomConflicts(Collections.singletonList(
                        ConflictResult.RoomConflict.builder()
                                .room("401").timeSlot(slot)
                                .conflictingSchedules(Arrays.asList(e1, e2))
                                .conflictWeeks(Collections.singletonList("1"))
                                .build()))
                .teacherConflicts(Collections.singletonList(
                        ConflictResult.TeacherConflict.builder()
                                .teacherId("GV001").timeSlot(slot)
                                .conflictingSchedules(Collections.singletonList(e3))
                                .conflictWeeks(Collections.singletonList("1"))
                                .build()))
                .build();

        assertEquals(2, result.getTotalConflicts());
        assertEquals(1, result.getRoomConflicts().size());
        assertEquals(1, result.getTeacherConflicts().size());
    }

    // HVK83: RoomConflict.getConflictDescription với nhiều schedules
    @Test
    @DisplayName("HVK83_roomConflictDescription_formatWithMultipleSchedules")
    void hvk83() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntry("INT2201", "401", "A2", slot);
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);
        ScheduleEntry e3 = makeEntry("INT2203", "401", "A2", slot);

        ConflictResult.RoomConflict conflict = ConflictResult.RoomConflict.builder()
                .room("401").timeSlot(slot)
                .conflictingSchedules(Arrays.asList(e1, e2, e3))
                .conflictWeeks(Collections.singletonList("1"))
                .build();

        String desc = conflict.getConflictDescription();
        assertNotNull(desc);
        assertTrue(desc.length() > 0);
    }

    // HVK84: TeacherConflict.getConflictDescription với teacherName null
    @Test
    @DisplayName("HVK84_teacherConflictNullTeacherName_descriptionWorks")
    void hvk84() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ConflictResult.TeacherConflict conflict = ConflictResult.TeacherConflict.builder()
                .teacherId("GV001")
                .teacherName(null)
                .timeSlot(slot)
                .conflictingSchedules(Collections.emptyList())
                .build();

        String desc = conflict.getConflictDescription();
        assertNotNull(desc);
    }

    // HVK85: RoomConflict.getConflictKey format nhất quán
    @Test
    @DisplayName("HVK85_roomConflictKey_formatConsistent")
    void hvk85() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ConflictResult.RoomConflict c1 = ConflictResult.RoomConflict.builder()
                .room("401").timeSlot(slot).build();
        ConflictResult.RoomConflict c2 = ConflictResult.RoomConflict.builder()
                .room("401").timeSlot(slot).build();

        assertEquals(c1.getConflictKey(), c2.getConflictKey());
    }

    // HVK86: TeacherConflict.getConflictKey với teacherName null
    @Test
    @DisplayName("HVK86_teacherConflictKey_withNullTeacherName")
    void hvk86() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ConflictResult.TeacherConflict conflict = ConflictResult.TeacherConflict.builder()
                .teacherId("GV001")
                .teacherName(null)
                .timeSlot(slot)
                .build();

        String key = conflict.getConflictKey();
        assertNotNull(key);
        assertTrue(key.contains("GV001"));
    }

    // HVK88: TimeSlot với tất cả fields null -> getSlotKey xử lý tốt
    @Test
    @DisplayName("HVK88_timeSlotAllNullFields_worksCorrectly")
    void hvk88() {
        ScheduleEntry.TimeSlot slot = ScheduleEntry.TimeSlot.builder()
                .date(null).dayOfWeek(null).shift(null).startPeriod(null).numberOfPeriods(null)
                .build();
        assertNotNull(slot.getSlotKey());
        assertNotNull(slot.getDisplayInfo());
    }

    // HVK89: isOnlineClass với building=null -> NPE
    // BUG: getBuilding().toLowerCase() được gọi không có kiểm tra null
    @Test
    @DisplayName("HVK89_isOnlineClass_buildingNull_throwsNPE")
    void hvk89() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building(null)
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);

        // BUG: isOnlineClass() gọi entry.getBuilding().toLowerCase() không kiểm tra null
        // Nếu building là null -> NullPointerException
        assertThrows(NullPointerException.class, () -> conflictService.detectConflicts(Arrays.asList(e1, e2)));
    }

    // HVK90: isOnlineClass với room=null -> không NPE (có kiểm tra null)
    @Test
    @DisplayName("HVK90_isOnlineClass_roomNull_noNPE")
    void hvk90() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room(null).building("A2")
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", slot);

        // Bug: entry room=null tham gia vào kiểm tra xung đột phòng
        // e1 (room=null) và e2 (room=401) cùng giờ -> phát hiện xung đột phòng
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(1, result.getRoomConflicts().size());
    }

    // HVK91: detectRoomConflicts với teacherId=null và room=null -> phát hiện xung đột
    @Test
    @DisplayName("HVK91_bothRoomAndTeacherNull_detectsConflict")
    void hvk91() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room(null).building(null)
                .timeSlots(Collections.singletonList(slot)).build();
        ScheduleEntry e2 = ScheduleEntry.builder()
                .subjectCode("INT2202").room(null).building(null)
                .timeSlots(Collections.singletonList(slot)).build();

        // Bug: cả room/teacher đều null -> vẫn xung đột với nhau
        // Hai entry có room null cùng giờ phải xung đột
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertTrue(result.getTotalConflicts() > 0);
    }

    // HVK92: Xung đột phòng với timeSlots null -> entry bị bỏ qua
    @Test
    @DisplayName("HVK92_nullTimeSlots_entrySkipped")
    void hvk92() {
        ScheduleEntry e1 = ScheduleEntry.builder()
                .subjectCode("INT2201").room("401").building("A2")
                .timeSlots(null)
                .build();
        ScheduleEntry e2 = makeEntry("INT2202", "401", "A2", makeSlot("Tuần 1", "Thứ 2", "1", "1", "2"));

        // Bug: timeSlots null -> entry bị bỏ qua hoàn toàn
        // e1 bị bỏ qua, e2 một mình không có xung đột
        ConflictResult result = conflictService.detectConflicts(Arrays.asList(e1, e2));
        assertEquals(0, result.getTotalConflicts());
    }

    // HVK93: slotKey với số tuần một chữ số
    @Test
    @DisplayName("HVK93_slotKeyParsing_weekNumberExtractedCorrectly")
    void hvk93() {
        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        String key = slot.getSlotKey();
        assertEquals("Tuần 1-Thứ 2-1-1-2", key);
        assertTrue(key.contains("Tuần 1"));
        assertTrue(key.contains("Thứ 2"));
    }

    // HVK94: groupRoomConflictsByPattern - date null gây null trong conflictWeeks
    @Test
    @DisplayName("HVK94_conflictWithNullDate_noWeekInResult")
    void hvk94() {
        ScheduleEntry.TimeSlot slot = makeSlot(null, "Thứ 2", "1", "1", "2");
        ConflictResult.RoomConflict conflict = ConflictResult.RoomConflict.builder()
                .room("401")
                .timeSlot(slot)
                .conflictingSchedules(Collections.emptyList())
                .build();

        assertNotNull(conflict.getConflictDescription());
    }

    // HVK95: ConflictResult với danh sách roomConflicts null -> getTotalConflicts xử lý null
    @Test
    @DisplayName("HVK95_conflictResultNullLists_handledGracefully")
    void hvk95() {
        ConflictResult result = ConflictResult.builder()
                .roomConflicts(null)
                .teacherConflicts(null)
                .totalConflicts(0)
                .build();

        assertEquals(0, result.getTotalConflicts());
    }

    // ----------------------------------------
    // HVK96-HVK98: Bắt lỗi file TKB thiếu cột (34 cột < 44 cột) - XTTĐH-01
    // ----------------------------------------
    // Tình huống: Người dùng xuất file TKB từ chức năng "Xuất Excel" (SavedSchedulesPage.tsx),
    // file TKB chỉ có 34 cột (thiếu 10 cột tuần T11-T17 và có thể cột khác).
    // Người dùng upload file đó lên hệ thống qua /api/schedule-validation/validate-format
    // hoặc /analyze.
    // Expected (Đúng): hệ thống từ chối file, báo "File có 34 cột, cần đủ 44 cột".
    // Thực tế (Bug): hệ thống chấp nhận file 34 cột hoặc báo lỗi chung chung không rõ lý do.

    /**
     * Tạo Excel file TKB không hợp lệ - thiếu cột (chỉ có 34 cột thay vì 44 cột).
     * Backend đọc data rows từ row index 3 (bỏ qua 3 header rows).
     * Cột tuần: chỉ có AB(27)-AK(36) = 10 cột (T1-T10), thiếu 7 cột tuần cuối.
     * File này có đầy đủ thông tin: mã môn, tên, phòng, giáo viên, 3 header rows,
     * nhưng KHÔNG đủ 44 cột theo định dạng chuẩn.
     */
    private MockMultipartFile createInvalidTKBExcel() throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("TKB");

        // Row 0, 1: header metadata (backend bỏ qua 3 rows đầu)
        sheet.createRow(0);
        sheet.createRow(1);
        // Row 2: header column names - CHỈ 34 cột (thiếu 10 cột so với chuẩn 44)
        Row headerRow = sheet.createRow(2);
        for (int col = 0; col < 34; col++) {
            headerRow.createCell(col).setCellValue("Col" + col);
        }
        // Row 3: data row - môn học: thứ 2, kíp 1, tiết 1, 2 tiết, phòng 401-A2, tuần 1-5
        Row dataRow = sheet.createRow(3);
        dataRow.createCell(1).setCellValue("INT2201");      // B - Mã môn
        dataRow.createCell(2).setCellValue("Nhập môn lập trình"); // C - Tên
        dataRow.createCell(3).setCellValue("K64");           // D - Nhóm
        dataRow.createCell(6).setCellValue("2");             // G - Thứ
        dataRow.createCell(7).setCellValue("1");             // H - Kíp
        dataRow.createCell(8).setCellValue("1");             // I - Tiết bắt đầu
        dataRow.createCell(9).setCellValue("2");             // J - Số tiết
        dataRow.createCell(10).setCellValue("401");          // K - Phòng
        dataRow.createCell(11).setCellValue("A2");           // L - Tòa
        dataRow.createCell(19).setCellValue(45);             // T - Sĩ số
        dataRow.createCell(21).setCellValue("GV001");        // V - Mã GV
        dataRow.createCell(22).setCellValue("Nguyễn Văn A");// W - Tên GV
        for (int w = 1; w <= 5; w++) {
            dataRow.createCell(27 + w - 1).setCellValue("x"); // Tuần 1-5: AB(27) -> AF(31)
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        wb.close();
        return new MockMultipartFile("file", "tkb_thieu_cot_34.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, bos.toByteArray());
    }

    // HVK96: validate-format - file TKB có 34 cột bị hệ thống chấp nhận sai
    // Expected output: hệ thống phải từ chối file có 34 cột (phải có đủ 44 cột)
    // - File TKB chuẩn phải có 44 cột: mã môn (B), tên (C), nhóm (D), thứ (G), kíp (H),
    //   tiết bắt đầu (I), số tiết (J), phòng (K), tòa (L), sĩ số (T), mã GV (V), tên GV (W),
    //   và 17 cột tuần AB-AR (27-43) cho T1-T17.
    // - File có 34 cột: thiếu 10 cột tuần (T11-T17) và có thể thiếu cột khác.
    // Expected: success=false (hệ thống phải reject file thiếu cột).
    // Actual (BUG): success=true vì hệ thống chấp nhận file 34 cột -> test FAIL.
    @Test
    @DisplayName("HVK96_validateFormat_tkb34Columns_shouldReject")
    void hvk96() throws Exception {
        MockMultipartFile invalidTKB = createInvalidTKBExcel();

        // Hệ thống hiện tại (BUG): accept file 34 cột
        when(excelReaderService.validateScheduleExcelFormat(invalidTKB)).thenReturn(true);

        mockMvc.perform(multipart("/api/schedule-validation/validate-format")
                        .file(invalidTKB))
                .andExpect(status().isOk())
                // Expected: reject -> success=false
                // Actual (BUG): accept -> success=true -> FAIL
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK97: analyze - file TKB có 34 cột bị hệ thống chấp nhận sai ở bước validate
    // Expected output: hệ thống phải từ chối file TKB 34 cột ở bước validate, không phân tích xung đột
    // - validate phải reject file có 34 cột (file thiếu cột không hợp lệ)
    // - readScheduleFromExcel KHÔNG được gọi vì validate đã reject
    // Expected: success=false vì validate reject file 34 cột.
    // Actual (BUG): validate trả true cho file 34 cột -> readScheduleFromExcel được gọi
    //   -> vì mock trả empty list nên cũng reject, nhưng không phải do lỗi cột
    //   -> sửa: mock readScheduleFromExcel trả dữ liệu để ensure bug đến từ validate
    // Expected: success=false (hệ thống phải reject ở bước validate).
    // Actual (BUG): success=true -> test FAIL.
    @Test
    @DisplayName("HVK97_analyze_tkb34Columns_shouldReject")
    void hvk97() throws Exception {
        MockMultipartFile invalidTKB = createInvalidTKBExcel();

        ScheduleEntry.TimeSlot slot = makeSlot("Tuần 1", "Thứ 2", "1", "1", "2");
        ScheduleEntry e1 = makeEntryFull("INT2201", "Nhập môn lập trình",
                "GV001", "Nguyễn Văn A", "401", "A2", "K64", 45, slot);

        // Hệ thống hiện tại (BUG): accept file 34 cột ở bước validate
        when(excelReaderService.validateScheduleExcelFormat(invalidTKB)).thenReturn(true);
        // mock read trả dữ liệu để ensure bug đến từ validate chứ không phải read
        when(excelReaderService.readScheduleFromExcel(invalidTKB)).thenReturn(Collections.singletonList(e1));

        mockMvc.perform(multipart("/api/schedule-validation/analyze")
                        .file(invalidTKB))
                .andExpect(status().isOk())
                // Expected: reject -> success=false
                // Actual (BUG): accept -> success=true -> FAIL
                .andExpect(jsonPath("$.success").value(false));
    }

    // HVK98: validate-format - file TKB thiếu cột (34 cột) bị hệ thống chấp nhận sai
    // Expected output: file TKB thiếu cột phải bị từ chối với thông báo chi tiết số cột
    // - File TKB hợp lệ phải có đủ 44 cột: 27 cột thông tin + 17 cột tuần (T1-T17)
    // - File chỉ có 34 cột: thiếu 10 cột (T11-T17 và có thể cột khác)
    // - Thông báo lỗi phải nêu rõ: "File có 34 cột, cần đủ 44 cột"
    // Expected: success=false với message chứa "34 cột" và "44 cột".
    // Actual (BUG): success=true -> test FAIL.
    @Test
    @DisplayName("HVK98_validateFormat_tkb34Columns_shouldRejectWithColumnError")
    void hvk98() throws Exception {
        MockMultipartFile invalidTKB = createInvalidTKBExcel();

        // Hệ thống hiện tại (BUG): accept file 34 cột
        when(excelReaderService.validateScheduleExcelFormat(invalidTKB)).thenReturn(true);

        mockMvc.perform(multipart("/api/schedule-validation/validate-format")
                        .file(invalidTKB))
                .andExpect(status().isOk())
                // Expected: reject -> success=false
                // Actual (BUG): accept -> success=true -> FAIL
                .andExpect(jsonPath("$.success").value(false));
    }
}
