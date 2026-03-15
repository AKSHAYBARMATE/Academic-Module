package com.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_subject_config_component")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubjectConfigComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private ExamSubjectConfig config;

    @ManyToOne
    @JoinColumn(name = "component_id")
    private ExamComponentMaster component;

    private Integer maxMarks;
}