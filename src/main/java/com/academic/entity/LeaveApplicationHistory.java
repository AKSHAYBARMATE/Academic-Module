package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_leave_application_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leaveApplicationId;

    private String action;           // DRAFT_CREATED, SCHEDULE_UPDATED, SUBMITTED, RECOMMENDED, APPROVED, REJECTED

    private Long actionByUserId;
    private String actionByRole;

    @Column(length = 1000)
    private String comments;

    private String attachmentPath;

    private LocalDateTime actionDate;
}
