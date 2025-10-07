package com.academic.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teacherName;

    @Column(nullable = false, unique = false)
    private String employeeId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String classes; // Store as comma-separated string. For normalization, use a join table.

    @Column(nullable = false)
    private Integer loadHours;

    private String status; // e.g., Active, Scheduled

    private Boolean isDeleted = false;
}
