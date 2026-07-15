package com.academic.request;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private Long id;
    private String startTime;
    private String endTime;

    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String roomId;
    private Integer day;
    private String notes;
}
