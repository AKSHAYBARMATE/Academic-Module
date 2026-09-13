package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    private String department;

    private String program; // e.g. "B.Tech (CSE)", "BCA", "MBA"

    private String semester; // e.g. "Semester 1", "Semester 3"

    private Integer credits; // e.g. 4, 3, 2

    private String type; // Theory, Practical, Elective

    private Integer hrsPerWeek; // e.g. 4, 2, 6

    private String faculty; // Primary faculty in-charge

    @Column(length = 1000)
    private String faculties; // Comma-separated faculty members in-charge

    private String academicYear; // e.g. "2025-2026"

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
        if (this.hrsPerWeek == null) {
            this.hrsPerWeek = 4;
        }
        if (this.credits == null) {
            this.credits = 4;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Alias methods for clean dual-compatibility
    public String getCode() {
        return this.subjectCode;
    }

    public void setCode(String code) {
        this.subjectCode = code;
    }

    public String getName() {
        return this.subjectName;
    }

    public void setName(String name) {
        this.subjectName = name;
    }
}
