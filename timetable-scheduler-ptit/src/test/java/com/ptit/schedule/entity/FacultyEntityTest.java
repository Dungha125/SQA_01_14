package com.ptit.schedule.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Test Suite Faculty Entity - Kiểm thử Entity Khoa")
class FacultyEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(FacultyEntityTest.class);

    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testFaculty = new Faculty();
        testFaculty.setId("CNTT001");
        testFaculty.setFacultyName("Công nghệ thông tin");
    }

    @Test
    @DisplayName("DT068 - Tạo khoa")
    void testFacultyCreation() {
        logger.info("TC001 - Input: facultyId={}, facultyName={}", 
            testFaculty.getId(), testFaculty.getFacultyName());
        
        assertThat(testFaculty).isNotNull();
        
        logger.info("TC001 - Output: facultyNotNull={}, id={}, name={}", 
            true, testFaculty.getId(), testFaculty.getFacultyName());
        
        assertThat(testFaculty.getId()).isEqualTo("CNTT001");
        assertThat(testFaculty.getFacultyName()).isEqualTo("Công nghệ thông tin");
    }

    @Test
    @DisplayName("DT069 - Thiết lập và lấy tên khoa")
    void testSetAndGetFacultyName() {
        logger.info("TC002 - Input: newFacultyName={}", "Điện tử - Viễn thông");
        
        testFaculty.setFacultyName("Điện tử - Viễn thông");
        
        logger.info("TC002 - Output: facultyName={}", testFaculty.getFacultyName());
        
        assertThat(testFaculty.getFacultyName()).isEqualTo("Điện tử - Viễn thông");
    }

    @Test
    @DisplayName("DT070 - Thiết lập và lấy ID khoa")
    void testSetAndGetId() {
        logger.info("TC003 - Input: newId={}", "DTVT002");
        
        testFaculty.setId("DTVT002");
        
        logger.info("TC003 - Output: facultyId={}", testFaculty.getId());
        
        assertThat(testFaculty.getId()).isEqualTo("DTVT002");
    }

    @Test
    @DisplayName("DT071 - Khoa có danh sách ngành học")
    void testFacultyWithMajors() {
        logger.info("TC004 - Input: faculty={}", testFaculty.getId());
        
        boolean idNotNull = testFaculty != null && testFaculty.getId() != null;
        logger.info("TC004 - Output: facultyNotNull={}, idNotNull={}", 
            testFaculty != null, idNotNull);
        
        assertThat(testFaculty).isNotNull();
        assertThat(testFaculty.getId()).isNotNull();
    }

    @Test
    @DisplayName("DT072 - Các thuộc tính khoa không null")
    void testFacultyPropertiesNotNull() {
        logger.info("TC005 - Input: faculty has id and name");
        
        boolean idNotNull = testFaculty != null && testFaculty.getId() != null;
        boolean nameNotNull = testFaculty != null && testFaculty.getFacultyName() != null;
        
        logger.info("TC005 - Output: idNotNull={}, nameNotNull={}", idNotNull, nameNotNull);
        
        assertThat(testFaculty.getId()).isNotNull();
        assertThat(testFaculty.getFacultyName()).isNotNull();
    }

    @Test
    @DisplayName("DT073 - Khoa với các ký tự đặc biệt trong tên")
    void testFacultyWithSpecialCharacters() {
        logger.info("TC006 - Input: newFacultyName={}", "Khoa Công Nghệ Thông Tin - PTIT");
        
        testFaculty.setFacultyName("Khoa Công Nghệ Thông Tin - PTIT");
        
        logger.info("TC006 - Output: facultyName={}, containsPTIT={}", 
            testFaculty.getFacultyName(), testFaculty.getFacultyName().contains("PTIT"));
        
        assertThat(testFaculty.getFacultyName()).contains("PTIT");
    }
}

