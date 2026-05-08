package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.MajorResponse;
import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.entity.Major;
import com.ptit.schedule.repository.MajorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite MajorServiceImpl - Kiểm thử Service Ngành")
class MajorServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(MajorServiceImplTest.class);

    @Mock
    private MajorRepository majorRepository;

    @InjectMocks
    private MajorServiceImpl majorService;

    private Major testMajor1;
    private Major testMajor2;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        testFaculty = new Faculty();
        testFaculty.setId(UUID.randomUUID().toString());
        testFaculty.setFacultyName("Công nghệ thông tin");

        testMajor1 = Major.builder()
                .id(1L)
                .majorCode("KA2021")
                .classYear("2021")
                .majorName("Khóa 2021")
                .numberOfStudents(100)
                .faculty(testFaculty)
                .build();

        testMajor2 = Major.builder()
                .id(2L)
                .majorCode("KA2022")
                .classYear("2022")
                .majorName("Khóa 2022")
                .numberOfStudents(120)
                .faculty(testFaculty)
                .build();
    }

    @Test
    @DisplayName("DT021 - Get all majors successfully")
    void testGetAllMajorsSuccess() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1, testMajor2);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC001 - Input: getAllMajors()");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC001 - Output: resultCount={}, firstMajorCode={}, secondMajorCode={}", 
            responses.size(), responses.get(0).getMajorCode(), responses.get(1).getMajorCode());
        
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getMajorCode()).isEqualTo("KA2021");
        assertThat(responses.get(1).getMajorCode()).isEqualTo("KA2022");
        verify(majorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("DT022 - Get all majors when empty")
    void testGetAllMajorsEmpty() {
        // Arrange
        when(majorRepository.findAll()).thenReturn(Arrays.asList());
        logger.info("TC002 - Input: getAllMajors() with empty result");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC002 - Output: resultCount={}", responses.size());
        
        assertThat(responses).isEmpty();
        verify(majorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("DT023 - Get all majors with multiple entries")
    void testGetAllMajorsMultiple() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1, testMajor2);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC003 - Input: getAllMajors() with multiple entries");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC003 - Output: resultCount={}", responses.size());
        
        assertThat(responses).hasSize(2);
        assertThat(responses).allSatisfy(response -> {
            assertThat(response.getMajorCode()).isNotNull();
            assertThat(response.getClassYear()).isNotNull();
            assertThat(response.getNumberOfStudents()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("DT024 - Get majors returns correct data")
    void testGetMajorsReturnCorrectData() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC004 - Input: getAllMajors() to verify data correctness");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC004 - Output: numberOfStudents={}, majorName={}", 
            responses.get(0).getNumberOfStudents(), responses.get(0).getMajorName());
        
        MajorResponse response = responses.get(0);
        assertThat(response.getNumberOfStudents()).isEqualTo(100);
        assertThat(response.getMajorName()).isEqualTo("Khóa 2021");
    }

    @Test
    @DisplayName("DT025 - Get majors with large student count")
    void testGetMajorsWithLargeStudentCount() {
        // Arrange
        Major largeMajor = testMajor1.toBuilder().numberOfStudents(1000).build();
        List<Major> majors = Arrays.asList(largeMajor);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC005 - Input: getAllMajors() with numberOfStudents={}", 1000);

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC005 - Output: numberOfStudents={}", responses.get(0).getNumberOfStudents());
        
        assertThat(responses.get(0).getNumberOfStudents()).isEqualTo(1000);
    }

    @Test
    @DisplayName("DT026 - Get majors with different class years")
    void testGetMajorsWithDifferentClassYears() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1, testMajor2);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC006 - Input: getAllMajors() with different classYears");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC006 - Output: classYears={}", 
            responses.stream().map(MajorResponse::getClassYear).toArray());
        
        assertThat(responses)
                .extracting("classYear")
                .containsExactly("2021", "2022");
    }

    @Test
    @DisplayName("DT027 - Get majors verifies all properties")
    void testGetMajorsVerifiesAllProperties() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC007 - Input: getAllMajors() to verify all properties");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC007 - Output: majorCode={}, classYear={}, numberOfStudents={}", 
            responses.get(0).getMajorCode(), responses.get(0).getClassYear(), 
            responses.get(0).getNumberOfStudents());
        
        MajorResponse response = responses.get(0);
        assertThat(response)
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(1L);
                    assertThat(r.getMajorCode()).isEqualTo("KA2021");
                    assertThat(r.getClassYear()).isEqualTo("2021");
                    assertThat(r.getMajorName()).isEqualTo("Khóa 2021");
                    assertThat(r.getNumberOfStudents()).isEqualTo(100);
                });
    }

    @Test
    @DisplayName("DT028 - Get majors calls repository once")
    void testGetMajorsCallsRepositoryOnce() {
        // Arrange
        when(majorRepository.findAll()).thenReturn(Arrays.asList());
        logger.info("TC008 - Input: getAllMajors() to verify repository call count");

        // Act
        majorService.getAllMajors();

        // Assert
        logger.info("TC008 - Output: repositoryCallCount={}", 1);
        
        verify(majorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("DT029 - Get majors with special characters in name")
    void testGetMajorsWithSpecialCharacters() {
        // Arrange
        Major specialMajor = testMajor1.toBuilder()
                .majorName("Khóa K2021 - Hệ thống")
                .build();
        when(majorRepository.findAll()).thenReturn(Arrays.asList(specialMajor));
        logger.info("TC009 - Input: getAllMajors() with majorName={}", "Khóa K2021 - Hệ thống");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC009 - Output: majorName={}, containsSpecialChars={}", 
            responses.get(0).getMajorName(), responses.get(0).getMajorName().contains("-"));
        
        assertThat(responses.get(0).getMajorName()).contains("Khóa");
        assertThat(responses.get(0).getMajorName()).contains("Hệ thống");
    }

    @Test
    @DisplayName("DT030 - Get majors preserves order")
    void testGetMajorsPreservesOrder() {
        // Arrange
        List<Major> majors = Arrays.asList(testMajor1, testMajor2);
        when(majorRepository.findAll()).thenReturn(majors);
        logger.info("TC010 - Input: getAllMajors() to verify order preservation");

        // Act
        List<MajorResponse> responses = majorService.getAllMajors();

        // Assert
        logger.info("TC010 - Output: majorCodes={}", 
            responses.stream().map(MajorResponse::getMajorCode).toArray());
        
        assertThat(responses)
                .extracting("majorCode")
                .containsExactly("KA2021", "KA2022");
    }
}

