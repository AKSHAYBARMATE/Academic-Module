package com.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college_marksheet_subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeMarksheetSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marksheet_id", nullable = false)
    @JsonIgnore
    private CollegeMarksheet collegeMarksheet;

    @Column(nullable = false)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    private String subjectType; // "Theory", "Practical", "Tutorial", "Elective", "Project", "Audit"

    private Double credits;

    private Integer internalMaxMarks;
    private Integer internalObtainedMarks;

    private Integer externalMaxMarks;
    private Integer externalObtainedMarks;

    private Integer practicalMaxMarks;
    private Integer practicalObtainedMarks;

    private Integer totalMarks;
    private Integer maxTotalMarks;

    private Double gradePoint; // 0.0 to 10.0
    private String letterGrade; // "O", "A+", "A", "B+", "B", "C", "P", "F", "Ab"

    private String status; // "Pass", "Fail", "Absent", "Exempted"

    @Builder.Default
    private Boolean isBacklog = false;

    @Builder.Default
    private Integer attemptNumber = 1;
}
