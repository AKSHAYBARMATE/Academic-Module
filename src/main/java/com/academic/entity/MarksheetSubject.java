package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "marksheet_subject")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "marksheet_id")
    private Marksheet marksheet;

    private Integer subjectId;

    private Integer totalMarks;

    private Integer totalMax;

    private String grade;

    private String subjectRemarks;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MarksheetSubjectComponent> components;
}