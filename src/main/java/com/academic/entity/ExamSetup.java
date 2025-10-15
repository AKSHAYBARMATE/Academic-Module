package com.academic.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "exam_setup")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String examName;

    // Foreign keys referencing CommonMaster IDs
    @Column(nullable = false)
    private Integer classId;

    @Column(nullable = false)
    private Integer subjectId;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private Integer maxMarks;

    @Column(name = "academicYearId")
    private Integer academicYearId;

    // Soft delete flag
    @Builder.Default
    private Boolean isDeleted = false;
}
