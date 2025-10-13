package com.academic.response;


import com.academic.request.TimeSlotDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeTableResponse {
    private Long id;
    private String timetableName;
    private Integer classId;
    private String className; // new
    private Integer daysCoveredId;
    private Integer sectionId;
    private String sectionName; // new
    private List<TimeSlotResponse> slots;
}


