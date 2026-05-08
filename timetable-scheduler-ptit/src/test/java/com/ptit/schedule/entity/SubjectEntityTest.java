package com.ptit.schedule.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Test Suite Subject Entity - Kiểm thử Entity Môn học")
class SubjectEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(SubjectEntityTest.class);

    private Subject testSubject;
    private Major testMajor;
    private Faculty testFaculty;
    private Semester testSemester;

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

        testSemester = new Semester();
        testSemester.setId(1L);
        testSemester.setSemesterName("Học kỳ 1");
        testSemester.setAcademicYear("2023-2024");

        testSubject = Subject.builder()
                .id(1L)
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
    @DisplayName("DT098 - Tạo môn học bằng builder")
    void testSubjectCreation() {
        logger.info("TC001 - Input: subjectCode={}, subjectName={}, credits={}, theoryHours={}", 
            testSubject.getSubjectCode(), testSubject.getSubjectName(), 
            testSubject.getCredits(), testSubject.getTheoryHours());
        
        logger.info("TC001 - Output: subjectNotNull={}, id={}, code={}", 
            testSubject != null, testSubject.getId(), testSubject.getSubjectCode());
        
        assertThat(testSubject).isNotNull();
        assertThat(testSubject.getId()).isEqualTo(1L);
        assertThat(testSubject.getSubjectCode()).isEqualTo("CS101");
        assertThat(testSubject.getSubjectName()).isEqualTo("Nhập môn lập trình");
        assertThat(testSubject.getTheoryHours()).isEqualTo(30);
        assertThat(testSubject.getCredits()).isEqualTo(3);
    }

    @Test
    @DisplayName("DT099 - Thiết lập và lấy mã môn học")
    void testSetAndGetSubjectCode() {
        logger.info("TC002 - Input: newSubjectCode={}", "CS102");
        
        testSubject.setSubjectCode("CS102");
        
        logger.info("TC002 - Output: subjectCode={}", testSubject.getSubjectCode());
        
        assertThat(testSubject.getSubjectCode()).isEqualTo("CS102");
    }

    @Test
    @DisplayName("DT100 - Thiết lập và lấy tên môn học")
    void testSetAndGetSubjectName() {
        logger.info("TC003 - Input: newSubjectName={}", "Lập trình nâng cao");
        
        testSubject.setSubjectName("Lập trình nâng cao");
        
        logger.info("TC003 - Output: subjectName={}", testSubject.getSubjectName());
        
        assertThat(testSubject.getSubjectName()).isEqualTo("Lập trình nâng cao");
    }

    @Test
    @DisplayName("DT101 - Thiết lập và lấy số giờ học")
    void testSetAndGetHours() {
        logger.info("TC004 - Input: theoryHours={}, exerciseHours={}, labHours={}, projectHours={}", 
            40, 20, 10, 5);
        
        testSubject.setTheoryHours(40);
        testSubject.setExerciseHours(20);
        testSubject.setLabHours(10);
        testSubject.setProjectHours(5);

        logger.info("TC004 - Output: theoryHours={}, exerciseHours={}, labHours={}, projectHours={}", 
            testSubject.getTheoryHours(), testSubject.getExerciseHours(), 
            testSubject.getLabHours(), testSubject.getProjectHours());
        
        assertThat(testSubject.getTheoryHours()).isEqualTo(40);
        assertThat(testSubject.getExerciseHours()).isEqualTo(20);
        assertThat(testSubject.getLabHours()).isEqualTo(10);
        assertThat(testSubject.getProjectHours()).isEqualTo(5);
    }

    @Test
    @DisplayName("DT102 - Thiết lập và lấy số tín chỉ")
    void testSetAndGetCredits() {
        logger.info("TC005 - Input: newCredits={}", 4);
        
        testSubject.setCredits(4);
        
        logger.info("TC005 - Output: credits={}", testSubject.getCredits());
        
        assertThat(testSubject.getCredits()).isEqualTo(4);
    }

    @Test
    @DisplayName("DT103 - Thiết lập và lấy loại chương trình")
    void testSetAndGetProgramType() {
        logger.info("TC006 - Input: newProgramType={}", "CLC");
        
        testSubject.setProgramType("CLC");
        
        logger.info("TC006 - Output: programType={}", testSubject.getProgramType());
        
        assertThat(testSubject.getProgramType()).isEqualTo("CLC");
    }

    @Test
    @DisplayName("DT104 - Thiết lập và lấy trạng thái môn học chung")
    void testSetAndGetIsCommon() {
        logger.info("TC007 - Input: isCommon={}", true);
        
        testSubject.setIsCommon(true);
        
        logger.info("TC007 - Output: isCommon={}", testSubject.getIsCommon());
        
        assertThat(testSubject.getIsCommon()).isTrue();
    }

    @Test
    @DisplayName("DT105 - Thiết lập và lấy số lớp")
    void testSetAndGetNumberOfClasses() {
        logger.info("TC008 - Input: newNumberOfClasses={}", 4);
        
        testSubject.setNumberOfClasses(4);
        
        logger.info("TC008 - Output: numberOfClasses={}", testSubject.getNumberOfClasses());
        
        assertThat(testSubject.getNumberOfClasses()).isEqualTo(4);
    }

    @Test
    @DisplayName("DT106 - Thiết lập và lấy số sinh viên mỗi lớp")
    void testSetAndGetStudentsPerClass() {
        logger.info("TC009 - Input: newStudentsPerClass={}", 60);
        
        testSubject.setStudentsPerClass(60);
        
        logger.info("TC009 - Output: studentsPerClass={}", testSubject.getStudentsPerClass());
        
        assertThat(testSubject.getStudentsPerClass()).isEqualTo(60);
    }

    @Test
    @DisplayName("DT107 - Môn học với bộ môn")
    void testSubjectWithDepartment() {
        logger.info("TC010 - Input: newDepartment={}", "Bộ môn Lập trình");
        
        testSubject.setDepartment("Bộ môn Lập trình");
        
        logger.info("TC010 - Output: department={}", testSubject.getDepartment());
        
        assertThat(testSubject.getDepartment()).isEqualTo("Bộ môn Lập trình");
    }

    @Test
    @DisplayName("DT108 - Môn học với hình thức thi")
    void testSubjectWithExamFormat() {
        logger.info("TC011 - Input: newExamFormat={}", "Thi trắc nghiệm");
        
        testSubject.setExamFormat("Thi trắc nghiệm");
        
        logger.info("TC011 - Output: examFormat={}", testSubject.getExamFormat());
        
        assertThat(testSubject.getExamFormat()).isEqualTo("Thi trắc nghiệm");
    }

    @Test
    @DisplayName("DT109 - Các mối quan hệ của môn học")
    void testSubjectRelationships() {
        logger.info("TC012 - Input: subject={}, major={}, semester={}", 
            testSubject.getId(), testMajor.getId(), testSemester.getId());
        
        logger.info("TC012 - Output: hasMajor={}, hasSemester={}", 
            testSubject.getMajor() != null, testSubject.getSemester() != null);
        
        assertThat(testSubject.getMajor()).isEqualTo(testMajor);
        assertThat(testSubject.getSemester()).isEqualTo(testSemester);
    }

}
