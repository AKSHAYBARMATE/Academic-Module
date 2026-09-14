package com.academic.service;

import com.academic.response.CollegeDashboardResponse;

public interface CollegeDashboardService {

    CollegeDashboardResponse getDashboardData(String academicYear);
}
