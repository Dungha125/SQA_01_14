package com.ptit.schedule.repository;

import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.entity.Major;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Test Suite MajorRepository - Kiểm thử Repository Ngành")
class MajorRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(MajorRepositoryTest.class);

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Major testMajor;
    private Faculty testFaculty;
    private String facultyId;

    @BeforeEach
    void setUp() {
        facultyId = UUID.randomUUID().toString();
        testFaculty = new Faculty();
        testFaculty.setId(facultyId);
        testFaculty.setFacultyName("Công nghệ thông tin");

        testMajor = Major.builder()
                .majorCode("KA2021")
                .classYear("2021")
                .majorName("Khóa 2021")
                .numberOfStudents(100)
                .faculty(testFaculty)
                .build();
    }

    @Test
    @DisplayName("DT074 - Save major successfully")
    void testSaveMajorSuccess() {
        // Arrange
        facultyRepository.save(testFaculty);
        logger.info("TC001 - Input: majorCode={}, classYear={}, majorName={}, numberOfStudents={}", 
            testMajor.getMajorCode(), testMajor.getClassYear(), testMajor.getMajorName(), 
            testMajor.getNumberOfStudents());

        // Act
        Major saved = majorRepository.save(testMajor);
        entityManager.flush();

        // Assert
        logger.info("TC001 - Output: savedId={}, savedCode={}", saved.getId(), saved.getMajorCode());
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMajorCode()).isEqualTo("KA2021");
    }

    @Test
    @DisplayName("DT075 - Find major by ID")
    void testFindByIdSuccess() {
        // Arrange
        facultyRepository.save(testFaculty);
        Major saved = majorRepository.save(testMajor);
        entityManager.flush();
        logger.info("TC002 - Input: majorId={}", saved.getId());

        // Act
        Optional<Major> found = majorRepository.findById(saved.getId());

        // Assert
        logger.info("TC002 - Output: found={}, majorCode={}", 
            found.isPresent(), found.map(Major::getMajorCode).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getMajorCode()).isEqualTo("KA2021");
    }

    @Test
    @DisplayName("DT076 - Find major by code and class year")
    void testFindByMajorCodeAndClassYear() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        entityManager.flush();
        logger.info("TC003 - Input: majorCode={}, classYear={}", "KA2021", "2021");

        // Act
        Optional<Major> found = majorRepository.findByMajorCodeAndClassYear("KA2021", "2021");

        // Assert
        logger.info("TC003 - Output: found={}, majorName={}", 
            found.isPresent(), found.map(Major::getMajorName).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getMajorName()).isEqualTo("Khóa 2021");
    }

    @Test
    @DisplayName("DT077 - Find majors by faculty ID")
    void testFindByFacultyId() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        entityManager.flush();
        logger.info("TC004 - Input: facultyId={}", facultyId);

        // Act
        List<Major> results = majorRepository.findByFacultyId(facultyId);

        // Assert
        logger.info("TC004 - Output: resultCount={}, firstCode={}", 
            results.size(), results.isEmpty() ? null : results.get(0).getMajorCode());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMajorCode()).isEqualTo("KA2021");
    }

    @Test
    @DisplayName("DT078 - Find majors by non-existent faculty ID")
    void testFindByFacultyIdNotFound() {
        // Arrange
        logger.info("TC005 - Input: facultyId={}", "nonexistent");
        
        // Act
        List<Major> results = majorRepository.findByFacultyId("nonexistent");

        // Assert
        logger.info("TC005 - Output: resultCount={}", results.size());
        
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("DT079 - Find all majors")
    void testFindAllMajors() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        logger.info("TC006 - Input: saving major1={}, major2={}", "KA2021", "KA2022");
        
        Major major2 = Major.builder()
                .majorCode("KA2022")
                .classYear("2022")
                .majorName("Khóa 2022")
                .numberOfStudents(100)
                .faculty(testFaculty)
                .build();
        majorRepository.save(major2);
        entityManager.flush();

        // Act
        List<Major> results = majorRepository.findAll();

        // Assert
        logger.info("TC006 - Output: resultCount={}", results.size());
        
        assertThat(results).hasSize(2);
    }
}

