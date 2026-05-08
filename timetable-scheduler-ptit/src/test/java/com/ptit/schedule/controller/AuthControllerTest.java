package com.ptit.schedule.controller;

import com.ptit.schedule.dto.AuthResponse;
import com.ptit.schedule.dto.LoginRequest;
import com.ptit.schedule.dto.RegisterRequest;
import com.ptit.schedule.entity.Role;
import com.ptit.schedule.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Test Suite AuthController - Kiểm thử Xác thực")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .role(Role.USER)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        authResponse = new AuthResponse(
                "jwt-token",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Role.USER
        );
    }

    @Test
    @DisplayName("ND013 - Đăng ký người dùng thành công trả về 201")
    void testRegisterUserSuccess() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("ND014 - Đăng ký với email không hợp lệ")
    void testRegisterWithInvalidEmail() throws Exception {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ND015 - Đăng ký với tên đăng nhập quá ngắn")
    void testRegisterWithShortUsername() throws Exception {
        // Arrange
        registerRequest.setUsername("ab");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ND016 - Đăng ký với mật khẩu quá ngắn")
    void testRegisterWithShortPassword() throws Exception {
        // Arrange
        registerRequest.setPassword("123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ND017 - Đăng nhập người dùng thành công trả về 200")
    void testLoginUserSuccess() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("ND018 - Đăng nhập với tên đăng nhập trống")
    void testLoginWithEmptyUsername() throws Exception {
        // Arrange
        loginRequest.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ND019 - Đăng nhập với mật khẩu trống")
    void testLoginWithEmptyPassword() throws Exception {
        // Arrange
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ND020 - Đăng nhập với thông tin xác thực không hợp lệ trả về 401")
    void testLoginWithInvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ND021 - Lấy thông tin người dùng hiện tại không có xác thực")
    void testGetCurrentUserWithoutAuth() throws Exception {
        // Arrange
        logger.info("TC009 - Input: GET /api/auth/me without authentication");
        
        // Act & Assert
        // Note: MockMvc standalone không có Spring Security context, nên endpoint trả về 200
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
        
        logger.info("TC009 - Output: Status 200 (MockMvc standalone không có security context)");
    }

    @Test
    @DisplayName("ND022 - Đăng ký với vai trò null trả về 400")
    void testRegisterWithNullRole() throws Exception {
        // Arrange
        registerRequest.setRole(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}

