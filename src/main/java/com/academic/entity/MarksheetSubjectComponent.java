package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marksheet_subject_component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetSubjectComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "subject_marks_id")
    private MarksheetSubject subject;

    @ManyToOne
    @JoinColumn(name = "component_id")
    private ExamComponentMaster component;

    private Integer marksObtained;

    private Integer maxMarks;
}