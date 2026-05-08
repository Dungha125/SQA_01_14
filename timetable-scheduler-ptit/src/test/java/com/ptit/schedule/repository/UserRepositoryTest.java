package com.ptit.schedule.repository;

import com.ptit.schedule.entity.Role;
import com.ptit.schedule.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Test Suite UserRepository - Kiểm thử Repository Người dùng")
class UserRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .fullName("Test User")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("ND051 - Lưu người dùng thành công")
    void testSaveUserSuccess() {
        // Arrange
        logger.info("TC001 - Input: username={}, email={}, password={}, role={}, enabled={}",
                testUser.getUsername(), testUser.getEmail(), testUser.getPassword(),
                testUser.getRole(), testUser.getEnabled());

        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        logger.info("TC001 - Output: savedId={}, username={}, email={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("ND052 - Tìm người dùng theo username")
    void testFindByUsername() {
        // Arrange
        userRepository.save(testUser);
        logger.info("TC002 - Input: username={}", "testuser");

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        logger.info("TC002 - Output: found={}, email={}",
                foundUser.isPresent(), foundUser.map(User::getEmail).orElse(null));

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    @DisplayName("ND053 - Tìm người dùng theo email")
    void testFindByEmail() {
        // Arrange
        userRepository.save(testUser);
        logger.info("TC003 - Input: email={}", "testuser@example.com");

        // Act
        Optional<User> foundUser = userRepository.findByEmail("testuser@example.com");

        // Assert
        logger.info("TC003 - Output: found={}, username={}",
                foundUser.isPresent(), foundUser.map(User::getUsername).orElse(null));

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    @DisplayName("ND054 - Kiểm tra username tồn tại")
    void testExistsByUsername() {
        // Arrange
        userRepository.save(testUser);
        logger.info("TC004 - Input: username={}", "testuser");

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        logger.info("TC004 - Output: exists={}", exists);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("ND055 - Kiểm tra username không tồn tại")
    void testExistsByUsernameNotFound() {
        // Arrange
        logger.info("TC005 - Input: username={}", "nonexistent");

        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        logger.info("TC005 - Output: exists={}", exists);

        assertThat(exists).isFalse();
    }

}