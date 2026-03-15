package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "marksheet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Marksheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer studentId;

    private Integer classId;
    private Integer sectionId;
    private Integer sessionId;

    private Integer examTypeId;


    private LocalDate examDate;

    private Integer totalMarksObtained;
    private Integer totalMaxMarks;

    /* EVALUATION */
    private Integer attendanceDays;
    private Integer totalWorkingDays;
    private String conductGrade;
    private String sportsGrade;
    private String extraCurricularGrade;

    private Double percentage;

    private String grade;

    private Boolean published = false;

    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "marksheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MarksheetSubject> subjects;
}