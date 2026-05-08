package com.ptit.schedule.repository;

import com.ptit.schedule.entity.MajorBuildingPreference;
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

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Test Suite MajorBuildingPreferenceRepository - Kiểm thử Repository Ưu tiên Tòa nhà Ngành")
class MajorBuildingPreferenceRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(MajorBuildingPreferenceRepositoryTest.class);

    @Autowired
    private MajorBuildingPreferenceRepository preferenceRepository;

    @Autowired
    private TestEntityManager entityManager;

    private MajorBuildingPreference testPreference;

    @BeforeEach
    void setUp() {
        testPreference = new MajorBuildingPreference();
        testPreference.setNganh("KA2021");
        testPreference.setPreferredBuilding("A1");
        testPreference.setPriorityLevel(1);
        testPreference.setIsActive(true);
        testPreference.setNotes("Tòa ưu tiên 1");
    }

    @Test
    @DisplayName("DT118 - Lưu ưu tiên thành công")
    void testSavePreferenceSuccess() {
        // Arrange
        logger.info("TC001 - Input: nganh={}, preferredBuilding={}, priorityLevel={}, isActive={}", 
            testPreference.getNganh(), testPreference.getPreferredBuilding(), 
            testPreference.getPriorityLevel(), testPreference.getIsActive());
        
        // Act
        MajorBuildingPreference saved = preferenceRepository.save(testPreference);
        entityManager.flush();

        // Assert
        logger.info("TC001 - Output: savedId={}, nganh={}, building={}", 
            saved.getId(), saved.getNganh(), saved.getPreferredBuilding());
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNganh()).isEqualTo("KA2021");
        assertThat(saved.getPreferredBuilding()).isEqualTo("A1");
    }

    @Test
    @DisplayName("DT119 - Tìm ưu tiên theo ngành và tòa nhà")
    void testFindByNganhAndBuilding() {
        // Arrange
        preferenceRepository.save(testPreference);
        entityManager.flush();
        logger.info("TC002 - Input: nganh={}, preferredBuilding={}", "KA2021", "A1");

        // Act
        Optional<MajorBuildingPreference> found = 
                preferenceRepository.findByNganhAndPreferredBuildingAndIsActiveTrue("KA2021", "A1");

        // Assert
        logger.info("TC002 - Output: found={}, nganh={}", 
            found.isPresent(), found.map(MajorBuildingPreference::getNganh).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getNganh()).isEqualTo("KA2021");
    }

    @Test
    @DisplayName("DT120 - Tìm ưu tiên hoạt động theo ngành")
    void testFindByNganhAndIsActiveTrue() {
        // Arrange
        preferenceRepository.save(testPreference);
        
        MajorBuildingPreference pref2 = new MajorBuildingPreference();
        pref2.setNganh("KA2021");
        pref2.setPreferredBuilding("A2");
        pref2.setPriorityLevel(2);
        pref2.setIsActive(true);
        preferenceRepository.save(pref2);
        
        entityManager.flush();
        logger.info("TC003 - Input: nganh={}, isActive={}", "KA2021", true);

        // Act
        List<MajorBuildingPreference> results = 
                preferenceRepository.findByNganhAndIsActiveTrueOrderByPriorityLevelAsc("KA2021");

        // Assert
        logger.info("TC003 - Output: resultCount={}, firstPriority={}, secondPriority={}", 
            results.size(), 
            results.isEmpty() ? null : results.get(0).getPriorityLevel(),
            results.size() > 1 ? results.get(1).getPriorityLevel() : null);
        
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getPriorityLevel()).isEqualTo(1);
        assertThat(results.get(1).getPriorityLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("DT121 - Tìm tất cả ưu tiên hoạt động sắp xếp")
    void testFindAllActivePreferences() {
        // Arrange
        preferenceRepository.save(testPreference);
        entityManager.flush();
        logger.info("TC004 - Input: findByIsActiveTrueOrderByNganhAscPriorityLevelAsc()");

        // Act
        List<MajorBuildingPreference> results = 
                preferenceRepository.findByIsActiveTrueOrderByNganhAscPriorityLevelAsc();

        // Assert
        logger.info("TC004 - Output: resultCount={}, firstActive={}", 
            results.size(), results.isEmpty() ? null : results.get(0).getIsActive());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("DT122 - Tìm các ngành hoạt động khác nhau")
    void testFindDistinctActiveMajors() {
        // Arrange
        preferenceRepository.save(testPreference);
        
        MajorBuildingPreference pref2 = new MajorBuildingPreference();
        pref2.setNganh("KA2022");
        pref2.setPreferredBuilding("B1");
        pref2.setPriorityLevel(1);
        pref2.setIsActive(true);
        preferenceRepository.save(pref2);
        
        entityManager.flush();
        logger.info("TC005 - Input: findDistinctActiveMajors()");

        // Act
        List<String> majors = preferenceRepository.findDistinctActiveMajors();

        // Assert
        logger.info("TC005 - Output: resultCount={}, majors={}", majors.size(), majors);
        
        assertThat(majors).hasSize(2);
        assertThat(majors).contains("KA2021", "KA2022");
    }

    @Test
    @DisplayName("DT123 - Ưu tiên không hoạt động không được bao gồm trong kết quả hoạt động")
    void testInactivePreferenceExcluded() {
        // Arrange
        testPreference.setIsActive(false);
        preferenceRepository.save(testPreference);
        entityManager.flush();
        logger.info("TC006 - Input: nganh={}, preferredBuilding={}, isActive={}", 
            "KA2021", "A1", false);

        // Act
        Optional<MajorBuildingPreference> found = 
                preferenceRepository.findByNganhAndPreferredBuildingAndIsActiveTrue("KA2021", "A1");

        // Assert
        logger.info("TC006 - Output: found={}", found.isEmpty());
        
        assertThat(found).isEmpty();
    }
}

