package com.ptit.schedule.config;

import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.entity.Role;
import com.ptit.schedule.entity.User;
import com.ptit.schedule.repository.FacultyRepository;
import com.ptit.schedule.repository.RoomRepository;
import com.ptit.schedule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final DataSource dataSource;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        try {
            // Chỉ chạy SQL batch nếu database chưa có data faculties/rooms — tách khỏi user init
            if (isDatabaseEmpty()) {
                log.info("Database is empty. Starting data initialization from data.sql...");
                try (Connection connection = dataSource.getConnection()) {
                    ClassPathResource resource = new ClassPathResource("data.sql");
                    if (resource.exists()) {
                        ScriptUtils.executeSqlScript(connection, resource);
                        log.info("Data initialization completed successfully.");
                        log.info("Faculties count: {}", facultyRepository.count());
                        log.info("Rooms count: {}", roomRepository.count());
                    } else {
                        log.warn("data.sql file not found, skipping data initialization.");
                    }
                }
            } else {
                log.info("Database already contains data. Skipping data initialization.");
                log.info("Current faculties count: {}", facultyRepository.count());
                log.info("Current rooms count: {}", roomRepository.count());
            }
        } catch (Exception e) {
            log.error("Error during faculties/rooms data initialization: {}", e.getMessage(), e);
            // Vẫn tiếp tục để tạo user admin/user — tránh login JMeter 401 kiểu 'Tài khoản không tồn tại'
        }

        ensureFacultyCn1();

        try {
            initializeDefaultUsers();
        } catch (Exception e) {
            log.error("Error initializing default users: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Khởi tạo users mặc định
     */
    private void initializeDefaultUsers() {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@ptit.edu.vn")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            
            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        } else {
            log.info("Admin user already exists, skipping creation.");
        }
        
        // Create default regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@ptit.edu.vn")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Regular User")
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            
            userRepository.save(user);
            log.info("Default user created: username=user, password=user123");
        } else {
            log.info("Regular user already exists, skipping creation.");
        }
    }
    
    /**
     * Kiểm tra xem database có rỗng không
     */
    private boolean isDatabaseEmpty() {
        return facultyRepository.count() == 0 && roomRepository.count() == 0;
    }

    /**
     * Luôn đảm bảo có khoa CN1 sau mỗi lần khởi động — JMeter/smoke có thể đã DELETE mã CN1 trong khi DB
     * vẫn không trống nên không chạy lại được toàn bộ data.sql.
     */
    private void ensureFacultyCn1() {
        try {
            if (facultyRepository.existsById("CN1")) {
                return;
            }
            Faculty f = new Faculty();
            f.setId("CN1");
            f.setFacultyName("Công nghệ thông tin");
            facultyRepository.save(f);
            log.info("Đã khôi phục/tạo khoa CN1 (thiếu trong DB nhưng vẫn có dữ liệu khác).");
        } catch (Exception e) {
            log.warn("Không khôi phục được khoa CN1: {}", e.getMessage());
        }
    }

    /**
     * Force reload data (có thể gọi từ endpoint admin nếu cần)
     */
    public void forceReloadData() {
        try {
            log.info("Force reloading data from data.sql...");
            
            Connection connection = dataSource.getConnection();
            ClassPathResource resource = new ClassPathResource("data.sql");
            
            if (resource.exists()) {
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("Force data reload completed successfully.");
            }
            
            connection.close();
            
        } catch (Exception e) {
            log.error("Error during force data reload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reload data", e);
        }
    }
}