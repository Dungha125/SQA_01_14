package com.ptit.schedule.controller;

import com.ptit.schedule.dto.SubjectFullDTO;
import com.ptit.schedule.dto.SubjectRequest;
import com.ptit.schedule.service.SubjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite SubjectController - Kiểm thử Quản lý Môn học")
class SubjectControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SubjectControllerTest.class);

    @Mock(lenient = true)
    private SubjectService subjectService;

    @InjectMocks
    private SubjectController subjectController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SubjectFullDTO subjectDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subjectController).build();
        objectMapper = new ObjectMapper();

        subjectDTO = SubjectFullDTO.builder()
                .id(1L)
                .subjectCode("CS101")
                .subjectName("Nhập môn lập trình")
                .theoryHours(30)
                .exerciseHours(15)
                .labHours(15)
                .projectHours(0)
                .credits(3)
                .examFormat("Thi viết")
                .programType("Chính quy")
                .build();
    }

    @Test
    @DisplayName("DT049 - Lấy danh sách môn học với phân trang trả về 200")
    void testGetAllSubjectsWithPaginationSuccess() throws Exception {
        // Arrange
        var page = new PageImpl<>(Arrays.asList(subjectDTO), PageRequest.of(0, 10), 1);
        when(subjectService.getSubjects(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
                .thenReturn(page);
        logger.info("TC001 - Input: GET /api/subjects, page=0, size=10, sortBy=id, sortDir=asc");

        // Act & Assert
        mockMvc.perform(get("/api/subjects")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDir", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC001 - Output: status=200, resultCount={}", page.getTotalElements());
    }

    @Test
    @DisplayName("DT050 - Lấy môn học với bộ lọc trả về 200")
    void testGetSubjectsWithFiltersSuccess() throws Exception {
        // Arrange
        var page = new PageImpl<>(Arrays.asList(subjectDTO), PageRequest.of(0, 10), 1);
        when(subjectService.getSubjects(
                ArgumentMatchers.anyString(), 
                ArgumentMatchers.nullable(String.class), 
                ArgumentMatchers.nullable(String.class), 
                ArgumentMatchers.nullable(String.class),
                ArgumentMatchers.nullable(String.class), 
                ArgumentMatchers.nullable(String.class), 
                any()))
                .thenReturn(page);
        logger.info("TC002 - Input: GET /api/subjects, search=CS");

        // Act & Assert
        mockMvc.perform(get("/api/subjects")
                .param("search", "CS")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC002 - Output: status=200, resultCount={}", page.getTotalElements());
    }

    @Test
    @DisplayName("DT051 - Tạo môn học trả về 201")
    void testCreateSubjectSuccess() throws Exception {
        // Arrange
        SubjectRequest request = SubjectRequest.builder()
                .subjectCode("CS101")
                .subjectName("Nhập môn lập trình")
                .semesterName("Học kỳ 1")
                .academicYear("2023-2024")
                .classYear("2023")
                .numberOfStudents(30)
                .majorId("1")
                .credits(3)
                .facultyId("1")
                .build();

        var subjectResponse = com.ptit.schedule.dto.SubjectResponse.builder()
                .id(1L)
                .subjectCode("CS101")
                .subjectName("Nhập môn lập trình")
                .build();
        
        when(subjectService.createSubject(any(SubjectRequest.class)))
                .thenReturn(subjectResponse);
        logger.info("TC003 - Input: POST /api/subjects, subjectCode={}, subjectName={}", 
            request.getSubjectCode(), request.getSubjectName());

        // Act & Assert
        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        logger.info("TC003 - Output: status=201, createdSubject={}", request.getSubjectCode());
    }

    @Test
    @DisplayName("DT052 - Lấy môn học theo ID trả về 200")
    void testGetSubjectByIdSuccess() throws Exception {
        // Arrange
        var subjectResponse = com.ptit.schedule.dto.SubjectResponse.builder()
                .id(1L)
                .subjectCode("CS101")
                .subjectName("Nhập môn lập trình")
                .build();
        
        when(subjectService.getSubjectById(1L))
                .thenReturn(subjectResponse);
        logger.info("TC004 - Input: GET /api/subjects/1");

        // Act & Assert
        mockMvc.perform(get("/api/subjects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC004 - Output: status=200, subjectId={}", 1L);
    }

    @Test
    @DisplayName("DT053 - Cập nhật môn học trả về 200")
    void testUpdateSubjectSuccess() throws Exception {
        // Arrange
        SubjectRequest request = SubjectRequest.builder()
                .subjectCode("CS101-Updated")
                .subjectName("Nhập môn lập trình - Updated")
                .semesterName("Học kỳ 1")
                .academicYear("2023-2024")
                .classYear("2023")
                .numberOfStudents(30)
                .majorId("1")
                .credits(3)
                .facultyId("1")
                .build();
        
        var subjectResponse = com.ptit.schedule.dto.SubjectResponse.builder()
                .id(1L)
                .subjectCode("CS101-Updated")
                .subjectName("Nhập môn lập trình - Updated")
                .build();
        
        when(subjectService.updateSubject(1L, request))
                .thenReturn(subjectResponse);
        logger.info("TC005 - Input: PUT /api/subjects/1, subjectCode={}", request.getSubjectCode());

        // Act & Assert
        mockMvc.perform(put("/api/subjects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        logger.info("TC005 - Output: status=200, updateSuccess={}", true);
    }

    @Test
    @DisplayName("DT054 - Xóa môn học trả về 200")
    void testDeleteSubjectSuccess() throws Exception {
        // Arrange
        doNothing().when(subjectService).deleteSubject(1L);
        logger.info("TC006 - Input: DELETE /api/subjects/1");

        // Act & Assert
        mockMvc.perform(delete("/api/subjects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC006 - Output: status=200, deleteSuccess={}", true);
    }

    @Test
    @DisplayName("DT055 - Lấy tất cả loại chương trình trả về 200")
    void testGetAllProgramTypes() throws Exception {
        // Arrange
        when(subjectService.getAllProgramTypes())
                .thenReturn(Arrays.asList("Chính quy", "CLC"));
        logger.info("TC007 - Input: GET /api/subjects/program-types");

        // Act & Assert
        mockMvc.perform(get("/api/subjects/program-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC007 - Output: status=200, programTypeCount={}", 2);
    }

    @Test
    @DisplayName("DT056 - Lấy tất cả năm khóa trả về 200")
    void testGetAllClassYears() throws Exception {
        // Arrange
        when(subjectService.getAllClassYears())
                .thenReturn(Arrays.asList("2021", "2022"));
        logger.info("TC008 - Input: GET /api/subjects/class-years");

        // Act & Assert
        mockMvc.perform(get("/api/subjects/class-years")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC008 - Output: status=200, classYearCount={}", 2);
    }

    @Test
    @DisplayName("DT057 - Tạo môn học không có mã trả về 400")
    void testCreateSubjectWithoutCode() throws Exception {
        // Arrange
        String request = "{}";
        logger.info("TC009 - Input: POST /api/subjects with empty request body");

        // Act & Assert
        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        logger.info("TC009 - Output: status=400, validationError={}", true);
    }

    @Test
    @DisplayName("DT058 - Lấy môn học với số trang không hợp lệ")
    void testGetSubjectsWithInvalidPageNumber() throws Exception {
        // Arrange - Controller validates page and sets to 0 if negative, so returns 200 with valid data
        var page = new PageImpl<>(Arrays.asList(subjectDTO), PageRequest.of(0, 10), 1);
        when(subjectService.getSubjects(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
                .thenReturn(page);
        logger.info("TC010 - Input: GET /api/subjects with invalid page=invalid");

        // Act & Assert - Invalid page parameter causes 400 Bad Request from Spring
        mockMvc.perform(get("/api/subjects")
                .param("page", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        logger.info("TC010 - Output: status=400, validationError={}", true);
    }

    @Test
    @DisplayName("DT059 - Lấy danh sách môn học rỗng trả về 200")
    void testGetSubjectsEmptyResult() throws Exception {
        // Arrange
        PageImpl<SubjectFullDTO> page = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(subjectService.getSubjects(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
                .thenReturn(page);
        logger.info("TC011 - Input: GET /api/subjects with empty result");

        // Act & Assert
        mockMvc.perform(get("/api/subjects")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDir", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC011 - Output: status=200, resultCount={}", 0);
    }

    @Test
    @DisplayName("DT060 - Xóa môn học không tồn tại trả về 404")
    void testDeleteNonExistentSubject() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Subject not found"))
                .when(subjectService).deleteSubject(999L);
        logger.info("TC012 - Input: DELETE /api/subjects/999");

        // Act & Assert
        mockMvc.perform(delete("/api/subjects/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        logger.info("TC012 - Output: status=404, exceptionThrown={}", true);
    }
}

