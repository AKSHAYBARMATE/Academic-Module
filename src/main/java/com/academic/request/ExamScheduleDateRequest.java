package com.academic.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleDateRequest {

    private Long id; // Optional, present in case of update
    private Long examScheduleId; // Parent Exam Schedule ID (optional)
    private Integer sessionId; // Academic Session ID
    private Integer classId; // Class ID (from common_master)
    private Integer sectionId; // Section ID (optional, from common_master)
    private Integer subjectId; // Subject ID (from subjects table)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate; // Date of Exam

    private String startTime; // e.g. "09:00 AM" or "09:00"
    private String endTime; // e.g. "12:00 PM" or "12:00"

    private Integer invigilatorId; // Staff ID (optional)
    private String roomNo; // Room number / Hall number

    private Integer maxMarks; // Maximum marks
    private Integer passingMarks; // Minimum passing marks

    private String status; // SCHEDULED / ONGOING / COMPLETED / CANCELLED
}
