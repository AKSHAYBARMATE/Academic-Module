package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "grading_rules",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "gradeName"),
        @UniqueConstraint(columnNames = {"minPercentage", "maxPercentage"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gradeName;      // A+, A, B, C, F

    @Column(nullable = false)
    private Double gradePoint;     // 4.0, 3.7, 0.0

    @Column(nullable = false)
    private Integer minPercentage; // 0 - 100

    @Column(nullable = false)
    private Integer maxPercentage;

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
