package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "college_marksheets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeMarksheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String universityPrn;

    private String universityRollNo;

    @Column(nullable = false)
    private String collegeRollNo;

    private String fatherName;
    private String motherName;

    private String degreeCode;
    private String programCode;
    private String programName;
    private String departmentName;

    private String academicYear; // e.g. "2024-2025"
    private String semester;     // e.g. "Semester 5"
    private String examSession;  // e.g. "Winter 2024"
    private String examType;     // "REGULAR", "SUPPLEMENTARY", "REVALUATION"

    private Double totalCreditsOffered;
    private Double totalCreditsEarned;

    private Double sgpa;
    private Double cgpa;

    private Double totalMarksObtained;
    private Double totalMaxMarks;
    private Double percentage;

    private String resultStatus; // "FIRST_CLASS_DISTINCTION", "FIRST_CLASS", "SECOND_CLASS", "ATKT", "FAIL"
    private Integer backlogCount;

    private String marksheetNumber;
    private LocalDate issueDate;
    private String verificationCode;

    @Builder.Default
    private Boolean published = true;

    @Builder.Default
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "collegeMarksheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CollegeMarksheetSubject> subjects = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (published == null) published = true;
        if (isDeleted == null) isDeleted = false;
        if (backlogCount == null) backlogCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
