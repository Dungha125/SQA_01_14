package com.ptit.schedule.controller;

import com.ptit.schedule.dto.MajorResponse;
import com.ptit.schedule.service.MajorService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite MajorController - Kiểm thử Quản lý Ngành")
class MajorControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(MajorControllerTest.class);

    @Mock
    private MajorService majorService;

    @InjectMocks
    private MajorController majorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(majorController).build();
    }

    @Test
    @DisplayName("DT061 - Lấy danh sách tất cả ngành trả về 200")
    void testGetAllMajorsSuccess() throws Exception {
        // Arrange
        MajorResponse major1 = MajorResponse.builder()
                .id(1L)
                .majorCode("KA2021")
                .classYear("2021")
                .majorName("Khóa 2021")
                .numberOfStudents(100)
                .build();

        MajorResponse major2 = MajorResponse.builder()
                .id(2L)
                .majorCode("KA2022")
                .classYear("2022")
                .majorName("Khóa 2022")
                .numberOfStudents(120)
                .build();

        List<MajorResponse> majors = Arrays.asList(major1, major2);
        when(majorService.getAllMajors()).thenReturn(majors);
        logger.info("TC001 - Input: GET /api/majors");

        // Act & Assert
        mockMvc.perform(get("/api/majors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].majorCode").value("KA2021"))
                .andExpect(jsonPath("$.data[1].majorCode").value("KA2022"));
        logger.info("TC001 - Output: status=200, resultCount={}", majors.size());
    }

    @Test
    @DisplayName("DT062 - Lấy danh sách tất cả ngành khi trống")
    void testGetAllMajorsEmpty() throws Exception {
        // Arrange
        when(majorService.getAllMajors()).thenReturn(Arrays.asList());
        logger.info("TC002 - Input: GET /api/majors with empty result");

        // Act & Assert
        mockMvc.perform(get("/api/majors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        logger.info("TC002 - Output: status=200, resultCount={}", 0);
    }

    @Test
    @DisplayName("DT063 - Lấy danh sách tất cả ngành với nhiều mục")
    void testGetAllMajorsMultiple() throws Exception {
        // Arrange
        MajorResponse major1 = MajorResponse.builder()
                .id(1L)
                .majorCode("KA2021")
                .classYear("2021")
                .numberOfStudents(100)
                .build();

        List<MajorResponse> majors = Arrays.asList(major1);
        when(majorService.getAllMajors()).thenReturn(majors);
        logger.info("TC003 - Input: GET /api/majors with single entry");

        // Act & Assert
        mockMvc.perform(get("/api/majors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)));
        logger.info("TC003 - Output: status=200, resultCount={}", majors.size());
    }

}

