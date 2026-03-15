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

    @ManyToOne
    @JoinColumn(name = "marksheet_id")
    private Marksheet marksheet;

    private Long activityId;

    private String grade;
}