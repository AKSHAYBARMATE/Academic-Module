package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonthlyWeeklyOffResponse {

    private int year;
    private int month;
    private String monthName;
    private int totalOffDays;
    private List<OffDateItem> offDates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OffDateItem {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private String dayName;
        private int occurrenceInMonth;
        private String offType;
        private String description;
    }
}

