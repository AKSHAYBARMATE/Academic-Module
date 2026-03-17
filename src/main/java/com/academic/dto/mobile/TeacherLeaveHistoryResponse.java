package com.academic.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherLeaveHistoryResponse {
    private List<LeaveItemDto> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveItemDto {
        private String leaveType;
        private String dateRange; // "05 Oct - 06 Oct (2 Days)"
        private String reason;
        private String status; // "APPROVED", "PENDING", "REJECTED"
        private String rejectionReason;
    }
}
