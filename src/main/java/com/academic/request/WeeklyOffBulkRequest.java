package com.academic.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for configuring multiple weekly off days in bulk
 * (e.g., configuring Sunday all-day + 2nd and 4th Saturday).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyOffBulkRequest {

    @NotEmpty(message = "At least one weekly off configuration must be provided")
    @Valid
    private List<WeeklyOffRequest> configs;
}

