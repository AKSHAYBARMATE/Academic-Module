package com.academic.request;

import com.academic.utility.LooseLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private Long id;
    private String startTime;
    private String endTime;

    @JsonDeserialize(using = LooseLongDeserializer.class)
    private Long subjectId;
    
    private String subjectName;

    @JsonDeserialize(using = LooseLongDeserializer.class)
    private Long teacherId;

    private String teacherName;
    private String roomId;
    private Integer day;
    private String notes;
}
