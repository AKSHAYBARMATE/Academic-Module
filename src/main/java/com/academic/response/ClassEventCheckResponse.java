package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response when checking if an event exists on a specific date for attendance grid ("E" column).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassEventCheckResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private boolean isEvent;
    private String eventTag; // "E" for attendance grid
    private Long eventId;
    private String eventTitle; // e.g. "Parent Meeting"
    private String eventType; // e.g. "Parent-Teacher Meeting"
    private String applicableTo;
    private String description;
}

