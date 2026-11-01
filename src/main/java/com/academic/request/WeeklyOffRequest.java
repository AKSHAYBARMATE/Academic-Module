package com.academic.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for configuring Weekly Off days.
 * Matches the "Configure Weekly Off Days" UI.
 * e.g. { "days": ["SUNDAY"] } or { "days": ["SUNDAY", "SATURDAY"] }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyOffRequest {

    /**
     * List of selected weekly off days:
     * e.g. ["SUNDAY"], ["Sunday", "Saturday"], ["SUN", "SAT"]
     */
    private List<String> days;

    /**
     * Single day (if toggling or saving a single day):
     * e.g. "SUNDAY" or "Sunday"
     */
    private String dayOfWeek;
}
