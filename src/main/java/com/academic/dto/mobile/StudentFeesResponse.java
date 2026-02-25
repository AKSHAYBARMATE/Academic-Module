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
public class StudentFeesResponse {
    private String academicYear;
    private Double totalFees;
    private Double paidAmount;
    private Double remainingAmount;
    private List<FeeItemDto> feeStructure;
    private List<PaymentHistoryDto> paymentHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeItemDto {
        private String feeType;
        private Double amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistoryDto {
        private Long id;
        private String title;
        private String date;
        private String paymentMethod;
        private Double amount;
        private String status;
        private String receiptUrl;
    }
}
