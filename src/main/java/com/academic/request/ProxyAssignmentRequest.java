package com.academic.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProxyAssignmentRequest {

    @NotNull(message = "Proxy template ID is required")
    private Long templateId;

    @NotNull(message = "Target timetable plan ID is required")
    private Long timetableId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Day of week is required")
    private Integer dayOfWeek; // 1 = Monday, etc.

    @NotNull(message = "Proxy active date is required")
    private LocalDate proxyDate;

    private Long originalTeacherId;

    private String originalTeacherName;

    private Long subjectId;
    private String subjectName;

    private Long classId;
    private Long sectionId;

    private String remarks; // assignment remarks/notes
    private Boolean isClassTeacher; // check if against class teacher
}
