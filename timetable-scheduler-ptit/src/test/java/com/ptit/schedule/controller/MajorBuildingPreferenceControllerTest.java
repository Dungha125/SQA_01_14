package com.ptit.schedule.controller;

import com.ptit.schedule.dto.MajorBuildingPreferenceRequest;
import com.ptit.schedule.entity.MajorBuildingPreference;
import com.ptit.schedule.service.MajorBuildingPreferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite MajorBuildingPreferenceController - Kiểm thử Quản lý Ưu tiên Tòa nhà Ngành")
class MajorBuildingPreferenceControllerTest {

    @Mock
    private MajorBuildingPreferenceService preferenceService;

    @InjectMocks
    private MajorBuildingPreferenceController preferenceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MajorBuildingPreference testPreference;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(preferenceController).build();
        objectMapper = new ObjectMapper();

        testPreference = new MajorBuildingPreference();
        testPreference.setId(1L);
        testPreference.setNganh("KA2021");
        testPreference.setPreferredBuilding("A1");
        testPreference.setPriorityLevel(1);
        testPreference.setIsActive(true);
        testPreference.setNotes("Tòa ưu tiên 1");
    }

    @Test
    @DisplayName("DT110 - Lấy tất cả ưu tiên hoạt động trả về 200")
    void testGetAllPreferencesSuccess() throws Exception {
        // Arrange
        List<MajorBuildingPreference> prefs = Arrays.asList(testPreference);
        when(preferenceService.getAllActivePreferences()).thenReturn(prefs);

        // Act & Assert
        mockMvc.perform(get("/api/major-building-preferences")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nganh").value("KA2021"));
    }

    @Test
    @DisplayName("DT111 - Lấy tòa nhà ưu tiên cho ngành trả về 200")
    void testGetPreferredBuildingsSuccess() throws Exception {
        // Arrange
        List<String> buildings = Arrays.asList("A1", "A2", "B1");
        when(preferenceService.getPreferredBuildingsForMajor("KA2021"))
                .thenReturn(buildings);

        // Act & Assert
        mockMvc.perform(get("/api/major-building-preferences/major/KA2021")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("A1"));
    }

    @Test
    @DisplayName("DT112 - Tạo hoặc cập nhật ưu tiên trả về 200")
    void testCreateOrUpdatePreferenceSuccess() throws Exception {
        // Arrange
        MajorBuildingPreferenceRequest request = new MajorBuildingPreferenceRequest();
        request.setNganh("KA2021");
        request.setPreferredBuilding("A1");
        request.setPriorityLevel(1);
        request.setNotes("Tòa ưu tiên 1");

        when(preferenceService.createOrUpdatePreference(
                "KA2021", "A1", 1, "Tòa ưu tiên 1"))
                .thenReturn(testPreference);

        // Act & Assert
        mockMvc.perform(post("/api/major-building-preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lưu ưu tiên thành công"));
    }

    @Test
    @DisplayName("DT113 - Vô hiệu hóa ưu tiên trả về 200")
    void testDeactivatePreferenceSuccess() throws Exception {
        // Arrange
        doNothing().when(preferenceService)
                .deactivatePreference("KA2021", "A1");

        // Act & Assert
        mockMvc.perform(delete("/api/major-building-preferences/major/KA2021/building/A1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vô hiệu hóa ưu tiên thành công"));
    }

    @Test
    @DisplayName("DT114 - Tạo nhiều ưu tiên hàng loạt trả về 200")
    void testBulkCreatePreferencesSuccess() throws Exception {
        // Arrange
        MajorBuildingPreferenceRequest req1 = new MajorBuildingPreferenceRequest();
        req1.setNganh("KA2021");
        req1.setPreferredBuilding("A1");
        req1.setPriorityLevel(1);
        req1.setNotes("Tòa ưu tiên 1");

        MajorBuildingPreferenceRequest req2 = new MajorBuildingPreferenceRequest();
        req2.setNganh("KA2021");
        req2.setPreferredBuilding("A2");
        req2.setPriorityLevel(2);
        req2.setNotes("Tòa ưu tiên 2");

        List<MajorBuildingPreferenceRequest> requests = Arrays.asList(req1, req2);

        when(preferenceService.createOrUpdatePreference(anyString(), anyString(), anyInt(), any()))
                .thenReturn(testPreference);

        // Act & Assert
        mockMvc.perform(post("/api/major-building-preferences/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Import ưu tiên thành công"))
                .andExpect(jsonPath("$.data").value("Đã import 2 ưu tiên"));
    }

    @Test
    @DisplayName("DT115 - Lấy ưu tiên khi danh sách trống trả về 200")
    void testGetPreferencesEmpty() throws Exception {
        // Arrange
        when(preferenceService.getAllActivePreferences()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/major-building-preferences")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("DT116 - Tạo ưu tiên mà không có ngành trả về 400")
    void testCreatePreferenceWithoutNganh() throws Exception {
        // Arrange
        String request = "{\"preferredBuilding\": \"A1\"}";

        // Act & Assert
        mockMvc.perform(post("/api/major-building-preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DT117 - Lấy tòa nhà ưu tiên danh sách trống")
    void testGetPreferredBuildingsEmpty() throws Exception {
        // Arrange
        when(preferenceService.getPreferredBuildingsForMajor("KA9999"))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/major-building-preferences/major/KA9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}

