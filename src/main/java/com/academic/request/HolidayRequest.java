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
 * Request payload for creating and updating a Holiday.
 * Matches fields from the Add Holiday form.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequest {

    @NotBlank(message = "Holiday name is required")
    private String holidayName;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotBlank(message = "Holiday type is required")
    private String holidayType; // e.g., "Public Holiday", "National Holiday", "Festival", "Institutional Holiday"

    @NotBlank(message = "Applicable To is required (ALL_SCHOOL or SELECTED_CLASSES)")
    private String applicableTo; // "ALL_SCHOOL" or "SELECTED_CLASSES" (also accepts "All School", "Selected Classes")

    private List<Long> selectedClassIds;

    private String description;

    private Long sessionId;
}

