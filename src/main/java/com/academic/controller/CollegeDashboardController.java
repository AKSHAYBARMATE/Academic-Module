package com.academic.controller;

import com.academic.response.CollegeDashboardResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/college-dashboard")
@RequiredArgsConstructor
@Slf4j
public class CollegeDashboardController {

    private final CollegeDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<StandardResponse<CollegeDashboardResponse>> getDashboard(
            @RequestParam(required = false) String academicYear) {
        log.info("REST request to fetch college academic dashboard data for year: {}", academicYear);
        CollegeDashboardResponse response = dashboardService.getDashboardData(academicYear);
        return ResponseEntity.ok(StandardResponse.success(response, "College dashboard data retrieved successfully"));
    }
}
