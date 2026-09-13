package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "college_subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id")
    private Degree degree;

    private String program; // e.g. "B.Tech (CSE)", "BCA", "MBA"

    private String semester; // e.g. "Semester 1", "Semester 3"

    private Integer credits; // e.g. 4, 3, 2

    private String type; // Theory, Practical, Elective, Core

    private Integer hrsPerWeek; // e.g. 4, 2, 6

    private String faculty; // Primary faculty in-charge

    @Column(length = 1000)
    private String faculties; // Comma-separated faculty members in-charge

    private String academicYear; // e.g. "2025-2026"

    @Column(length = 1000)
    private String description;

    private String status; // Active / Inactive / Draft

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

    // Alias methods
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

    public String getDepartmentName() {
        return this.department != null ? this.department.getName() : null;
    }

    public String getDegreeCode() {
        return this.degree != null ? this.degree.getCode() : null;
    }

    public String getDegreeName() {
        return this.degree != null ? this.degree.getName() : null;
    }

    public Integer getDepartmentId() {
        return this.department != null ? this.department.getId() : null;
    }

    public Integer getDegreeId() {
        return this.degree != null ? this.degree.getId() : null;
    }
}
