package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Class Event APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassEventResponse {

    private Long id;
    private String eventType;
    private String eventTitle;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String formattedDate; // e.g. "12 Nov 2026"
    private String applicableTo; // "ENTIRE_SCHOOL", "SELECTED_CLASSES", "SELECTED_SECTIONS"

    private List<Long> classIds;
    private List<String> classNames;

    private List<Long> sectionIds;
    private List<String> sectionNames;

    private String description;
    private String academicSession;
    private Long sessionId;

    private String status; // "UPCOMING", "TODAY", "COMPLETED"

    // WHO Columns from BaseEntity
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdOn;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime modifiedAt;

    private Boolean isDeleted;
}

