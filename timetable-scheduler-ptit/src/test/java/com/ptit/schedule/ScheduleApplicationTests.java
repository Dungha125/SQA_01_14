package com.ptit.schedule;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ScheduleApplicationTests {

    @Test
    void contextLoads() {
        // Chỉ kiểm tra Spring context khởi động thành công
    }

}
