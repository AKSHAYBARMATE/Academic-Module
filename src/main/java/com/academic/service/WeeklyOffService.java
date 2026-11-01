package com.academic.service;

import com.academic.request.WeeklyOffRequest;
import com.academic.response.StandardResponse;
import com.academic.response.WeeklyOffCheckResponse;
import com.academic.response.WeeklyOffResponse;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyOffService {

    /**
     * Save/Configure Weekly Off Days (e.g., ["SUNDAY"]).
     */
    StandardResponse<WeeklyOffResponse> saveWeeklyOff(WeeklyOffRequest request);

    /**
     * Get the current Weekly Off configuration for all 7 days.
     */
    StandardResponse<WeeklyOffResponse> getWeeklyOffConfiguration();

    /**
     * Check if a specific date is a weekly off.
     */
    StandardResponse<WeeklyOffCheckResponse> checkIsWeeklyOff(LocalDate date);

    /**
     * Get all weekly off dates for a given month and year.
     */
    StandardResponse<List<LocalDate>> getWeeklyOffDatesForMonth(int year, int month);
}
