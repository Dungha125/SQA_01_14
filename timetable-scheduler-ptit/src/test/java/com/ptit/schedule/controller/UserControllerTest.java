package com.ptit.schedule.controller;

import com.ptit.schedule.dto.ToggleUserStatusRequest;
import com.ptit.schedule.dto.UserResponse;
import com.ptit.schedule.entity.Role;
import com.ptit.schedule.exception.GlobalExceptionHandler;
import com.ptit.schedule.service.UserService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite UserController - Kiểm thử Quản lý Người dùng")
class UserControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserResponse userResponse1;
    private UserResponse userResponse2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        userResponse1 = UserResponse.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .fullName("User One")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userResponse2 = UserResponse.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .fullName("User Two")
                .role(Role.ADMIN)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("ND033 - Lấy danh sách tất cả người dùng trả về 200")
    void testGetAllUsersSuccess() throws Exception {
        // Arrange
        List<UserResponse> users = Arrays.asList(userResponse1, userResponse2);
        when(userService.getAllUsers()).thenReturn(users);
        logger.info("TC001 - Input: getAllUsers()");

        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("TC001 - Output: Returned {} users", users.size());
    }

    @Test
    @DisplayName("ND034 - Lấy thông tin người dùng theo ID trả về 200")
    void testGetUserByIdSuccess() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userResponse1);
        logger.info("TC002 - Input: getUserById(id={})", 1L);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("TC002 - Output: Retrieved user={}, role={}", userResponse1.getUsername(), userResponse1.getRole());
    }

    @Test
    @DisplayName("ND035 - Bật trạng thái người dùng trả về 200")
    void testToggleUserStatusToEnabledSuccess() throws Exception {
        // Arrange
        ToggleUserStatusRequest request = new ToggleUserStatusRequest();
        request.setEnabled(true);
        
        UserResponse enabledUser = userResponse2.toBuilder().enabled(true).build();
        when(userService.toggleUserStatus(2L, true)).thenReturn(enabledUser);
        logger.info("TC003 - Input: toggleUserStatus(id={}, enabled={})", 2L, true);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/2/toggle-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        logger.info("TC003 - Output: User status enabled={}", enabledUser.getEnabled());
    }

    @Test
    @DisplayName("ND036 - Tắt trạng thái người dùng trả về 200")
    void testToggleUserStatusToDisabledSuccess() throws Exception {
        // Arrange
        ToggleUserStatusRequest request = new ToggleUserStatusRequest();
        request.setEnabled(false);
        
        UserResponse disabledUser = userResponse1.toBuilder().enabled(false).build();
        when(userService.toggleUserStatus(1L, false)).thenReturn(disabledUser);
        logger.info("TC004 - Input: toggleUserStatus(id={}, enabled={})", 1L, false);

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/toggle-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        logger.info("TC004 - Output: User status enabled={}", disabledUser.getEnabled());
    }

    @Test
    @DisplayName("ND037 - Xóa người dùng trả về 200")
    void testDeleteUserSuccess() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);
        logger.info("TC005 - Input: deleteUser(id={})", 1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("TC005 - Output: User deleted successfully");
    }

    @Test
    @DisplayName("ND038 - Lấy người dùng không tồn tại trả về 500")
    void testGetUserNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found"));
        logger.info("TC006 - Input: getUserById(id={})", 999L);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        
        logger.info("TC006 - Output: User not found error");
    }

    @Test
    @DisplayName("ND039 - Xóa người dùng không tồn tại trả về 500")
    void testDeleteUserNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(999L);
        logger.info("TC007 - Input: deleteUser(id={})", 999L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        
        logger.info("TC007 - Output: User not found error");
    }

    @Test
    @DisplayName("ND040 - Lấy danh sách tất cả người dùng có xác thực trả về 200")
    void testGetAllUsersWithAuth() throws Exception {
        // This test demonstrates the endpoint structure
        // Full authorization testing would require SecurityContext setup
        
        List<UserResponse> users = Arrays.asList(userResponse1, userResponse2);
        when(userService.getAllUsers()).thenReturn(users);
        logger.info("TC008 - Input: getAllUsers() with auth");

        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        logger.info("TC008 - Output: Returned {} users with auth", users.size());
    }

    @Test
    @DisplayName("ND041 - Bật/tắt trạng thái người dùng với trường enabled là null")
    void testToggleUserStatusWithNullEnabled() throws Exception {
        // Arrange
        String request = "{}";
        logger.info("TC009 - Input: toggleUserStatus with enabled=null");

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/1/toggle-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());
        
        logger.info("TC009 - Output: Validation error for null enabled");
    }

    @Test
    @DisplayName("ND042 - Lấy người dùng với ID không hợp lệ trả về 400")
    void testGetUserWithInvalidIdFormat() throws Exception {
        // Act & Assert
        logger.info("TC010 - Input: getUserById(id=invalid)");
        
        mockMvc.perform(get("/api/admin/users/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        logger.info("TC010 - Output: Bad request for invalid ID");
    }

    @Test
    @DisplayName("ND043 - Xóa người dùng với ID không hợp lệ trả về 400")
    void testDeleteUserWithInvalidIdFormat() throws Exception {
        // Act & Assert
        logger.info("TC011 - Input: deleteUser(id=invalid)");
        
        mockMvc.perform(delete("/api/admin/users/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        logger.info("TC011 - Output: Bad request for invalid ID");
    }

    @Test
    @DisplayName("ND044 - Bật/tắt trạng thái người dùng với ID không hợp lệ trả về 400")
    void testToggleUserStatusWithInvalidIdFormat() throws Exception {
        // Arrange
        ToggleUserStatusRequest request = new ToggleUserStatusRequest();
        request.setEnabled(true);
        logger.info("TC012 - Input: toggleUserStatus(id=invalid, enabled=true)");

        // Act & Assert
        mockMvc.perform(patch("/api/admin/users/invalid/toggle-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        logger.info("TC012 - Output: Bad request for invalid ID");
    }
}

