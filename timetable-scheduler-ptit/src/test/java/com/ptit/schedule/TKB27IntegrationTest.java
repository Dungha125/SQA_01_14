package com.ptit.schedule;

import com.ptit.schedule.entity.*;
import com.ptit.schedule.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TKB27 - Integration test cho ScheduleRepository.findByMajor() - Semantic Bug
 *
 * Bug: findByMajor() query:
 *   SELECT s FROM Schedule s WHERE s.subject.major.majorCode = :majorCode
 *
 * Lay gia tri tu Subject.major.majorCode (entity relationship).
 *
 * Nhung Schedule.major la String column, duoc luu truc tiep tu frontend
 * (SaveScheduleRequest.major), co the la gia tri khac voi Subject.major.majorCode.
 *
 * Ket qua: findByMajor("CNTT") tra ve 0 khi Schedule.major="CNTT" nhung
 * Subject.major.majorCode="CNTT-TT" hoac khac.
 *
 * Test nay chay voi H2 in-memory (khong mock repository) de phat hien bug.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.DisplayName.class)
class TKB27IntegrationTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private TKBTemplateRepository tkbTemplateRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("TKB27_findByMajor_SemanticBug_ReturnsZeroWhenShouldReturnOne")
    void tkb27() {
        // Setup: Tao du lieu thuc te
        // Frontend gui SaveScheduleRequest voi major="CNTT"
        // Backend luu vao Schedule.major = "CNTT" (String column)

        Faculty faculty = new Faculty();
        faculty.setId("CNTT");
        faculty.setFacultyName("Cong nghe thong tin");
        faculty = facultyRepository.save(faculty);

        Semester semester = semesterRepository.save(Semester.builder()
                .semesterName("HK1").academicYear("2024-2025").build());

        // Subject co major.majorCode = "CNTT-TT" (vi du: ma nganh day du)
        Major subjectMajor = majorRepository.save(Major.builder()
                .majorCode("CNTT-TT").majorName("Cong nghe thong tin - Thuc hanh")
                .classYear("2024").numberOfStudents(100)
                .faculty(faculty).build());

        Subject subject = subjectRepository.save(Subject.builder()
                .subjectCode("INT2201").subjectName("Lap trinh Huong doi tuong")
                .major(subjectMajor).semester(semester).build());

        TKBTemplate template = tkbTemplateRepository.save(TKBTemplate.builder()
                .templateId("T001").totalPeriods(45)
                .dayOfWeek(2).kip(1).startPeriod(1)
                .periodLength(3).weekSchedule("[1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]")
                .rowOrder(1).semester(semester).build());

        User user = userRepository.save(User.builder()
                .username("testuser_" + System.nanoTime())
                .email("test_" + System.nanoTime() + "@test.com")
                .password("password").role(Role.USER).enabled(true).build());

        // Frontend gui len: major="CNTT"
        // Backend luu vao Schedule.major = "CNTT" (String column)
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .subject(subject)
                .major("CNTT")         // Frontend gui: "CNTT"
                .classNumber(1)
                .tkbTemplate(template)
                .user(user)
                .build());

        // Act: goi findByMajor("CNTT")
        // Query: WHERE s.subject.major.majorCode = 'CNTT'
        // Ket qua: tra ve 0! Vi Subject.major.majorCode = "CNTT-TT", khong phai "CNTT"
        List<Schedule> result = scheduleRepository.findByMajor("CNTT");

        // Assert: BUG! Query tra ve 0, nhung ta vua luu 1 Schedule voi major="CNTT"
        // Bug sematic: query sai truong can lay gia tri
        // - Schedule.major = "CNTT" (tu frontend, dang can tim)
        // - Subject.major.majorCode = "CNTT-TT" (query lay tu day)
        assertEquals(0, result.size(),
                "BUG: findByMajor('CNTT') tra ve 0 vi query lay Subject.major.majorCode"
                        + " = 'CNTT-TT' thay vi Schedule.major = 'CNTT'. "
                        + "Du lieu ton tai nhung query khong tim thay.");
    }
}
