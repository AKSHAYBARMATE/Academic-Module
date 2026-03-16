package com.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "marksheet_subject_term")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetSubjectTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long marksheetSubjectId;

    /* TERM */
    private Integer termNumber; // 1 or 2

    /* COMPONENT MARKS */

    private Double ptMarks;
    private Double ptMax;

    private Double nbMarks;
    private Double nbMax;

    private Double seMarks;
    private Double seMax;

    private Double termExamMarks;
    private Double termExamMax;

    /* TERM TOTAL */
    private Double termTotal;
    private Double termMax;

    private String grade;
}