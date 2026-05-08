package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.UserResponse;
import com.ptit.schedule.entity.Role;
import com.ptit.schedule.entity.User;
import com.ptit.schedule.exception.ResourceNotFoundException;
import com.ptit.schedule.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite UserServiceImpl - Kiểm thử Service Người dùng")
class UserServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImplTest.class);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .password("encodedPassword")
                .fullName("User One")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .password("encodedPassword")
                .fullName("User Two")
                .role(Role.ADMIN)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("ND023 - Lấy tất cả người dùng thành công")
    void testGetAllUsersSuccess() {
        // Arrange
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userRepository.findAll()).thenReturn(users);
        logger.info("TC001 - Input: findAll() called");

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        logger.info("TC001 - Output: totalUsers={}, user1={}, user2={}", responses.size(), responses.get(0).getUsername(), responses.get(1).getUsername());
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getUsername()).isEqualTo("user1");
        assertThat(responses.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("ND024 - Xử lý danh sách rỗng")
    void testGetAllUsersEmpty() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        logger.info("TC002 - Input: findAll() with empty result");

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        logger.info("TC002 - Output: isEmpty={}", responses.isEmpty());
        assertThat(responses).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("ND025 - Lấy người dùng theo ID thành công")
    void testGetUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        logger.info("TC003 - Input: userId=1");

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        logger.info("TC003 - Output: userId={}, username={}, email={}", response.getId(), response.getUsername(), response.getEmail());
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("user1");
        assertThat(response.getEmail()).isEqualTo("user1@example.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ND026 - Lấy người dùng theo ID không tồn tại")
    void testGetUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        logger.info("TC004 - Input: userId=999 (not found)");

        // Act & Assert
        logger.info("TC004 - Output: ResourceNotFoundException thrown");
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("ND027 - Bật trạng thái người dùng")
    void testToggleUserStatusToEnabled() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));
        when(userRepository.save(any(User.class))).thenReturn(testUser2.toBuilder().enabled(true).build());
        logger.info("TC005 - Input: userId=2, enabled=true");

        // Act
        UserResponse response = userService.toggleUserStatus(2L, true);

        // Assert
        logger.info("TC005 - Output: userId={}, enabled={}", response.getId(), response.getEnabled());
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEnabled()).isTrue();
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("ND028 - Tắt trạng thái người dùng")
    void testToggleUserStatusToDisabled() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        when(userRepository.save(any(User.class))).thenReturn(testUser1.toBuilder().enabled(false).build());
        logger.info("TC006 - Input: userId=1, enabled=false");

        // Act
        UserResponse response = userService.toggleUserStatus(1L, false);

        // Assert
        logger.info("TC006 - Output: userId={}, enabled={}", response.getId(), response.getEnabled());
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEnabled()).isFalse();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("ND029 - Bật/Tắt trạng thái người dùng không tồn tại")
    void testToggleUserStatusUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        logger.info("TC007 - Input: userId=999 (not found), enabled=true");

        // Act & Assert
        logger.info("TC007 - Output: ResourceNotFoundException thrown");
        assertThatThrownBy(() -> userService.toggleUserStatus(999L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("ND030 - Xoá người dùng thành công")
    void testDeleteUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser1));
        doNothing().when(userRepository).delete(any(User.class));
        logger.info("TC008 - Input: userId=1, username={}", testUser1.getUsername());

        // Act
        userService.deleteUser(1L);

        // Assert
        logger.info("TC008 - Output: User deleted successfully, findById called, delete called");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    @DisplayName("ND031 - Xoá người dùng không tồn tại")
    void testDeleteUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        logger.info("TC009 - Input: userId=999 (not found)");

        // Act & Assert
        logger.info("TC009 - Output: ResourceNotFoundException thrown");
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("ND032 - Lấy người dùng với vai trò ADMIN")
    void testGetUserWithAdminRole() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser2));
        logger.info("TC010 - Input: userId=2 (ADMIN role)");

        // Act
        UserResponse response = userService.getUserById(2L);

        // Assert
        logger.info("TC010 - Output: userId={}, role={}", response.getId(), response.getRole());
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }
}

