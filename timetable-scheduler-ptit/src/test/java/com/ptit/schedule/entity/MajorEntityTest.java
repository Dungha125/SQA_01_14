package com.ptit.schedule.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Test Suite Major Entity - Kiểm thử Entity Ngành")
class MajorEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(MajorEntityTest.class);

    private Major testMajor;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testFaculty = new Faculty();
        testFaculty.setId("CNTT001");
        testFaculty.setFacultyName("Công nghệ thông tin");

        testMajor = Major.builder()
                .id(1L)
                .majorCode("KA2021")
                .classYear("2021")
                .majorName("Khóa 2021")
                .numberOfStudents(100)
                .faculty(testFaculty)
                .build();
    }

    @Test
    @DisplayName("DT080 - Tạo ngành bằng builder")
    void testMajorCreation() {
        logger.info("TC001 - Input: id={}, majorCode={}, classYear={}, majorName={}, numberOfStudents={}", 
            testMajor.getId(), testMajor.getMajorCode(), testMajor.getClassYear(), 
            testMajor.getMajorName(), testMajor.getNumberOfStudents());
        
        logger.info("TC001 - Output: majorNotNull={}, id={}, code={}, faculty={}", 
            testMajor != null, testMajor.getId(), testMajor.getMajorCode(), 
            testMajor.getFaculty() != null ? testMajor.getFaculty().getId() : null);
        
        assertThat(testMajor).isNotNull();
        assertThat(testMajor.getId()).isEqualTo(1L);
        assertThat(testMajor.getMajorCode()).isEqualTo("KA2021");
        assertThat(testMajor.getClassYear()).isEqualTo("2021");
        assertThat(testMajor.getMajorName()).isEqualTo("Khóa 2021");
        assertThat(testMajor.getNumberOfStudents()).isEqualTo(100);
        assertThat(testMajor.getFaculty()).isEqualTo(testFaculty);
    }

    @Test
    @DisplayName("DT081 - Thiết lập và lấy mã ngành")
    void testSetAndGetMajorCode() {
        logger.info("TC002 - Input: newMajorCode={}", "KA2022");
        
        testMajor.setMajorCode("KA2022");
        
        logger.info("TC002 - Output: majorCode={}", testMajor.getMajorCode());
        
        assertThat(testMajor.getMajorCode()).isEqualTo("KA2022");
    }

    @Test
    @DisplayName("DT082 - Thiết lập và lấy năm khóa")
    void testSetAndGetClassYear() {
        logger.info("TC003 - Input: newClassYear={}", "2022");
        
        testMajor.setClassYear("2022");
        
        logger.info("TC003 - Output: classYear={}", testMajor.getClassYear());
        
        assertThat(testMajor.getClassYear()).isEqualTo("2022");
    }

    @Test
    @DisplayName("DT083 - Thiết lập và lấy số lượng sinh viên")
    void testSetAndGetNumberOfStudents() {
        logger.info("TC004 - Input: newNumberOfStudents={}", 150);
        
        testMajor.setNumberOfStudents(150);
        
        logger.info("TC004 - Output: numberOfStudents={}", testMajor.getNumberOfStudents());
        
        assertThat(testMajor.getNumberOfStudents()).isEqualTo(150);
    }

    @Test
    @DisplayName("DT084 - Thiết lập và lấy khoa")
    void testSetAndGetFaculty() {
        Faculty newFaculty = new Faculty();
        newFaculty.setId("DTVT002");
        newFaculty.setFacultyName("Điện tử");
        
        logger.info("TC005 - Input: newFacultyId={}, newFacultyName={}", "DTVT002", "Điện tử");
        
        testMajor.setFaculty(newFaculty);
        
        logger.info("TC005 - Output: facultyId={}, facultyName={}", 
            testMajor.getFaculty().getId(), testMajor.getFaculty().getFacultyName());
        
        assertThat(testMajor.getFaculty()).isEqualTo(newFaculty);
    }

    @Test
    @DisplayName("DT085 - Ngành với số lượng sinh viên tối thiểu")
    void testMajorWithMinimumStudents() {
        logger.info("TC006 - Input: minNumberOfStudents={}", 1);
        
        testMajor.setNumberOfStudents(1);
        
        logger.info("TC006 - Output: numberOfStudents={}", testMajor.getNumberOfStudents());
        
        assertThat(testMajor.getNumberOfStudents()).isEqualTo(1);
    }

    @Test
    @DisplayName("DT086 - Ngành với số lượng sinh viên tối đa")
    void testMajorWithMaximumStudents() {
        logger.info("TC007 - Input: maxNumberOfStudents={}", 1000);
        
        testMajor.setNumberOfStudents(1000);
        
        logger.info("TC007 - Output: numberOfStudents={}", testMajor.getNumberOfStudents());
        
        assertThat(testMajor.getNumberOfStudents()).isEqualTo(1000);
    }

    @Test
    @DisplayName("DT087 - Tên ngành có thể là null")
    void testMajorNameCanBeNull() {
        logger.info("TC008 - Input: majorName={}", (Object) null);
        
        testMajor.setMajorName(null);
        
        logger.info("TC008 - Output: majorName={}", testMajor.getMajorName());
        
        assertThat(testMajor.getMajorName()).isNull();
    }

    @Test
    @DisplayName("DT088 - Chuyển ngành sang builder và quay lại")
    void testMajorToBuilderAndBack() {
        logger.info("TC009 - Input: original={}, code={}", 
            testMajor.getId(), testMajor.getMajorCode());
        
        Major copiedMajor = testMajor.toBuilder().build();
        
        logger.info("TC009 - Output: copiedCode={}, copiedStudents={}", 
            copiedMajor.getMajorCode(), copiedMajor.getNumberOfStudents());
        
        assertThat(copiedMajor.getMajorCode()).isEqualTo(testMajor.getMajorCode());
        assertThat(copiedMajor.getNumberOfStudents()).isEqualTo(testMajor.getNumberOfStudents());
    }

    @Test
    @DisplayName("DT089 - Ngành với mã dài")
    void testMajorWithLongCode() {
        logger.info("TC010 - Input: longMajorCode={}", "KNTT-CNTT-PTIT-2021-01");
        
        testMajor.setMajorCode("KNTT-CNTT-PTIT-2021-01");
        
        logger.info("TC010 - Output: majorCode={}", testMajor.getMajorCode());
        
        assertThat(testMajor.getMajorCode()).isEqualTo("KNTT-CNTT-PTIT-2021-01");
    }
}

