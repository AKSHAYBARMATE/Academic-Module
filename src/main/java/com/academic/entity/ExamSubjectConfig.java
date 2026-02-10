package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_subject_config", uniqueConstraints = @UniqueConstraint(columnNames = { "session_id", "exam_type_id", "class_id",
        "subject_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubjectConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Academic Year */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    /* Exam Type */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", nullable = false)
    private CommonMaster examType;

    /* Exam Type */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private CommonMaster classId;

    /* Subject */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private Integer theoryMarks;
    private Integer practicalMarks;
    private Integer internalMarks;
    private Integer totalMarks;

    @Builder.Default
    private Boolean isDelete = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
