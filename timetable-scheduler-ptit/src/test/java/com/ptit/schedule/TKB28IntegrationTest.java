package com.ptit.schedule;

import com.ptit.schedule.controller.ScheduleController;
import com.ptit.schedule.exception.GlobalExceptionHandler;
import com.ptit.schedule.repository.*;
import com.ptit.schedule.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TKB28 - Integration test cho ScheduleController.getAllSchedules() ClassCastException
 *
 * Bug: ScheduleController.getAllSchedules() cast authentication.getPrincipal() thanh User
 * ma khong kiem tra type:
 *
 *   User currentUser = (User) authentication.getPrincipal();
 *
 * Neu principal la String ("anonymousUser", token, etc.) thi se nem ClassCastException.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.DisplayName.class)
class TKB28IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private TKBTemplateRepository tkbTemplateRepository;

    @MockBean
    private SubjectRepository subjectRepository;

    @MockBean
    private DataLoaderService dataLoaderService;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private RedisService redisService;

    @Test
    @DisplayName("TKB28_getAllSchedules_ClassCastException_Fails")
    void tkb28() throws Exception {
        // Setup: Tao authentication voi principal la String, khong phai User entity
        // Controller se goi: User currentUser = (User) authentication.getPrincipal();
        // -> ClassCastException vi principal la String
        RequestPostProcessor setStringPrincipal = request -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "anonymousUser",  // String, not User entity
                    null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };

        // Act: goi GET /api/schedules voi authentication co principal la String
        var result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/schedules")
                        .with(setStringPrincipal));

        // Assert: HTTP 500 (ClassCastException bi nem boi controller)
        // Bug la REAL - he thong chua kiem tra type principal
        result.andExpect(res -> {
            assertEquals(500, res.getResponse().getStatus(),
                    "Controller phai tra 500 vi ClassCastException");
        });
    }
}
