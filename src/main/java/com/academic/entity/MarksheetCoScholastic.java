package com.academic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "marksheet_coscholastic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetCoScholastic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Marksheet */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marksheet_id", nullable = false)
    private Marksheet marksheet;

    /* Activity */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private CoScholasticActivityMaster activity;

    /* Marks */

    private Integer marksObtained;

    private Integer maxMarks;

    /* Derived grade */

    private String grade;
}