package com.academic.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTableRequest {
    @NotBlank
    private String timetableName;

    private Long sectionId;

    @NotNull
    private Long classId;

    @NotNull
    private Long daysCoveredId;

    private List<TimeSlotDTO> slots;
}
