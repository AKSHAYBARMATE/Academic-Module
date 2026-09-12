package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "degrees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String name;

    private String degreeType; // UG, PG, Diploma, Doctorate, Integrated

    private String duration; // e.g. "4 Years", "3 Years"

    private Integer totalSemesters; // e.g. 8, 6, 4

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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
