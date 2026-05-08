package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.AuthResponse;
import com.ptit.schedule.dto.LoginRequest;
import com.ptit.schedule.dto.RegisterRequest;
import com.ptit.schedule.dto.UserResponse;
import com.ptit.schedule.entity.Role;
import com.ptit.schedule.entity.User;
import com.ptit.schedule.exception.DuplicateResourceException;
import com.ptit.schedule.exception.InvalidDataException;
import com.ptit.schedule.exception.ResourceNotFoundException;
import com.ptit.schedule.repository.UserRepository;
import com.ptit.schedule.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bộ test AuthService - Xác thực và đăng ký người dùng")
class AuthServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImplTest.class);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
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

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("ND001 - Đăng ký người dùng thành công")
    void testRegisterUserSuccess() {
        // Arrange
        logger.info("TC001 - Input: username={}, email={}, password={}, fullName={}, role={}", 
            registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword(), 
            registerRequest.getFullName(), registerRequest.getRole());
        
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getRole())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        logger.info("TC001 - Output: username={}, email={}, token={}", 
            response.getUsername(), response.getEmail(), response.getToken());
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtTokenProvider, times(1)).generateToken(testUser.getEmail(), testUser.getRole());
    }

    @Test
    @DisplayName("ND002 - Đăng ký người dùng với username trùng")
    void testRegisterUserWithDuplicateUsername() {
        // Arrange
        logger.info("TC002 - Input: username={} (existing)", registerRequest.getUsername());
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username đã tồn tại");
        logger.info("TC002 - Output: DuplicateResourceException thrown");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("ND003 - Đăng ký người dùng với email trùng")
    void testRegisterUserWithDuplicateEmail() {
        // Arrange
        logger.info("TC003 - Input: email={} (existing)", registerRequest.getEmail());
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email đã tồn tại");
        logger.info("TC003 - Output: DuplicateResourceException thrown");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("ND004 - Đăng nhập bằng username thành công")
    void testLoginUserSuccessByUsername() {
        // Arrange
        logger.info("TC004 - Input: username={}, password={}", loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getRole())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        logger.info("TC004 - Output: username={}, token={}, role={}", 
            response.getUsername(), response.getToken(), response.getRole());
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("ND005 - Đăng nhập bằng email thành công")
    void testLoginUserSuccessByEmail() {
        // Arrange
        logger.info("TC005 - Input: email={}, password={}", loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getRole())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        logger.info("TC005 - Output: email={}, token={}", response.getEmail(), response.getToken());
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail(loginRequest.getUsername());
    }

    @Test
    @DisplayName("ND006 - Đăng nhập với tài khoản không tồn tại")
    void testLoginUserNotFound() {
        // Arrange
        logger.info("TC006 - Input: username={} (non-existent)", loginRequest.getUsername());
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(loginRequest.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tài khoản không tồn tại");
        logger.info("TC006 - Output: ResourceNotFoundException thrown");
    }

    @Test
    @DisplayName("ND007 - Đăng nhập với tài khoản bị vô hiệu hóa")
    void testLoginUserAccountDisabled() {
        // Arrange
        logger.info("TC007 - Input: username={}, enabled=false", loginRequest.getUsername());
        User disabledUser = testUser.toBuilder().enabled(false).build();
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(disabledUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("Tài khoản chưa đươc kích hoạt");
        logger.info("TC007 - Output: InvalidDataException thrown");
    }

    @Test
    @DisplayName("ND008 - Lấy thông tin người dùng hiện tại thành công")
    void testGetCurrentUserSuccess() {
        // Arrange
        logger.info("TC008 - Input: email={}", testUser.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        UserResponse response = authService.getCurrentUser();

        // Assert
        logger.info("TC008 - Output: id={}, username={}, email={}", 
            response.getId(), response.getUsername(), response.getEmail());
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ND009 - Lấy người dùng khi chưa đăng nhập")
    void testGetCurrentUserNotAuthenticated() {
        // Arrange
        logger.info("TC009 - Input: authentication=null");
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User chưa đăng nhập");
        logger.info("TC009 - Output: ResourceNotFoundException thrown");
    }

    @Test
    @DisplayName("ND010 - Lấy người dùng khi user không tồn tại trong DB")
    void testGetCurrentUserNotFoundInDatabase() {
        // Arrange
        logger.info("TC010 - Input: email={} (not found in DB)", testUser.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User không tồn tại");
        logger.info("TC010 - Output: ResourceNotFoundException thrown");
    }

    @Test
    @DisplayName("ND011 - Đăng ký người dùng với vai trò ADMIN")
    void testRegisterUserWithAdminRole() {
        // Arrange
        registerRequest.setRole(Role.ADMIN);
        logger.info("TC011 - Input: username={}, role={}", registerRequest.getUsername(), registerRequest.getRole());
        User adminUser = testUser.toBuilder().role(Role.ADMIN).build();

        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(jwtTokenProvider.generateToken(adminUser.getEmail(), adminUser.getRole())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        logger.info("TC011 - Output: role={}, token={}", response.getRole(), response.getToken());
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("ND012 - Đăng nhập với mật khẩu chính xác")
    void testLoginWithCorrectPassword() {
        // Arrange
        logger.info("TC012 - Input: username={}, password={} (correct)", 
            loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = mock(Authentication.class);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(testUser.getEmail(), testUser.getRole())).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        logger.info("TC012 - Output: id={}, fullName={}, token={}", 
            response.getId(), response.getFullName(), response.getToken());
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Test User");
    }
}

