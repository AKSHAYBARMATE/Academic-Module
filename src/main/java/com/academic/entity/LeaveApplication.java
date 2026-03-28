package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_leave_application")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Application Info
    private String applicationNo; // LR-2024-001

    private Long employeeId;
    private Long departmentId;

    private String leaveCategory; // PRIOR / POST / URGENT
    private String leaveType;     // CASUAL / SICK / EARNED / LWP

    private LocalDate fromDate;
    private LocalDate toDate;

    @Column(length = 1000)
    private String reason;

    private String attachmentPath;

    // ===== APPROVAL STATE =====
    private String status;
    /*
        DRAFT
        SUBMITTED
        PENDING
        RECOMMENDED
        APPROVED
        REJECTED
     */

    private Long currentApproverUserId;
    private String currentApproverRole;

    private Long lastActionByUserId;
    private String lastActionByRole;

    @Column(length = 1000)
    private String lastActionComments;

    private String lastActionAttachment;
    private LocalDateTime lastActionDate;

    // Meta
    private Boolean isDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
