package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private String startTime;
    private String endTime;
    private Long subjectId;
    private String teacherId;
    private String roomId;
    private Integer day;
    private String notes;
}
