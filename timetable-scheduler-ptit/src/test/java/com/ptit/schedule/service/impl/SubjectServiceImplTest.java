package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.SubjectMajorDTO;
import com.ptit.schedule.dto.SubjectRequest;
import com.ptit.schedule.dto.SubjectResponse;
import com.ptit.schedule.entity.*;
import com.ptit.schedule.exception.InvalidDataException;
import com.ptit.schedule.exception.ResourceNotFoundException;
import com.ptit.schedule.repository.*;
import com.ptit.schedule.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite SubjectServiceImpl - Kiểm thử Service Môn học")
class SubjectServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImplTest.class);

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private SubjectServiceImpl subjectService;

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
    @DisplayName("DT031 - Lấy tất cả môn học thành công")
    void testGetAllSubjectsSuccess() {
        // Arrange
        SubjectMajorDTO dto = new SubjectMajorDTO("CS101", "Nhập môn lập trình", "KA2021",
                "2021", 30, 15, 15, 0, 60, 100, 50);
        List<SubjectMajorDTO> dtos = Arrays.asList(dto);
        when(subjectRepository.getAllSubjectsWithMajorInfo()).thenReturn(dtos);
        logger.info("TC001 - Input: getAllSubjects()");

        // Act
        List<SubjectMajorDTO> results = subjectService.getAllSubjects();

        // Assert
        logger.info("TC001 - Output: resultCount={}, firstSubjectCode={}",
                results.size(), results.isEmpty() ? null : results.get(0).getSubjectCode());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubjectCode()).isEqualTo("CS101");
        verify(subjectRepository, times(1)).getAllSubjectsWithMajorInfo();
    }

    @Test
    @DisplayName("DT032 - Lấy môn học với phân trang")
    void testGetAllSubjectsWithPagination() {
        // Arrange
        when(subjectRepository.findAllWithMajorAndFaculty(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(testSubject)));
        logger.info("TC002 - Input: getAllSubjectsWithPagination(page=0, size=10)");

        // Act
        var page = subjectService.getAllSubjectsWithPagination(0, 10, "id", "asc");

        // Assert
        logger.info("TC002 - Output: resultCount={}, totalElements={}",
                page.getContent().size(), page.getTotalElements());

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(subjectRepository, times(1)).findAllWithMajorAndFaculty(any(Pageable.class));
    }

    @Test
    @DisplayName("DT033 - Tạo môn học thành công")
    void testCreateSubjectSuccess() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS101");
        request.setSubjectName("Nhập môn lập trình");
        request.setTheoryHours(30);
        request.setExerciseHours(15);
        request.setLabHours(15);
        request.setProjectHours(0);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setAcademicYear("2023-2024");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);

        when(semesterRepository.findBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(Optional.of(testSemester));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));
        when(subjectRepository.findBySubjectCodeAndMajorCodeAndSemesterAndClassYear(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        logger.info("TC003 - Input: createSubject(subjectCode={}, subjectName={})",
                request.getSubjectCode(), request.getSubjectName());

        // Act
        SubjectResponse response = subjectService.createSubject(request);

        // Assert
        logger.info("TC003 - Output: createdSubjectCode={}, credits={}",
                response.getSubjectCode(), response.getCredits());

        assertThat(response).isNotNull();
        assertThat(response.getSubjectCode()).isEqualTo("CS101");
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    @DisplayName("DT034 - Tạo môn với tên học kỳ trống ném exception")
    void testCreateSubjectWithEmptySemesterName() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSemesterName("");
        logger.info("TC004 - Input: createSubject with emptySemesterName");

        // Act & Assert
        logger.info("TC004 - Output: exceptionThrown={}", true);

        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Tên học kỳ không được để trống");
    }

    @Test
    @DisplayName("DT035 - Cập nhật môn học thành công")
    void testUpdateSubjectSuccess() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS101-Updated");
        request.setSubjectName("Nhập môn lập trình - Updated");
        request.setTheoryHours(35);
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);
        request.setExerciseHours(15);
        request.setLabHours(15);
        request.setProjectHours(0);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));
        when(semesterRepository.findBySemesterName("Học kỳ 1"))
                .thenReturn(Optional.of(testSemester));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        logger.info("TC005 - Input: updateSubject(id=1, subjectCode={})", request.getSubjectCode());

        // Act
        SubjectResponse response = subjectService.updateSubject(1L, request);

        // Assert
        logger.info("TC005 - Output: updateSuccess={}, subjectCode={}", true, response.getSubjectCode());

        assertThat(response).isNotNull();
        verify(subjectRepository, times(1)).findById(1L);
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    @DisplayName("DT036 - Xóa môn học thành công")
    void testDeleteSubjectSuccess() {
        // Arrange
        when(subjectRepository.existsById(1L)).thenReturn(true);
        doNothing().when(subjectRepository).deleteById(1L);
        logger.info("TC006 - Input: deleteSubject(id=1)");

        // Act
        subjectService.deleteSubject(1L);

        // Assert
        logger.info("TC006 - Output: deleteSuccess={}", true);

        verify(subjectRepository, times(1)).existsById(1L);
        verify(subjectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DT037 - Xóa môn học không tồn tại ném exception")
    void testDeleteSubjectNotFound() {
        // Arrange
        when(subjectRepository.existsById(999L)).thenReturn(false);
        logger.info("TC007 - Input: deleteSubject(id=999)");

        // Act & Assert
        logger.info("TC007 - Output: exceptionThrown={}", true);

        assertThatThrownBy(() -> subjectService.deleteSubject(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(subjectRepository, never()).deleteById(999L);
    }

    @Test
    @DisplayName("DT038 - Lấy môn học theo ID ngành")
    void testGetSubjectsByMajorId() {
        // Arrange
        when(subjectRepository.findByMajorId(1)).thenReturn(Arrays.asList(testSubject));
        logger.info("TC008 - Input: getSubjectsByMajorId(majorId=1)");

        // Act
        List<SubjectResponse> results = subjectService.getSubjectsByMajorId(1);

        // Assert
        logger.info("TC008 - Output: resultCount={}, firstSubjectCode={}",
                results.size(), results.isEmpty() ? null : results.get(0).getSubjectCode());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSubjectCode()).isEqualTo("CS101");
        verify(subjectRepository, times(1)).findByMajorId(1);
    }

    @Test
    @DisplayName("DT039 - Lấy môn học chung")
    void testGetCommonSubjects() {
        // Arrange
        SubjectMajorDTO commonSubject = new SubjectMajorDTO("MATH101", "Toán cao cấp", "KA2021",
                "2021", 30, 0, 0, 0, 60, 200, 50);
        List<SubjectMajorDTO> dtos = Arrays.asList(commonSubject);
        when(subjectRepository.findCommonSubjects("Học kỳ 1", "2023-2024"))
                .thenReturn(dtos);
        logger.info("TC009 - Input: getCommonSubjects(semester={}, academicYear={})",
                "Học kỳ 1", "2023-2024");

        // Act
        List<SubjectMajorDTO> results = subjectService.getCommonSubjects("Học kỳ 1", "2023-2024");

        // Assert
        logger.info("TC009 - Output: resultCount={}, majorCode={}",
                results.size(), results.isEmpty() ? null : results.get(0).getMajorCode());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMajorCode()).isEqualTo("Chung");
        verify(subjectRepository, times(1)).findCommonSubjects(anyString(), anyString());
    }

    @Test
    @DisplayName("DT040 - Lấy tất cả loại chương trình")
    void testGetAllProgramTypes() {
        // Arrange
        when(subjectRepository.findAllDistinctProgramTypes())
                .thenReturn(Arrays.asList("Chính quy", "CLC", "Đặc thù"));
        logger.info("TC010 - Input: getAllProgramTypes()");

        // Act
        List<String> results = subjectService.getAllProgramTypes();

        // Assert
        logger.info("TC010 - Output: resultCount={}, types={}", results.size(), results);

        assertThat(results).hasSize(3);
        assertThat(results).contains("Chính quy", "CLC", "Đặc thù");
        verify(subjectRepository, times(1)).findAllDistinctProgramTypes();
    }

    @Test
    @DisplayName("DT041 - Lấy tất cả năm khóa")
    void testGetAllClassYears() {
        // Arrange
        when(subjectRepository.findAllDistinctClassYears())
                .thenReturn(Arrays.asList("2021", "2022", "2023"));
        logger.info("TC011 - Input: getAllClassYears()");

        // Act
        List<String> results = subjectService.getAllClassYears();

        // Assert
        logger.info("TC011 - Output: resultCount={}, classYears={}", results.size(), results);

        assertThat(results).hasSize(3);
        assertThat(results).contains("2021", "2022", "2023");
    }

    @Test
    @DisplayName("DT042 - Xóa môn học theo học kỳ thành công")
    void testDeleteSubjectsBySemester() {
        // Arrange
        List<Subject> subjects = Arrays.asList(testSubject);
        when(subjectRepository.findBySemesterName("Học kỳ 1"))
                .thenReturn(subjects);
        doNothing().when(subjectRepository).deleteBySemesterName("Học kỳ 1");
        logger.info("TC012 - Input: deleteSubjectsBySemesterName(semesterName={})", "Học kỳ 1");

        // Act
        int count = subjectService.deleteSubjectsBySemesterName("Học kỳ 1");

        // Assert
        logger.info("TC012 - Output: deletedCount={}", count);

        assertThat(count).isEqualTo(1);
        verify(subjectRepository, times(1)).findBySemesterName("Học kỳ 1");
        verify(subjectRepository, times(1)).deleteBySemesterName("Học kỳ 1");
    }

    @Test
    @DisplayName("DT043 - Tạo môn học trùng sẽ cập nhật thay vì tạo mới")
    void testCreateSubjectDuplicateUpdates() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS101");
        request.setSubjectName("Nhập môn lập trình");
        request.setTheoryHours(30);
        request.setExerciseHours(15);
        request.setLabHours(15);
        request.setProjectHours(0);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setAcademicYear("2023-2024");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);

        when(semesterRepository.findBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(Optional.of(testSemester));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));
        when(subjectRepository.findBySubjectCodeAndMajorCodeAndSemesterAndClassYear(
                "CS101", "KA2021", "Học kỳ 1", "2023-2024", "2021"))
                .thenReturn(Optional.of(testSubject));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(semesterRepository.findBySemesterName("Học kỳ 1"))
                .thenReturn(Optional.of(testSemester));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        logger.info("TC013 - Input: createSubject with duplicate={}", request.getSubjectCode());

        // Act
        SubjectResponse response = subjectService.createSubject(request);

        // Assert
        logger.info("TC013 - Output: updateInsteadOfCreate={}, subjectCode={}",
                true, response.getSubjectCode());

        assertThat(response).isNotNull();
        verify(subjectRepository, times(1)).save(any(Subject.class));
    }

    @Test
    @DisplayName("DT044 - Lấy môn học với bộ lọc")
    void testGetSubjectsWithFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(subjectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<Subject>(Arrays.asList(testSubject), pageable, 1));
        logger.info("TC014 - Input: getSubjects(search=CS, semester=Học kỳ 1, classYear=2021, majorCode=KA2021)");

        // Act
        var page = subjectService.getSubjects("CS", "Học kỳ 1", "2021", "KA2021", "Chính quy", "2023-2024", pageable);

        // Assert
        logger.info("TC014 - Output: resultCount={}", page.getContent().size());

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("DT045 - Xóa môn theo học kỳ và năm học")
    void testDeleteSubjectsBySemesterAndAcademicYear() {
        // Arrange
        when(subjectRepository.findBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(Arrays.asList(testSubject));
        when(scheduleRepository.findBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(Arrays.asList());
        when(subjectRepository.deleteBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(1);
        logger.info("TC015 - Input: deleteSubjectsBySemesterNameAndAcademicYear(semester={}, academicYear={})",
                "Học kỳ 1", "2023-2024");

        // Act
        int count = subjectService.deleteSubjectsBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024");

        // Assert
        logger.info("TC015 - Output: deletedCount={}", count);

        assertThat(count).isEqualTo(1);
    }

    // ==================== FAIL TEST CASES FOR SERVICE LAYER ====================
    // Test case ID: CTDT-SVC-FAIL-001
    // File name: SubjectServiceImplTest.java
    // Method name: failTestCreateSubjectWithDuplicateCodeDoesNotReject
    // Purpose: Kiểm tra createSubject() không reject khi code trùng
    // Input: createSubject(request với subjectCode="CS101" đã tồn tại)
    // Expected output: Throw DuplicateSubjectException
    // Test Result: FAIL (thực tế gọi updateSubject thay vì reject)
    // Note: Service không reject duplicate, logic sai

    @Test
    @DisplayName("DT046 - CreateSubject không reject duplicate code")
    void failTestCreateSubjectWithDuplicateCodeDoesNotReject() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS101");  // Code trùng
        request.setSubjectName("Lập trình nâng cao");
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setAcademicYear("2023-2024");
        request.setTheoryHours(30);
        request.setExerciseHours(15);
        request.setProjectHours(0);
        request.setLabHours(15);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);

        when(semesterRepository.findBySemesterNameAndAcademicYear("Học kỳ 1", "2023-2024"))
                .thenReturn(Optional.of(testSemester));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));

        // Mock duplicate found
        when(subjectRepository.findBySubjectCodeAndMajorCodeAndSemesterAndClassYear(
                "CS101", "KA2021", "Học kỳ 1", "2023-2024", "2021"))
                .thenReturn(Optional.of(testSubject));

        logger.info("FAIL-TC-SVC-001 - Input: createSubject(code=CS101 - duplicate)");

        // Act & Assert
        logger.info("FAIL-TC-SVC-001 - Output: FAIL - Service không reject, gọi updateSubject");

        // BUG: Service sẽ gọi updateSubject thay vì throw exception
        // Test sẽ FAIL vì kỳ vọng throw exception nhưng thực tế không
        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    // Test case ID: CTDT-SVC-FAIL-002
    // File name: SubjectServiceImplTest.java
    // Method name: failTestCreateSubjectWithDuplicateNameDoesNotReject
    // Purpose: Kiểm tra createSubject() không validate subjectName trùng
    // Input: createSubject(request với subjectName đã tồn tại)
    // Expected output: Throw DuplicateSubjectException
    // Test Result: FAIL (service không có validation cho name)
    // Note: Không có method repository để tìm theo name



    // Test case ID: CTDT-SVC-FAIL-003
    // File name: SubjectServiceImplTest.java
    // Method name: failTestUpdateSubjectCodeToDuplicateDoesNotValidate
    // Purpose: Kiểm tra updateSubject() không validate code trùng
    // Input: updateSubject(id=1, request với subjectCode="CS103" đã tồn tại khác)
    // Expected output: Throw DuplicateSubjectException
    // Test Result: FAIL (update ngay không validate)
    // Note: updateSubject() không có validation

    @Test
    @DisplayName("DT047 - UpdateSubject không validate duplicate code")
    void failTestUpdateSubjectCodeToDuplicateDoesNotValidate() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS103");  // Code của subject khác
        request.setSubjectName("Code updated");
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setAcademicYear("2023-2024");
        request.setTheoryHours(30);
        request.setExerciseHours(15);
        request.setProjectHours(0);
        request.setLabHours(15);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(testSubject));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));
        when(semesterRepository.findBySemesterName("Học kỳ 1"))
                .thenReturn(Optional.of(testSemester));

        // Update ngay không validate
        when(subjectRepository.save(any(Subject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        logger.info("FAIL-TC-SVC-003 - Input: updateSubject(id=1, code=CS103 - duplicate)");

        // Act
        SubjectResponse response = subjectService.updateSubject(1L, request);

        // Assert - BUG: Service update thành công mà kỳ vọng throw exception
        logger.info("FAIL-TC-SVC-003 - Output: FAIL - Update thành công không validate");
        
        // Test sẽ FAIL vì service không validate duplicate code
        assertThat(response.getSubjectCode())
                .as("BUG CTDT-SVC-FAIL-003: updateSubject không validate duplicate code, phải throw exception")
                .isNotEqualTo("CS103");  // Kỳ vọng update failed, nhưng thực tế update thành công
    }

    // Test case ID: CTDT-SVC-FAIL-004
    // File name: SubjectServiceImplTest.java
    // Method name: failTestUpdateSubjectNameToDuplicateDoesNotValidate
    // Purpose: Kiểm tra updateSubject() không validate name trùng
    // Input: updateSubject(id=1, request với subjectName đã tồn tại khác)
    // Expected output: Throw DuplicateSubjectException
    // Test Result: FAIL (update ngay không validate)
    // Note: updateSubject() không validate name

    @Test
    @DisplayName("DT048 - UpdateSubject không validate duplicate name")
    void failTestUpdateSubjectNameToDuplicateDoesNotValidate() {
        // Arrange
        SubjectRequest request = new SubjectRequest();
        request.setSubjectCode("CS101");
        request.setSubjectName("Cấu trúc dữ liệu");  // Name của subject khác
        request.setMajorId("KA2021");
        request.setClassYear("2021");
        request.setFacultyId(testFaculty.getId());
        request.setSemesterName("Học kỳ 1");
        request.setAcademicYear("2023-2024");
        request.setTheoryHours(30);
        request.setExerciseHours(15);
        request.setProjectHours(0);
        request.setLabHours(15);
        request.setSelfStudyHours(60);
        request.setCredits(3);
        request.setNumberOfClasses(2);
        request.setStudentsPerClass(50);
        request.setDepartment("Khoa CNTT");
        request.setExamFormat("Thi viết");
        request.setProgramType("Chính quy");
        request.setIsCommon(false);

        when(subjectRepository.findById(1L))
                .thenReturn(Optional.of(testSubject));
        when(majorRepository.findByMajorCodeAndClassYear("KA2021", "2021"))
                .thenReturn(Optional.of(testMajor));
        when(semesterRepository.findBySemesterName("Học kỳ 1"))
                .thenReturn(Optional.of(testSemester));

        // Update ngay không validate
        when(subjectRepository.save(any(Subject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        logger.info("FAIL-TC-SVC-004 - Input: updateSubject(id=1, name=Cấu trúc dữ liệu - duplicate)");

        // Act
        SubjectResponse response = subjectService.updateSubject(1L, request);

        // Assert - BUG: Service update thành công mà kỳ vọng throw exception
        logger.info("FAIL-TC-SVC-004 - Output: FAIL - Update thành công không validate");

        // Test sẽ FAIL vì service không validate duplicate name
        assertThat(response.getSubjectName())
                .as("BUG CTDT-SVC-FAIL-004: updateSubject không validate duplicate name, phải throw exception")
                .isNotEqualTo("Cấu trúc dữ liệu");  // Kỳ vọng update failed, nhưng thực tế update thành công
    }

}