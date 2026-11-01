package com.academic.controller;

import com.academic.request.WeeklyOffRequest;
import com.academic.response.StandardResponse;
import com.academic.response.WeeklyOffCheckResponse;
import com.academic.response.WeeklyOffResponse;
import com.academic.service.WeeklyOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller exposing REST APIs for "Configure Weekly Off Days".
 * Matches the 7-day card selection UI (Sun to Sat) and "Save Weekly Off" button.
 */
@RestController
@RequestMapping("/api/v1/academic-module/weekly-off")
@RequiredArgsConstructor
public class WeeklyOffController {

    private final WeeklyOffService weeklyOffService;

    /**
     * Save / Configure Weekly Off Days.
     * Corresponds to the "Save Weekly Off" button in the UI.
     * e.g. Body: { "days": ["SUNDAY"] }
     */
    @PostMapping({"/saveWeeklyOff", "/configure"})
    public ResponseEntity<StandardResponse<WeeklyOffResponse>> saveWeeklyOff(
            @RequestBody WeeklyOffRequest request
    ) {
        return ResponseEntity.ok(weeklyOffService.saveWeeklyOff(request));
    }

    /**
     * Get the current Weekly Off configuration.
     * Returns all 7 cards (Sun..Sat) with isWeeklyOff flag and configured summary text.
     */
    @GetMapping({"/getConfiguration", ""})
    public ResponseEntity<StandardResponse<WeeklyOffResponse>> getConfiguration() {
        return ResponseEntity.ok(weeklyOffService.getWeeklyOffConfiguration());
    }

    /**
     * Check if a specific date is a weekly off.
     */
    @GetMapping("/checkIsWeeklyOff")
    public ResponseEntity<StandardResponse<WeeklyOffCheckResponse>> checkIsWeeklyOff(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(weeklyOffService.checkIsWeeklyOff(date));
    }

    /**
     * Get all weekly off dates for a given month and year.
     */
    @GetMapping("/monthlyDates")
    public ResponseEntity<StandardResponse<List<LocalDate>>> getMonthlyDates(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(weeklyOffService.getWeeklyOffDatesForMonth(year, month));
    }
}
