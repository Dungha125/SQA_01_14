package com.ptit.schedule.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Test Suite User Entity - Kiểm thử Entity Người dùng")
class UserEntityTest {

    private static final Logger logger = LoggerFactory.getLogger(UserEntityTest.class);

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
    @DisplayName("ND057 - Tạo người dùng bằng builder")
    void testUserCreation() {
        logger.info("TC001 - Input: username={}, email={}, fullName={}, role={}, enabled={}", 
            testUser.getUsername(), testUser.getEmail(), testUser.getFullName(), testUser.getRole(), testUser.getEnabled());
        
        // Assert
        assertThat(testUser).isNotNull();
        assertThat(testUser.getId()).isEqualTo(1L);
        assertThat(testUser.getUsername()).isEqualTo("testuser");
        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
        assertThat(testUser.getFullName()).isEqualTo("Test User");
        assertThat(testUser.getRole()).isEqualTo(Role.USER);
        assertThat(testUser.getEnabled()).isTrue();
        
        logger.info("TC001 - Output: User created successfully");
    }

    @Test
    @DisplayName("ND058 - getPassword trả về mật khẩu mã hóa")
    void testGetPassword() {
        logger.info("TC002 - Input: password is set");
        
        // Act
        String password = testUser.getPassword();

        // Assert
        assertThat(password).isEqualTo("encodedPassword");
        logger.info("TC002 - Output: password={}", password);
    }

    @Test
    @DisplayName("ND059 - getUsername trả về tên đăng nhập")
    void testGetUsername() {
        logger.info("TC003 - Input: username={}", testUser.getUsername());
        
        // Act
        String username = testUser.getUsername();

        // Assert
        assertThat(username).isEqualTo("testuser");
        logger.info("TC003 - Output: username={}", username);
    }

    @Test
    @DisplayName("ND060 - getAuthorities trả về vai trò đúng")
    void testGetAuthorities() {
        logger.info("TC004 - Input: role={}", testUser.getRole());
        
        // Act
        Collection<? extends GrantedAuthority> authorities = testUser.getAuthorities();

        // Assert
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority).toList())
                .contains("ROLE_USER");
        
        logger.info("TC004 - Output: authorities={}", authorities.stream().map(GrantedAuthority::getAuthority).toList());
    }

    @Test
    @DisplayName("ND061 - isEnabled trả về true khi bật")
    void testIsEnabledTrue() {
        logger.info("TC005 - Input: enabled={}", true);
        
        // Act
        boolean isEnabled = testUser.isEnabled();

        // Assert
        assertThat(isEnabled).isTrue();
        logger.info("TC005 - Output: isEnabled={}", isEnabled);
    }

    @Test
    @DisplayName("ND062 - isEnabled trả về false khi tắt")
    void testIsEnabledFalse() {
        logger.info("TC006 - Input: setting enabled=false");
        
        // Arrange
        testUser.setEnabled(false);

        // Act
        boolean isEnabled = testUser.isEnabled();

        // Assert
        assertThat(isEnabled).isFalse();
        logger.info("TC006 - Output: isEnabled={}", isEnabled);
    }

    @Test
    @DisplayName("ND063 - isAccountNonExpired luôn trả về true")
    void testIsAccountNonExpired() {
        logger.info("TC007 - Input: User account");
        
        // Act
        boolean isNonExpired = testUser.isAccountNonExpired();

        // Assert
        assertThat(isNonExpired).isTrue();
        logger.info("TC007 - Output: isAccountNonExpired={}", isNonExpired);
    }

    @Test
    @DisplayName("ND064 - isAccountNonLocked luôn trả về true")
    void testIsAccountNonLocked() {
        logger.info("TC008 - Input: User account");
        
        // Act
        boolean isNonLocked = testUser.isAccountNonLocked();

        // Assert
        assertThat(isNonLocked).isTrue();
        logger.info("TC008 - Output: isAccountNonLocked={}", isNonLocked);
    }

    @Test
    @DisplayName("ND065 - isCredentialsNonExpired luôn trả về true")
    void testIsCredentialsNonExpired() {
        logger.info("TC009 - Input: User credentials");
        
        // Act
        boolean isNonExpired = testUser.isCredentialsNonExpired();

        // Assert
        assertThat(isNonExpired).isTrue();
        logger.info("TC009 - Output: isCredentialsNonExpired={}", isNonExpired);
    }

    @Test
    @DisplayName("ND066 - Người dùng với vai trò ADMIN")
    void testUserWithAdminRole() {
        logger.info("TC010 - Input: Creating user with ADMIN role");
        
        // Arrange
        User adminUser = testUser.toBuilder().role(Role.ADMIN).build();

        // Act
        Collection<? extends GrantedAuthority> authorities = adminUser.getAuthorities();

        // Assert
        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority).toList())
                .contains("ROLE_ADMIN");
        
        logger.info("TC010 - Output: authorities={}", authorities.stream().map(GrantedAuthority::getAuthority).toList());
    }

    @Test
    @DisplayName("ND067 - Thời gian tạo và cập nhật người dùng không null")
    void testUserTimestamps() {
        logger.info("TC011 - Input: Check timestamps");
        
        // Assert
        assertThat(testUser.getCreatedAt()).isNotNull();
        assertThat(testUser.getUpdatedAt()).isNotNull();
        
        logger.info("TC011 - Output: createdAt={}, updatedAt={}", testUser.getCreatedAt(), testUser.getUpdatedAt());
    }

    @Test
    @DisplayName("ND068 - Người dùng có thể bật và tắt trạng thái")
    void testToggleUserStatus() {
        logger.info("TC012 - Input: Initial enabled={}", testUser.getEnabled());
        
        // Arrange
        assertThat(testUser.getEnabled()).isTrue();

        // Act
        testUser.setEnabled(false);

        // Assert
        assertThat(testUser.getEnabled()).isFalse();
        assertThat(testUser.isEnabled()).isFalse();
        
        logger.info("TC012 - After disable: enabled={}", testUser.getEnabled());

        // Act
        testUser.setEnabled(true);

        // Assert
        assertThat(testUser.getEnabled()).isTrue();
        assertThat(testUser.isEnabled()).isTrue();
        
        logger.info("TC012 - After re-enable: enabled={}", testUser.getEnabled());
    }
}

