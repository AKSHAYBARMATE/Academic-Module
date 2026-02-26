package com.schoolerp.feemodule.entity;

import com.schoolerp.feemodule.response.FeePaymentResponse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fee_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student studentId;

    @ManyToOne
    @JoinColumn(name = "fee_structure_id")
    private FeeStructure feeStructure;

    @OneToMany(mappedBy = "feePayment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeePaymentCategory> selectedCategories;
    private BigDecimal baseAmount;
    private BigDecimal discount;
    private BigDecimal lateFee;
    private BigDecimal netAmount;
    private String paymentMethod;
    private String transactionId;
    private String receiptNo;
    private LocalDate dueDate;
    private String paymentRemarks;
    private LocalDateTime paidAt;
    private Integer academicYear;
    private Long accountHeadId;
}
