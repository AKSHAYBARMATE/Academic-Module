package com.academic.controller;

import com.academic.request.HolidayRequest;
import com.academic.response.HolidayCheckResponse;
import com.academic.response.HolidayResponse;
import com.academic.response.StandardResponse;
import com.academic.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing REST APIs for Holiday configuration and management.
 */
@RestController
@RequestMapping("/api/v1/academic-module/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * Create / Save a new holiday.
     * Corresponds to the "Save Holiday" button in the Add Holiday form.
     */
    @PostMapping({"/saveHoliday", "/createHoliday"})
    public ResponseEntity<StandardResponse<HolidayResponse>> saveHoliday(@Valid @RequestBody HolidayRequest request) {
        StandardResponse<HolidayResponse> response = holidayService.createHoliday(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing holiday by ID.
     */
    @PutMapping("/updateHoliday/{id}")
    public ResponseEntity<StandardResponse<HolidayResponse>> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request
    ) {
        return ResponseEntity.ok(holidayService.updateHoliday(id, request));
    }

    /**
     * Get holiday details by ID.
     */
    @GetMapping({"/getHoliday/{id}", "/{id}"})
    public ResponseEntity<StandardResponse<HolidayResponse>> getHolidayById(@PathVariable Long id) {
        return ResponseEntity.ok(holidayService.getHolidayById(id));
    }

    /**
     * Get paginated list of holidays with optional filters.
     */
    @GetMapping({"/getAllHolidays", ""})
    public ResponseEntity<StandardResponse<Page<HolidayResponse>>> getAllHolidays(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String holidayType,
            @RequestParam(required = false) String applicableTo,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(holidayService.getAllHolidays(
                search, holidayType, applicableTo, classId, year, month, page, size
        ));
    }

    /**
     * Get full unpaged list of holidays (useful for dropdowns or calendar views).
     */
    @GetMapping("/list")
    public ResponseEntity<StandardResponse<List<HolidayResponse>>> getHolidaysList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long classId
    ) {
        return ResponseEntity.ok(holidayService.getAllHolidaysList(year, month, classId));
    }

    /**
     * Soft delete holiday by ID.
     */
    @DeleteMapping({"/deleteHoliday/{id}", "/{id}"})
    public ResponseEntity<StandardResponse<Map<String, Object>>> deleteHoliday(@PathVariable Long id) {
        return ResponseEntity.ok(holidayService.deleteHoliday(id));
    }

    /**
     * Check if a specific date is a holiday for all school or for a given class.
     */
    @GetMapping("/checkIsHoliday")
    public ResponseEntity<StandardResponse<HolidayCheckResponse>> checkIsHoliday(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId
    ) {
        return ResponseEntity.ok(holidayService.checkIsHoliday(date, classId));
    }

    /**
     * Get upcoming holidays from today.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<StandardResponse<List<HolidayResponse>>> getUpcomingHolidays(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long classId
    ) {
        return ResponseEntity.ok(holidayService.getUpcomingHolidays(limit, classId));
    }
}

