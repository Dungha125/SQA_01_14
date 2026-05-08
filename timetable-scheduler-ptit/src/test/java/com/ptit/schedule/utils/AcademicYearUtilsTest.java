package com.ptit.schedule.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.modelmapper.internal.Pair;

import java.time.LocalDate;

import static com.ptit.schedule.utils.AcademicYearUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class AcademicYearUtilsTest {

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("\n=== TEST: " + testInfo.getDisplayName() + " ===");
    }

    @Test
    @DisplayName("LL-115: Trả về năm học được cung cấp khi hợp lệ")
    void resolveAcademicYear_shouldReturnProvidedYearWhenValid() {
        String input = "2024-2025";
        String result = resolveAcademicYear(input);
        System.out.println("INPUT: academicYear=" + input);
        System.out.println("OUTPUT: result=" + result);
        assertThat(result).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-116: Trả về năm học hiện tại khi null")
    void resolveAcademicYear_shouldReturnCurrentYearWhenNull() {
        String input = null;
        String result = resolveAcademicYear(input);
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int startYear = (month >= 8) ? year : year - 1;
        String expected = startYear + "-" + (startYear + 1);
        System.out.println("INPUT: academicYear=null");
        System.out.println("OUTPUT: result=" + result);
        System.out.println("EXPECTED: " + expected);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-117: Trả về năm học hiện tại khi chuỗi rỗng")
    void resolveAcademicYear_shouldReturnCurrentYearWhenEmpty() {
        String input = "";
        String result = resolveAcademicYear(input);
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int startYear = (month >= 8) ? year : year - 1;
        String expected = startYear + "-" + (startYear + 1);
        System.out.println("INPUT: academicYear=\"\"");
        System.out.println("OUTPUT: result=" + result);
        System.out.println("EXPECTED: " + expected);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-118: Trả về năm học hiện tại khi chỉ có khoảng trắng")
    void resolveAcademicYear_shouldReturnCurrentYearWhenOnlyWhitespace() {
        String input = "   ";
        String result = resolveAcademicYear(input);
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int startYear = (month >= 8) ? year : year - 1;
        String expected = startYear + "-" + (startYear + 1);
        System.out.println("INPUT: academicYear=\"" + input + "\"");
        System.out.println("OUTPUT: result=" + result);
        System.out.println("EXPECTED: " + expected);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("LL-119: Từ tháng 8 trở đi: năm học bắt đầu từ năm hiện tại")
    void resolveAcademicYear_shouldStartFromAugust() {
        // Test for August (month 8) - should use current year
        LocalDate augustDate = LocalDate.of(2025, 8, 15);
        String result = AcademicYearUtils.resolveAcademicYearForDate(augustDate);
        System.out.println("INPUT: date=" + augustDate);
        System.out.println("OUTPUT: result=" + result);
        System.out.println("EXPECTED: 2025-2026");
        assertThat(result).isEqualTo("2025-2026");
    }

    @Test
    @DisplayName("LL-120: Tháng 7: năm học bắt đầu từ năm trước")
    void resolveAcademicYear_shouldStartFromJuly() {
        // Test for July (month 7) - should use previous year
        LocalDate julyDate = LocalDate.of(2025, 7, 15);
        String result = AcademicYearUtils.resolveAcademicYearForDate(julyDate);
        System.out.println("INPUT: date=" + julyDate);
        System.out.println("OUTPUT: result=" + result);
        System.out.println("EXPECTED: 2024-2025");
        assertThat(result).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-121: Tách học kỳ & năm học: trả về null khi input null")
    void splitSemesterAndYear_shouldReturnNullWhenInputIsNull() {
        String input = null;
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=null");
        System.out.println("OUTPUT: result=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-122: Tách học kỳ & năm học: xử lý dấu gạch nối en-dash")
    void splitSemesterAndYear_shouldHandleEnDash() {
        String input = "HK1–2024-2025";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: semester=" + (result == null ? null : result.getLeft())
                + ", academicYear=" + (result == null ? null : result.getRight()));
        assertThat(result).isNotNull();
        assertThat(result.getLeft()).isEqualTo("HK1");
        assertThat(result.getRight()).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-123: Tách học kỳ & năm học: xử lý dấu gạch nối thường")
    void splitSemesterAndYear_shouldHandleRegularDash() {
        String input = "HK1-2024-2025";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: semester=" + (result == null ? null : result.getLeft())
                + ", academicYear=" + (result == null ? null : result.getRight()));
        assertThat(result).isNotNull();
        assertThat(result.getLeft()).isEqualTo("HK1");
        assertThat(result.getRight()).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-124: Tách học kỳ & năm học: xử lý khoảng trắng quanh dấu gạch nối")
    void splitSemesterAndYear_shouldHandleSpacesAroundDash() {
        String input = "HK1  -  2024-2025";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: semester=" + (result == null ? null : result.getLeft())
                + ", academicYear=" + (result == null ? null : result.getRight()));
        assertThat(result).isNotNull();
        assertThat(result.getLeft()).isEqualTo("HK1");
        assertThat(result.getRight()).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-125: Tách học kỳ & năm học: hỗ trợ HK2")
    void splitSemesterAndYear_shouldHandleHK2() {
        String input = "HK2-2024-2025";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: semester=" + (result == null ? null : result.getLeft())
                + ", academicYear=" + (result == null ? null : result.getRight()));
        assertThat(result).isNotNull();
        assertThat(result.getLeft()).isEqualTo("HK2");
        assertThat(result.getRight()).isEqualTo("2024-2025");
    }

    @Test
    @DisplayName("LL-126: Tách học kỳ & năm học: trả về null khi format sai")
    void splitSemesterAndYear_shouldReturnNullForInvalidFormat() {
        String input = "invalid";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: result=" + result);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("LL-127: Tách học kỳ & năm học: tự động trim học kỳ và năm học")
    void splitSemesterAndYear_shouldTrimSemesterAndYear() {
        String input = "  HK1  -  2024-2025  ";
        Pair<String, String> result = splitSemesterAndYear(input);
        System.out.println("INPUT: input=" + input);
        System.out.println("OUTPUT: semester=" + (result == null ? null : result.getLeft())
                + ", academicYear=" + (result == null ? null : result.getRight()));
        assertThat(result).isNotNull();
        assertThat(result.getLeft()).isEqualTo("HK1");
        assertThat(result.getRight()).isEqualTo("2024-2025");
    }
}


