package com.ptit.schedule.config;

import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.entity.Major;
import com.ptit.schedule.entity.Semester;
import com.ptit.schedule.entity.Subject;
import com.ptit.schedule.repository.FacultyRepository;
import com.ptit.schedule.repository.MajorRepository;
import com.ptit.schedule.repository.SemesterRepository;
import com.ptit.schedule.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Bổ sung dữ liệu tối thiểu (học kỳ, ngành, môn) khi DB đã có faculties/rooms nhưng thiếu bảng khác —
 * để test API/JMeter không 404. Idempotent: chỉ tạo nếu chưa tồn tại.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class AdditionalTestDataSeed implements CommandLineRunner {

    public static final String SEED_SEMESTER_NAME = "HK1";
    public static final String SEED_ACADEMIC_YEAR = "2024-2025";
    private static final String SEED_MAJOR_CODE = "CNPM";
    private static final String SEED_CLASS_YEAR = "2024";
    private static final String SEED_FACULTY_ID = "CN1";
    private static final String SEED_SUBJECT_CODE = "SEED001";
    /** Môn chung — GET /api/subjects/common-subjects cần ít nhất một bản ghi is_common = true */
    private static final String SEED_COMMON_SUBJECT_CODE = "SEEDCOM";

    private final SemesterRepository semesterRepository;
    private final MajorRepository majorRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            Semester semester = ensureSemester();
            Major major = ensureMajor();
            if (semester != null && major != null) {
                ensureSubject(semester, major);
                ensureCommonSubject(semester, major);
            }
        } catch (Exception e) {
            log.warn("AdditionalTestDataSeed: {}", e.getMessage());
        }
    }

    private Semester ensureSemester() {
        if (semesterRepository.existsBySemesterNameAndAcademicYear(SEED_SEMESTER_NAME, SEED_ACADEMIC_YEAR)) {
            return semesterRepository
                    .findBySemesterNameAndAcademicYear(SEED_SEMESTER_NAME, SEED_ACADEMIC_YEAR)
                    .map(s -> {
                        if (s.getId() != 1L) {
                            log.warn("HK1/{} đang có DB id={}; nếu JMeter dùng semesterId=1 mà GET /api/semesters/1 trả 404, đổi UDVs semesterId theo id trên.",
                                    SEED_ACADEMIC_YEAR, s.getId());
                        }
                        return s;
                    })
                    .orElse(null);
        }
        Semester s = Semester.builder()
                .semesterName(SEED_SEMESTER_NAME)
                .academicYear(SEED_ACADEMIC_YEAR)
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .isActive(true)
                .description("Seed tự động (JMeter / dev)")
                .build();
        Semester saved = semesterRepository.save(s);
        log.info("Đã tạo học kỳ seed: {} — {} (id={})", SEED_SEMESTER_NAME, SEED_ACADEMIC_YEAR, saved.getId());
        return saved;
    }

    private Major ensureMajor() {
        Optional<Faculty> fac = facultyRepository.findById(SEED_FACULTY_ID);
        if (fac.isEmpty()) {
            log.warn("Không có khoa '{}' trong DB — bỏ qua seed ngành/môn. Chạy lại sau khi có data.sql (faculties).", SEED_FACULTY_ID);
            return null;
        }
        Optional<Major> existing = majorRepository.findByMajorCodeAndClassYear(SEED_MAJOR_CODE, SEED_CLASS_YEAR);
        if (existing.isPresent()) {
            return existing.get();
        }
        Major m = Major.builder()
                .majorCode(SEED_MAJOR_CODE)
                .classYear(SEED_CLASS_YEAR)
                .majorName("Công nghệ phần mềm (seed)")
                .numberOfStudents(60)
                .faculty(fac.get())
                .build();
        Major saved = majorRepository.save(m);
        log.info("Đã tạo ngành seed: {} — khóa {} (id={})", SEED_MAJOR_CODE, SEED_CLASS_YEAR, saved.getId());
        return saved;
    }

    private void ensureSubject(Semester semester, Major major) {
        if (subjectRepository.existsBySubjectCode(SEED_SUBJECT_CODE)) {
            return;
        }
        Subject sub = Subject.builder()
                .subjectCode(SEED_SUBJECT_CODE)
                .subjectName("Môn seed kiểm thử API")
                .studentsPerClass(50)
                .numberOfClasses(2)
                .credits(3)
                .theoryHours(30)
                .exerciseHours(10)
                .projectHours(0)
                .labHours(10)
                .selfStudyHours(30)
                .department("CNTT")
                .examFormat("Thi TN")
                .programType("Chính quy")
                .major(major)
                .semester(semester)
                .isCommon(false)
                .build();
        Subject saved = subjectRepository.save(sub);
        log.info("Đã tạo môn seed {} (id={}) gắn HK {} / ngành {}", SEED_SUBJECT_CODE, saved.getId(),
                semester.getSemesterName(), major.getMajorCode());
    }

    private void ensureCommonSubject(Semester semester, Major major) {
        if (subjectRepository.existsBySubjectCode(SEED_COMMON_SUBJECT_CODE)) {
            return;
        }
        Subject sub = Subject.builder()
                .subjectCode(SEED_COMMON_SUBJECT_CODE)
                .subjectName("Môn chung seed (JMeter / API smoke)")
                .studentsPerClass(45)
                .numberOfClasses(4)
                .credits(2)
                .theoryHours(30)
                .exerciseHours(0)
                .projectHours(0)
                .labHours(0)
                .selfStudyHours(30)
                .department("CNTT")
                .examFormat("Thi TN")
                .programType("Chính quy")
                .major(major)
                .semester(semester)
                .isCommon(true)
                .build();
        Subject saved = subjectRepository.save(sub);
        log.info("Đã tạo môn chung seed {} (id={}) cho {} / {}",
                SEED_COMMON_SUBJECT_CODE, saved.getId(),
                semester.getSemesterName(), semester.getAcademicYear());
    }
}
