package com.ptit.schedule.service.impl;

import com.ptit.schedule.entity.Role;
import com.ptit.schedule.entity.User;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite UserDetailsServiceImpl - Kiểm thử Service Chi tiết Người dùng")
class UserDetailsServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImplTest.class);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
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
    @DisplayName("ND045 - Tải user theo email thành công")
    void testLoadUserByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        logger.info("TC001 - Input: email={}", "test@example.com");

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        logger.info("TC001 - Output: username={}, password={}, enabled={}", userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled());
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("ND046 - Tải user với email không tồn tại")
    void testLoadUserByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        logger.info("TC002 - Input: email={} (not found)", "nonexistent@example.com");

        // Act & Assert
        logger.info("TC002 - Output: UsernameNotFoundException thrown");
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    @DisplayName("ND047 - Tải user trả về trạng thái enabled")
    void testLoadUserReturnsEnabledUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        logger.info("TC003 - Input: email={}, enabled={}", "test@example.com", true);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        logger.info("TC003 - Output: isEnabled={}", userDetails.isEnabled());
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("ND048 - Tải user bị vô hiệu hoá")
    void testLoadDisabledUser() {
        // Arrange
        User disabledUser = testUser.toBuilder().enabled(false).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(disabledUser));
        logger.info("TC004 - Input: email={}, enabled={}", "test@example.com", false);

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        logger.info("TC004 - Output: isEnabled={}", userDetails.isEnabled());
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("ND049 - Tải user trả về authorities đúng")
    void testLoadUserReturnsCorrectAuthorities() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        logger.info("TC005 - Input: email={}, role={}", "test@example.com", "USER");

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        logger.info("TC005 - Output: authorities={}", userDetails.getAuthorities().stream().map(Object::toString).toList());
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    @DisplayName("ND050 - Tải user admin trả về ADMIN authority")
    void testLoadAdminUserReturnsAdminAuthority() {
        // Arrange
        User adminUser = testUser.toBuilder().role(Role.ADMIN).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(adminUser));
        logger.info("TC006 - Input: email={}, role={}", "test@example.com", "ADMIN");

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        logger.info("TC006 - Output: authorities={}", userDetails.getAuthorities().stream().map(Object::toString).toList());
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }
}

