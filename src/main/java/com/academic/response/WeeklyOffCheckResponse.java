package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeeklyOffCheckResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private DayOfWeek dayOfWeek;
    private String dayName;
    private boolean isWeeklyOff;
}
