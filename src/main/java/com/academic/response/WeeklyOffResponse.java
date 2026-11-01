package com.academic.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Response DTO for the "Configure Weekly Off Days" screen.
 * Directly supports the 7-day card view and summary text shown in the UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeeklyOffResponse {

    /**
     * Text shown at the bottom: e.g. "Sunday" or "Sunday, Saturday" or "None"
     */
    private String configuredWeeklyOff;

    /**
     * List of configured DayOfWeek names e.g. ["SUNDAY", "SATURDAY"]
     */
    private List<String> configuredDays;

    /**
     * All 7 days in order (Sunday to Saturday) with their selection status for the UI cards
     */
    private List<DayCardDto> days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayCardDto {
        private String shortName; // "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
        private String dayName;   // "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        private DayOfWeek dayOfWeek;
        private boolean isWeeklyOff; // checked/selected status
    }
}
