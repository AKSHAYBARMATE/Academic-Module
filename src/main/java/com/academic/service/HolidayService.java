package com.academic.service;

import com.academic.request.HolidayRequest;
import com.academic.response.HolidayCheckResponse;
import com.academic.response.HolidayResponse;
import com.academic.response.StandardResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HolidayService {

    StandardResponse<HolidayResponse> createHoliday(HolidayRequest request);

    StandardResponse<HolidayResponse> updateHoliday(Long id, HolidayRequest request);

    StandardResponse<HolidayResponse> getHolidayById(Long id);

    StandardResponse<Page<HolidayResponse>> getAllHolidays(
            String search,
            String holidayType,
            String applicableTo,
            Long classId,
            Integer year,
            Integer month,
            int page,
            int size
    );

    StandardResponse<List<HolidayResponse>> getAllHolidaysList(Integer year, Integer month, Long classId);

    StandardResponse<Map<String, Object>> deleteHoliday(Long id);

    StandardResponse<HolidayCheckResponse> checkIsHoliday(LocalDate date, Long classId);

    StandardResponse<List<HolidayResponse>> getUpcomingHolidays(Integer limit, Long classId);
}

