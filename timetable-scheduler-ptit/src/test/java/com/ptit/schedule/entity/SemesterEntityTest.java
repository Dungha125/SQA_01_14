package com.ptit.schedule.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho entity Semester (Module Học Kỳ).
 */
public class SemesterEntityTest {

    @Test
    void testSemesterEntity_BuilderValid() {
        // HK-11: Mục đích: Khởi tạo Entity bằng file Builder và match thông tin học kỳ.
        Semester semester = Semester.builder()
                .id(1L)
                .semesterName("Học kỳ 1")
                .academicYear("2024-2025")
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .isActive(true)
                .description("Học kỳ mùa thu")
                .build();

        assertEquals(1L, semester.getId());
        assertEquals("Học kỳ 1", semester.getSemesterName());
        assertTrue(semester.getIsActive());
    }

    @Test
    void testSemesterEntity_DefaultConstructor() {
        // HK-12: Mục đích: Tạo bằng constructor Default không lỗi.
        Semester sem = new Semester();
        assertNotNull(sem);
    }

    @Test
    void testSemesterEntity_SetAcademicYear() {
        // HK-13: Mục đích: Đổi năm học thông qua setter.
        Semester sem = new Semester();
        sem.setAcademicYear("2025-2026");
        assertEquals("2025-2026", sem.getAcademicYear());
    }

    @Test
    void testSemesterEntity_SetDescription() {
        // HK-14: Mục đích: Thiết lập ghi chú mô tả.
        Semester sem = new Semester();
        sem.setDescription("Test DESC");
        assertEquals("Test DESC", sem.getDescription());
    }

    @Test
    void testSemesterEntity_SetIsActive() {
        // HK-15: Mục đích: Đặt trạng thái Active là False.
        Semester sem = new Semester();
        sem.setIsActive(false);
        assertFalse(sem.getIsActive());
    }

    @Test
    void testSemesterEntity_SetStartDate() {
        // HK-16: Mục đích: Đặt start date.
        Semester sem = new Semester();
        LocalDate d = LocalDate.of(2024, 1, 1);
        sem.setStartDate(d);
        assertEquals(d, sem.getStartDate());
    }

    @Test
    void testSemesterEntity_SetEndDate() {
        // HK-17: Mục đích: Đặt end date.
        Semester sem = new Semester();
        LocalDate d = LocalDate.of(2024, 6, 1);
        sem.setEndDate(d);
        assertEquals(d, sem.getEndDate());
    }

    @Test
    void testSemesterEntity_SetId() {
        // HK-18: Mục đích: Đặt id là null.
        Semester sem = new Semester();
        sem.setId(null);
        assertNull(sem.getId());
    }

    @Test
    void testSemesterEntity_SetSemesterNameEmpty() {
        // HK-19: Mục đích: Đặt tên rỗng.
        Semester sem = new Semester();
        sem.setSemesterName("");
        assertEquals("", sem.getSemesterName());
    }

    @Test
    void testSemesterEntity_IsActiveNull() {
        // HK-20: Mục đích: Đặt isActive là rỗng.
        Semester sem = new Semester();
        sem.setIsActive(null);
        assertNull(sem.getIsActive());
    }
}
