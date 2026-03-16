package com.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_coscholastic_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCoScholasticConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Session session;

    @ManyToOne
    private CommonMaster examType;

    @ManyToOne
    private CommonMaster classId;

    @ManyToOne
    private CoScholasticActivityMaster activity;

    private Integer maxMarks;

    private Boolean isDelete = false;
}