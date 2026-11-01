package com.academic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayCheckResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private boolean isHoliday;
    private Long holidayId;
    private String holidayName;
    private String holidayType;
    private String applicableTo;
    private String description;
}

