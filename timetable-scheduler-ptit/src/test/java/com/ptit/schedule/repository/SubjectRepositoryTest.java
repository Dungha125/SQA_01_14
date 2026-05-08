package com.ptit.schedule.repository;

import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.entity.Major;
import com.ptit.schedule.entity.Semester;
import com.ptit.schedule.entity.Subject;
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
@DisplayName("Test Suite SubjectRepository - Kiểm thử Repository Môn học")
class SubjectRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepositoryTest.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Subject testSubject;
    private Faculty testFaculty;
    private Major testMajor;
    private Semester testSemester;

    @BeforeEach
    void setUp() {
        testFaculty = new Faculty();
        testFaculty.setId(UUID.randomUUID().toString());
        testFaculty.setFacultyName("Công nghệ thông tin");

        testMajor = Major.builder()
                .majorCode("KA2021")
                .classYear("2021")
                .majorName("Khóa 2021")
                .numberOfStudents(100)
                .faculty(testFaculty)
                .build();

        testSemester = new Semester();
        testSemester.setSemesterName("Học kỳ 1");
        testSemester.setAcademicYear("2023-2024");

        testSubject = Subject.builder()
                .subjectCode("CS101")
                .subjectName("Nhập môn lập trình")
                .theoryHours(30)
                .exerciseHours(15)
                .projectHours(0)
                .labHours(15)
                .selfStudyHours(60)
                .credits(3)
                .numberOfClasses(2)
                .studentsPerClass(50)
                .department("Khoa CNTT")
                .examFormat("Thi viết")
                .programType("Chính quy")
                .major(testMajor)
                .semester(testSemester)
                .isCommon(false)
                .build();
    }

    @Test
    @DisplayName("DT090 - Save subject successfully")
    void testSaveSubjectSuccess() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        logger.info("TC001 - Input: subjectCode={}, subjectName={}, credits={}, numberOfClasses={}", 
            testSubject.getSubjectCode(), testSubject.getSubjectName(), 
            testSubject.getCredits(), testSubject.getNumberOfClasses());

        // Act
        Subject saved = subjectRepository.save(testSubject);
        entityManager.flush();

        // Assert
        logger.info("TC001 - Output: savedId={}, savedCode={}", saved.getId(), saved.getSubjectCode());
        
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT091 - Find subject by ID")
    void testFindByIdSuccess() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        Subject saved = subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC002 - Input: subjectId={}", saved.getId());

        // Act
        Optional<Subject> found = subjectRepository.findById(saved.getId());

        // Assert
        logger.info("TC002 - Output: found={}, subjectCode={}", 
            found.isPresent(), found.map(Subject::getSubjectCode).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT092 - Find subjects by major ID")
    void testFindByMajorId() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC003 - Input: majorId={}", testMajor.getId());

        // Act
        List<Subject> results = subjectRepository.findByMajorId(Math.toIntExact(testMajor.getId()));

        // Assert
        logger.info("TC003 - Output: resultCount={}, firstCode={}", 
            results.size(), results.isEmpty() ? null : results.get(0).getSubjectCode());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT093 - Find subject by code and semester and academic year")
    void testFindBySubjectCodeAndSemesterAndAcademicYear() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC004 - Input: subjectCode={}, semesterName={}, academicYear={}", 
            "CS101", "Học kỳ 1", "2023-2024");

        // Act
        List<Subject> results = subjectRepository.findAllBySubjectCodeAndSemesterAndAcademicYear(
                "CS101", "Học kỳ 1", "2023-2024"
        );

        // Assert
        logger.info("TC004 - Output: resultCount={}", results.size());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT094 - Find subject by all criteria")
    void testFindBySubjectCodeAndMajorCodeAndSemesterAndClassYear() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC005 - Input: subjectCode={}, majorCode={}, semesterName={}, academicYear={}, classYear={}", 
            "CS101", "KA2021", "Học kỳ 1", "2023-2024", "2021");

        // Act
        Optional<Subject> found = subjectRepository.findBySubjectCodeAndMajorCodeAndSemesterAndClassYear(
                "CS101", "KA2021", "Học kỳ 1", "2023-2024", "2021"
        );

        // Assert
        logger.info("TC005 - Output: found={}, subjectCode={}", 
            found.isPresent(), found.map(Subject::getSubjectCode).orElse(null));
        
        assertThat(found).isPresent();
        assertThat(found.get().getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT095 - Get all subjects with major info")
    void testGetAllSubjectsWithMajorInfo() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC006 - Input: getAllSubjectsWithMajorInfo()");

        // Act
        var results = subjectRepository.getAllSubjectsWithMajorInfo();

        // Assert
        logger.info("TC006 - Output: resultCount={}, firstCode={}", 
            results.size(), results.isEmpty() ? null : results.get(0).getSubjectCode());
        
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubjectCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("DT096 - Find subjects by semester name")
    void testFindBySemesterName() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC007 - Input: semesterName={}", "Học kỳ 1");

        // Act
        List<Subject> results = subjectRepository.findBySemesterName("Học kỳ 1");

        // Assert
        logger.info("TC007 - Output: resultCount={}", results.size());
        
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("DT097 - Get distinct program types")
    void testGetDistinctProgramTypes() {
        // Arrange
        facultyRepository.save(testFaculty);
        majorRepository.save(testMajor);
        semesterRepository.save(testSemester);
        subjectRepository.save(testSubject);
        entityManager.flush();
        logger.info("TC008 - Input: findAllDistinctProgramTypes()");

        // Act
        List<String> results = subjectRepository.findAllDistinctProgramTypes();

        // Assert
        logger.info("TC008 - Output: resultCount={}, contains={}", 
            results.size(), results.contains("Chính quy"));
        
        assertThat(results).contains("Chính quy");
    }
}

