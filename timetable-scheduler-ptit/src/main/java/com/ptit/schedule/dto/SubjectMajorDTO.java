package com.ptit.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMajorDTO {
    String subjectCode;
    String subjectName;
    String majorCode;
    String classYear;
    Integer theoryHours;
    Integer exerciseHours;
    Integer labHours;
    Integer projectHours;
    Integer selfStudyHours;
    Integer numberOfStudents;
    Integer studentPerClass;
}
