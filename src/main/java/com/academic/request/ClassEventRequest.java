package com.academic.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for creating and updating a Class Event.
 * Matches fields from the "Add Class Event" form.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassEventRequest {

    @NotBlank(message = "Event Type is required")
    private String eventType; // e.g. "Parent-Teacher Meeting", "Annual Function", "Sports Day"

    @NotBlank(message = "Event Title is required")
    private String eventTitle; // e.g. "Parent Meeting"

    @NotNull(message = "Event Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Optional end date for multi-day event

    @NotBlank(message = "Applicable To is required (ENTIRE_SCHOOL, SELECTED_CLASSES, SELECTED_SECTIONS)")
    private String applicableTo; // "Entire School", "Selected Classes", "Selected Sections"

    private List<Long> classIds; // When applicableTo is SELECTED_CLASSES or SELECTED_SECTIONS

    private List<Long> sectionIds; // When applicableTo is SELECTED_SECTIONS

    private String description;

    private String academicSession; // e.g. "2026-27"

    private Long sessionId;
}

