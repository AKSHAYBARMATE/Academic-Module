package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "programs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code; // e.g. "B.Tech (CSE)"

    private String name; // e.g. "Computer Science & Engineering"

    private String degreeCode; // e.g. "B.Tech" (Parent Degree code reference)

    private String degreeType; // e.g. "UG", "PG", "Diploma", "Doctorate", "Integrated"

    private String duration; // e.g. "4 Years", "3 Years"

    private Integer totalSemesters; // e.g. 8, 6

    private Integer intake; // e.g. 120, 60 (Seats / Year)

    private String accreditation; // e.g. "NBA & AICTE Accredited", "AICTE Approved"

    @Column(length = 1000)
    private String description;

    private String status; // Active / Inactive

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "Active";
        }
        if (this.intake == null) {
            this.intake = 60;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
