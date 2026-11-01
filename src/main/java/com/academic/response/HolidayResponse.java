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
 * Detailed response for Holiday API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayResponse {

    private Long id;
    private String holidayName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String formattedDateRange; // e.g. "02 Nov 2026 -> 05 Nov 2026"
    private Long totalDays;

    private String holidayType;
    private String applicableTo; // "ALL_SCHOOL" or "SELECTED_CLASSES"
    private List<Long> selectedClassIds;
    private List<String> selectedClassNames;

    private String description;
    private Long sessionId;
    private String sessionName;

    private String status; // "UPCOMING", "ONGOING", "COMPLETED"

    // WHO Columns from BaseEntity
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdOn;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime modifiedAt;

    private Boolean isDeleted;
}

