package com.academic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimeSlotResponse {

    private Long id;
    private String slotLabel;
    private String timeSlot;
    private String startTime;
    private String endTime;
    private Integer durationMinutes;
    private Boolean isBreak;
    private Integer slotOrder;
    private String slotType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
