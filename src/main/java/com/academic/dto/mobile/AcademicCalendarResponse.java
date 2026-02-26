package com.academic.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicCalendarResponse {

    private List<CalendarEventDto> events;
    private int totalEvents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarEventDto {
        private Long id;
        private String eventName;
        private String date; // ISO date: "2026-03-15"
        private String type; // HOLIDAY / EXAM / EVENT etc.
        private String duration; // e.g. "1 Day" or "2 Days"
        private String status; // e.g. UPCOMING / COMPLETED
    }
}
