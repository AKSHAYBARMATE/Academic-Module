package com.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_component_master")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamComponentMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String componentName;
    // PT, NB, SE, TERM, ASSIGNMENT, VIVA etc

    private Integer maxMarks;

    private Integer displayOrder;

    private Boolean active = true;
}
