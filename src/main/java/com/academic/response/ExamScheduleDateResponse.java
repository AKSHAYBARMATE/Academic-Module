package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleDateResponse {

    private Long id;
    private Long examScheduleId;
    private String examScheduleTitle;

    private Integer sessionId;
    private String session;

    private Integer classId;
    private String className;

    private Integer sectionId;
    private String sectionName;

    private Integer subjectId;
    private String subjectName;
    private String subjectCode;

    private LocalDate examDate;
    private String formattedDate; // e.g. "05 Oct 2026, Monday"
    private String dayOfWeek;     // e.g. "Monday"

    private String startTime;
    private String endTime;
    private String timeRange; // e.g. "09:00 AM - 12:00 PM"

    private Integer invigilatorId;
    private String invigilatorName;
    private String invigilatorCode;

    private String roomNo;
    private Integer maxMarks;
    private Integer passingMarks;

    private String status;
    private Boolean isActive;
}
