package com.academic.request;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private String startTime;
    private String endTime;
    private Long subjectId;
    private String teacherId;
    private String roomId;
    private String notes;
}
