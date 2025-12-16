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
    private String subjectName;
    private String teacherId;
    private String roomId;
    private Integer day;
    private String notes;
}
