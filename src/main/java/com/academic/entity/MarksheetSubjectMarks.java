package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marksheet_subject_marks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetSubjectMarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long marksheetId;
    private Long subjectId;

    private Integer theoryMarks;
    private Integer theoryMax;

    private Integer practicalMarks;
    private Integer practicalMax;

    private Integer internalMarks;
    private Integer internalMax;

    private Integer totalMarks;
    private Integer totalMax;

    private String subjectRemarks;
}
