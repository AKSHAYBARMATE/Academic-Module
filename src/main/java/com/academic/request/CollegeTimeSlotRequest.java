package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimeSlotRequest {

    private Long id;

    @NotBlank(message = "Slot label is required")
    private String slotLabel; // e.g. "Period 1", "Morning Refreshment"

    @NotBlank(message = "Time slot range is required")
    private String timeSlot; // e.g. "09:00 - 10:00 AM"

    private String startTime; // "09:00"

    private String endTime; // "10:00"

    private Integer durationMinutes; // e.g. 60, 15

    private Boolean isBreak; // true for Recess / Lunch

    private Integer slotOrder; // order in matrix

    private String slotType; // "Standard Lecture", "Extended Block", "Recess"

    private String status; // "Active", "Inactive"
}
