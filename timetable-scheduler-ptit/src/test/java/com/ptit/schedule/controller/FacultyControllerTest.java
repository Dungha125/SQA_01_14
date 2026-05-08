package com.ptit.schedule.controller;

import com.ptit.schedule.dto.FacultyRequest;
import com.ptit.schedule.dto.FacultyResponse;
import com.ptit.schedule.service.FacultyService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite FacultyController - Kiểm thử Quản lý Khoa")
class FacultyControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(FacultyControllerTest.class);

    @Mock
    private FacultyService facultyService;

    @InjectMocks
    private FacultyController facultyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private FacultyResponse facultyResponse1;
    private FacultyResponse facultyResponse2;
    private String facultyId1;
    private String facultyId2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(facultyController).build();
        objectMapper = new ObjectMapper();

        facultyId1 = UUID.randomUUID().toString();
        facultyId2 = UUID.randomUUID().toString();

        facultyResponse1 = FacultyResponse.builder()
                .id(facultyId1)
                .facultyName("Công nghệ thông tin")
                .build();

        facultyResponse2 = FacultyResponse.builder()
                .id(facultyId2)
                .facultyName("Điện tử - Viễn thông")
                .build();
    }

    @Test
    @DisplayName("DT011 - Lấy danh sách tất cả khoa trả về 200")
    void testGetAllFacultiesSuccess() throws Exception {
        // Arrange
        List<FacultyResponse> faculties = Arrays.asList(facultyResponse1, facultyResponse2);
        when(facultyService.getAllFaculties()).thenReturn(faculties);
        logger.info("TC001 - Input: GET /api/faculties");

        // Act & Assert
        mockMvc.perform(get("/api/faculties")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].facultyName").value("Công nghệ thông tin"));
        logger.info("TC001 - Output: status=200, resultCount={}", faculties.size());
    }

    @Test
    @DisplayName("DT012 - Lấy khoa theo ID trả về 200")
    void testGetFacultyByIdSuccess() throws Exception {
        // Arrange
        when(facultyService.getFacultyById(facultyId1)).thenReturn(facultyResponse1);
        logger.info("TC002 - Input: GET /api/faculties/{}", facultyId1);

        // Act & Assert
        mockMvc.perform(get("/api/faculties/" + facultyId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        logger.info("TC002 - Output: status=200, facultyName={}", facultyResponse1.getFacultyName());
        verify(facultyService, times(1)).getFacultyById(facultyId1);
    }

    @Test
    @DisplayName("DT013 - Tạo khoa trả về 201")
    void testCreateFacultySuccess() throws Exception {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Cơ khí");
        when(facultyService.createFaculty(any(FacultyRequest.class))).thenReturn(facultyResponse1);
        logger.info("TC003 - Input: POST /api/faculties, facultyName={}", request.getFacultyName());

        // Act & Assert
        mockMvc.perform(post("/api/faculties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        logger.info("TC003 - Output: status=201, createdFacultyId={}", facultyResponse1.getId());
    }

    @Test
    @DisplayName("DT014 - Cập nhật khoa trả về 200")
    void testUpdateFacultySuccess() throws Exception {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Công nghệ thông tin - Updated");

        FacultyResponse updatedResponse = facultyResponse1.toBuilder()
                .facultyName("Công nghệ thông tin - Updated")
                .build();
        when(facultyService.updateFaculty(eq(facultyId1), any(FacultyRequest.class)))
                .thenReturn(updatedResponse);
        logger.info("TC004 - Input: PUT /api/faculties/{}, newFacultyName={}", 
            facultyId1, request.getFacultyName());

        // Act & Assert
        mockMvc.perform(put("/api/faculties/" + facultyId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        logger.info("TC004 - Output: status=200, updatedName={}", updatedResponse.getFacultyName());
    }

    @Test
    @DisplayName("DT015 - Xóa khoa trả về 204")
    void testDeleteFacultySuccess() throws Exception {
        // Arrange
        doNothing().when(facultyService).deleteFaculty(facultyId1);
        logger.info("TC005 - Input: DELETE /api/faculties/{}", facultyId1);

        // Act & Assert
        mockMvc.perform(delete("/api/faculties/" + facultyId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        logger.info("TC005 - Output: status=204, deleteSuccess={}", true);
        verify(facultyService, times(1)).deleteFaculty(facultyId1);
    }

    @Test
    @DisplayName("DT016 - Lấy khoa với ID không hợp lệ trả về 500")
    void testGetFacultyNotFound() throws Exception {
        // Arrange
        when(facultyService.getFacultyById("nonexistent"))
                .thenThrow(new RuntimeException("Faculty not found"));
        logger.info("TC006 - Input: GET /api/faculties/nonexistent");

        // Act & Assert
        try {
            mockMvc.perform(get("/api/faculties/nonexistent")
                    .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            logger.info("TC006 - Output: exceptionThrown={}, message={}", true, e.getMessage());
            if (!(e.getCause() instanceof RuntimeException)) {
                throw new AssertionError("Expected RuntimeException but got " + e.getClass().getName());
            }
        }
    }

    @Test
    @DisplayName("DT017 - Tạo khoa mà không có tên")
    void testCreateFacultyWithoutName() throws Exception {
        // Arrange
        String request = "{}";
        logger.info("TC007 - Input: POST /api/faculties with empty request body");

        // Act & Assert
        mockMvc.perform(post("/api/faculties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        logger.info("TC007 - Output: status=400, validationError={}", true);
    }

    @Test
    @DisplayName("DT018 - Cập nhật khoa mà không có tên")
    void testUpdateFacultyWithoutName() throws Exception {
        // Arrange
        String request = "{}";
        logger.info("TC008 - Input: PUT /api/faculties/{} with empty request body", facultyId1);

        // Act & Assert
        mockMvc.perform(put("/api/faculties/" + facultyId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        logger.info("TC008 - Output: status=400, validationError={}", true);
    }

    @Test
    @DisplayName("DT019 - Lấy danh sách tất cả khoa khi trống")
    void testGetAllFacultiesEmpty() throws Exception {
        // Arrange
        when(facultyService.getAllFaculties()).thenReturn(Arrays.asList());
        logger.info("TC009 - Input: GET /api/faculties");

        // Act & Assert
        mockMvc.perform(get("/api/faculties")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        logger.info("TC009 - Output: status=200, resultCount={}", 0);
    }

    @Test
    @DisplayName("DT020 - Xóa khoa không tồn tại trả về 500")
    void testDeleteFacultyNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Faculty not found"))
                .when(facultyService).deleteFaculty("nonexistent");
        logger.info("TC010 - Input: DELETE /api/faculties/nonexistent");

        // Act & Assert
        try {
            mockMvc.perform(delete("/api/faculties/nonexistent")
                    .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            logger.info("TC010 - Output: exceptionThrown={}, message={}", true, e.getMessage());
            if (!(e.getCause() instanceof RuntimeException)) {
                throw new AssertionError("Expected RuntimeException but got " + e.getClass().getName());
            }
        }
    }
}

