package com.academic.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    private String department;

    private Integer credits;

    private String type;   // Core / Elective

    private String status; // Active / Draft / Inactive

    private Boolean isDeleted = false;
}
