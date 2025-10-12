package com.academic.entity;


import com.academic.utility.LongListToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Column(name = "classes_involved", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    private List<Long> classesInvolved;

    @Column(nullable = false)
    private Integer loadHours;

    private String status; // e.g., Active, Scheduled

    private Boolean isDeleted = false;
}
