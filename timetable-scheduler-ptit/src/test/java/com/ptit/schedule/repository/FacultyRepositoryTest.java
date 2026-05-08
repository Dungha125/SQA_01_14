package com.ptit.schedule.repository;

import com.ptit.schedule.entity.Faculty;
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
@DisplayName("Test Suite FacultyRepository - Kiểm thử Repository Khoa")
class FacultyRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(FacultyRepositoryTest.class);

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Faculty testFaculty;
    private String facultyId;

    @BeforeEach
    void setUp() {
        facultyId = UUID.randomUUID().toString();
        testFaculty = new Faculty();
        testFaculty.setId(facultyId);
        testFaculty.setFacultyName("Công nghệ thông tin");
    }

    @Test
    @DisplayName("DT064 - Save faculty successfully")
    void testSaveFacultySuccess() {
        // Arrange
        logger.info("TC001 - Input: facultyId={}, facultyName={}", 
            testFaculty.getId(), testFaculty.getFacultyName());
        
        // Act
        Faculty saved = facultyRepository.save(testFaculty);
        entityManager.flush();

        // Assert
        logger.info("TC001 - Output: savedId={}, savedName={}", 
            saved.getId(), saved.getFacultyName());
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(facultyId);
        assertThat(saved.getFacultyName()).isEqualTo("Công nghệ thông tin");
    }

    @Test
    @DisplayName("DT065 - Find faculty by ID")
    void testFindByIdSuccess() {
        // Arrange
        facultyRepository.save(testFaculty);
        entityManager.flush();
        logger.info("TC002 - Input: facultyId={}", facultyId);

        // Act
        Optional<Faculty> found = facultyRepository.findById(facultyId);

        // Assert
        logger.info("TC002 - Output: found={}, facultyName={}", 
            found.isPresent(), found.map(Faculty::getFacultyName).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getFacultyName()).isEqualTo("Công nghệ thông tin");
    }

    @Test
    @DisplayName("DT066 - Find faculty by ID not found")
    void testFindByIdNotFound() {
        // Arrange
        logger.info("TC003 - Input: facultyId={}", "nonexistent");
        
        // Act
        Optional<Faculty> found = facultyRepository.findById("nonexistent");

        // Assert
        logger.info("TC003 - Output: found={}", found.isEmpty());
        
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("DT067 - Find faculties by name containing")
    void testFindByFacultyNameContaining() {
        // Arrange
        Faculty faculty2 = new Faculty();
        faculty2.setId(UUID.randomUUID().toString());
        faculty2.setFacultyName("Điện tử Viễn thông");

        facultyRepository.save(testFaculty);
        facultyRepository.save(faculty2);
        entityManager.flush();
        logger.info("TC004 - Input: searchKeyword={}", "công");

        // Act
        List<Faculty> results = facultyRepository.findByFacultyNameContainingIgnoreCase("công");

        // Assert
        logger.info("TC004 - Output: resultCount={}, firstResult={}", 
            results.size(), results.isEmpty() ? null : results.get(0).getFacultyName());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFacultyName()).isEqualTo("Công nghệ thông tin");
    }


}

