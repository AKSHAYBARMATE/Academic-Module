package com.academic.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarkItemDto {

    private Long id; // Mark entry record ID if existing

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private String rollNo; // Admission no or roll no e.g. "1"
    private String studentName; // e.g. "AARAV"

    // Raw or numeric marks
    private Double theoryMarks;
    private Double practicalMarks;
    private Double internalMarks;

    // String representations for frontend (to allow "AB", "EX", "NA", or numeric string)
    private String theoryDisplay;
    private String practicalDisplay;
    private String internalDisplay;

    // Dynamic marks by component name (e.g. {"Theory": 65, "Practical": 8, "Internal": 18, "Viva": 9})
    private java.util.Map<String, Double> componentMarks;
    private java.util.Map<String, String> componentDisplays;

    private Double totalMarks; // Auto-calculated e.g. 91
    private Double percentage; // Auto-calculated e.g. 91.0
    private String attendanceStatus; // "PRESENT", "AB", "EX", "NA"
    private String grade; // e.g. "A+", "A", "C", "-"
    private String result; // "Pass", "Fail", "Absent", "Exempt"
    private String remarks; // Notes
}

