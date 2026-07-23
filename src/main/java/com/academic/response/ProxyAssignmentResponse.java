package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyAssignmentResponse {

    private Long id;

    // Template link
    private Long templateId;
    private String templateName;
    private Long substituteTeacherId;
    private String substituteTeacherName;

    // Timetable slot
    private Long timetableId;
    private String timetableName;
    private Long slotId;
    private Boolean isClassTeacher;
    private String startTime;
    private String endTime;
    private Integer dayOfWeek;
    private String dayName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate proxyDate;

    // Original Teacher
    private Long originalTeacherId;
    private String originalTeacherName;

    // Context metadata
    private Long subjectId;
    private String subjectName;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;

    // Details
    private String remarks;
    private String status; // e.g. ACTIVE

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
