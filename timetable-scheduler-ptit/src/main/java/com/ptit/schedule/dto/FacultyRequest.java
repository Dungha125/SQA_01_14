package com.ptit.schedule.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FacultyRequest {
    
    @NotBlank(message = "Faculty name is required")
    @Size(max = 255, message = "Faculty name must not exceed 255 characters")
    private String facultyName;
}
