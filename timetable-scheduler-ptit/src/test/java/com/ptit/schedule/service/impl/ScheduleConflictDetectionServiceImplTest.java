package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.ConflictResult;
import com.ptit.schedule.dto.ScheduleEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleConflictDetectionServiceImplTest {

    private final ScheduleConflictDetectionServiceImpl service = new ScheduleConflictDetectionServiceImpl();

    private static ScheduleEntry.TimeSlot slot(String week, String dayOfWeek, String shift, String start, String periods) {
        return ScheduleEntry.TimeSlot.builder()
                .date("Tuần " + week)
                .dayOfWeek(dayOfWeek)
                .shift(shift)
                .startPeriod(start)
                .numberOfPeriods(periods)
                .build();
    }

    private static ScheduleEntry entry(String subjectCode, String teacherId, String teacherName, String room, String building,
                                      List<ScheduleEntry.TimeSlot> slots) {
        return ScheduleEntry.builder()
                .subjectCode(subjectCode)
                .subjectName("Mon " + subjectCode)
                .teacherId(teacherId)
                .teacherName(teacherName)
                .room(room)
                .building(building)
                .classGroup("N1")
                .studentCount(50)
                .timeSlots(slots)
                .build();
    }

    @Test
    @DisplayName("LL-SCD-01: Detect room conflict, ignore online entries")
    void detectRoomConflicts_shouldDetectAndIgnoreOnline() {
        ScheduleEntry.TimeSlot s1 = slot("1", "Thứ 2", "1", "1", "3");
        ScheduleEntry a = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(s1));
        ScheduleEntry b = entry("INT1002", "GV2", "Teacher 2", "401", "A1", List.of(s1));

        // Online entry: should be ignored for room conflicts
        ScheduleEntry online = entry("INT9999", "GV3", "Teacher 3", "Online", "LMS", List.of(s1));

        List<ConflictResult.RoomConflict> conflicts = service.detectRoomConflicts(List.of(a, b, online));
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getRoom()).isEqualTo("401");
        assertThat(conflicts.get(0).getConflictingSchedules()).extracting(ScheduleEntry::getSubjectCode)
                .containsExactlyInAnyOrder("INT1001", "INT1002");
    }

    @Test
    @DisplayName("LL-SCD-02: Detect teacher conflict and remove duplicates")
    void detectTeacherConflicts_shouldDetectAndRemoveDuplicates() {
        ScheduleEntry.TimeSlot s1 = slot("1", "Thứ 3", "2", "4", "2");

        // Duplicate same subject-room-teacher (should be deduped -> no conflict with itself)
        ScheduleEntry d1 = entry("INT1001", "GV1", "Teacher 1", "402", "A2", List.of(s1));
        ScheduleEntry d2 = entry("INT1001", "GV1", "Teacher 1", "402", "A2", List.of(s1));

        // Real conflict: same teacher/time but different subject/room
        ScheduleEntry c1 = entry("INT2001", "GV1", "Teacher 1", "402", "A2", List.of(s1));
        ScheduleEntry c2 = entry("INT2002", "GV1", "Teacher 1", "403", "A2", List.of(s1));

        List<ConflictResult.TeacherConflict> conflicts = service.detectTeacherConflicts(List.of(d1, d2, c1, c2));
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getTeacherId()).isEqualTo("GV1");
        assertThat(conflicts.get(0).getConflictingSchedules()).extracting(ScheduleEntry::getSubjectCode)
                .containsExactlyInAnyOrder("INT1001", "INT2001", "INT2002");
    }

    @Test
    @DisplayName("LL-SCD-03: Group conflicts by pattern across weeks")
    void detectConflicts_shouldGroupByPatternAcrossWeeks() {
        ScheduleEntry.TimeSlot w1 = slot("1", "Thứ 4", "1", "1", "3");
        ScheduleEntry.TimeSlot w2 = slot("2", "Thứ 4", "1", "1", "3");

        ScheduleEntry a1 = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(w1));
        ScheduleEntry b1 = entry("INT1002", "GV2", "Teacher 2", "401", "A1", List.of(w1));

        ScheduleEntry a2 = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(w2));
        ScheduleEntry b2 = entry("INT1002", "GV2", "Teacher 2", "401", "A1", List.of(w2));

        ConflictResult result = service.detectConflicts(List.of(a1, b1, a2, b2));
        assertThat(result.getRoomConflicts()).hasSize(1);
        ConflictResult.RoomConflict grouped = result.getRoomConflicts().get(0);
        assertThat(grouped.getConflictWeeks()).containsExactlyInAnyOrder("1", "2");
        // schedules are merged across weeks (keep distinct entries)
        assertThat(grouped.getConflictingSchedules()).hasSize(4);
    }

    @Test
    @DisplayName("LL-SCD-04: Create representative timeslot from slotKey")
    void detectRoomConflicts_shouldCreateRepresentativeTimeSlot() {
        // SlotKey structure: "Tuần 1-Thứ 5-1-1-2"
        ScheduleEntry.TimeSlot t = ScheduleEntry.TimeSlot.builder()
                .date("Tuần 1")
                .dayOfWeek("Thứ 5")
                .shift("1")
                .startPeriod("1")
                .numberOfPeriods("2")
                .build();

        ScheduleEntry a = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(t));
        ScheduleEntry b = entry("INT1002", "GV2", "Teacher 2", "401", "A1", List.of(t));

        List<ConflictResult.RoomConflict> conflicts = service.detectRoomConflicts(List.of(a, b));
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getTimeSlot().getDate()).isEqualTo("Tuần 1");
        assertThat(conflicts.get(0).getTimeSlot().getDayOfWeek()).isEqualTo("Thứ 5");
        assertThat(conflicts.get(0).getTimeSlot().getNumberOfPeriods()).isEqualTo("2");
    }

    @Test
    @DisplayName("LL-SCD-05: Group teacher conflicts by pattern across weeks")
    void detectConflicts_shouldGroupTeacherConflictsAcrossWeeks() {
        ScheduleEntry.TimeSlot w1 = slot("1", "Thứ 6", "1", "1", "2");
        ScheduleEntry.TimeSlot w2 = slot("2", "Thứ 6", "1", "1", "2");

        ScheduleEntry a1 = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(w1));
        ScheduleEntry b1 = entry("INT1002", "GV1", "Teacher 1", "402", "A1", List.of(w1));

        ScheduleEntry a2 = entry("INT1001", "GV1", "Teacher 1", "401", "A1", List.of(w2));
        ScheduleEntry b2 = entry("INT1002", "GV1", "Teacher 1", "402", "A1", List.of(w2));

        ConflictResult result = service.detectConflicts(List.of(a1, b1, a2, b2));
        assertThat(result.getTeacherConflicts()).hasSize(1);
        ConflictResult.TeacherConflict grouped = result.getTeacherConflicts().get(0);
        assertThat(grouped.getConflictWeeks()).containsExactlyInAnyOrder("1", "2");
        assertThat(grouped.getConflictingSchedules()).hasSize(4);
    }
}

