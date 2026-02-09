package com.academic.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "exam_schedule",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"session_id", "exam_type_id", "examTitle"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Exam Title */
    @Column(nullable = false)
    private String examTitle; // Annual Examination 2024

    /* Academic Year */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    /* Exam Type */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", nullable = false)
    private CommonMaster examType;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private String status; // DRAFT / PUBLISHED

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
