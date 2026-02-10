package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "marksheet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marksheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Integer classId;
    private Integer sectionId;
    private Integer sessionId;
    private Integer examTypeId;
    private LocalDate examDate;

    /* SUMMARY */
    private Integer totalMarksObtained;
    private Integer totalMaxMarks;
    private Double percentage;
    private String grade;
    private Double gpa;

    /* EVALUATION */
    private Integer attendanceDays;
    private Integer totalWorkingDays;
    private String conductGrade;
    private String sportsGrade;
    private String extraCurricularGrade;

    @Column(columnDefinition = "TEXT")
    private String teacherRemarks;

    @Column(columnDefinition = "TEXT")
    private String principalRemarks;

    private Boolean published = false;
    private Boolean isDeleted = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
