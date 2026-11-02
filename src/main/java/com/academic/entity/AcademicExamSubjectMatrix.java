package com.academic.entity;

import com.academic.utility.IstClock;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Subject & Marks Matrix (Screen 3 - Step 2).
 * Defines Theory, Practical, Internal, Total Max Marks, Passing Marks, and Weightage per subject for an exam.
 * Extends BaseEntity for WHO audit columns.
 */
@Entity
@Table(name = "academic_exam_subject_matrices")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicExamSubjectMatrix extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "class_id")
    private Long classId; // Optional class-specific override; null means applies to all classes in the exam

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "subject_name")
    private String subjectName; // e.g., "English", "Mathematics", "Science"

    @Column(name = "theory_max_marks")
    private Double theoryMaxMarks;

    @Column(name = "practical_max_marks")
    private Double practicalMaxMarks;

    @Column(name = "internal_max_marks")
    private Double internalMaxMarks;

    @Column(name = "total_max_marks", nullable = false)
    @Builder.Default
    private Double totalMaxMarks = 100.0;

    @Column(name = "passing_marks")
    @Builder.Default
    private Double passingMarks = 33.0;

    @Column(name = "weightage_percentage")
    @Builder.Default
    private Double weightagePercentage = 100.0;

    @Column(name = "component_max_marks", columnDefinition = "json")
    @Convert(converter = com.academic.utility.MapStringDoubleToJsonConverter.class)
    @Builder.Default
    private java.util.Map<String, Double> componentMaxMarks = new java.util.LinkedHashMap<>();

    @PrePersist
    protected void onCreate() {
        if (getCreatedOn() == null) {
            setCreatedOn(IstClock.nowDateTime());
        }
        if (getModifiedAt() == null) {
            setModifiedAt(IstClock.nowDateTime());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        autoCalculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        setModifiedAt(IstClock.nowDateTime());
        autoCalculateTotal();
    }

    private void autoCalculateTotal() {
        if (componentMaxMarks != null && !componentMaxMarks.isEmpty()) {
            double sum = 0.0;
            for (Double v : componentMaxMarks.values()) {
                if (v != null) sum += v;
            }
            if (totalMaxMarks == null || totalMaxMarks <= 0.0) {
                totalMaxMarks = sum;
            }
            if (componentMaxMarks.containsKey("Theory")) theoryMaxMarks = componentMaxMarks.get("Theory");
            if (componentMaxMarks.containsKey("Practical")) practicalMaxMarks = componentMaxMarks.get("Practical");
            if (componentMaxMarks.containsKey("Internal")) internalMaxMarks = componentMaxMarks.get("Internal");
        } else {
            double t = theoryMaxMarks != null ? theoryMaxMarks : 0.0;
            double p = practicalMaxMarks != null ? practicalMaxMarks : 0.0;
            double i = internalMaxMarks != null ? internalMaxMarks : 0.0;
            if (totalMaxMarks == null || totalMaxMarks <= 0.0) {
                totalMaxMarks = t + p + i;
            }
            if (componentMaxMarks == null) componentMaxMarks = new java.util.HashMap<>();
            componentMaxMarks.put("Theory", t);
            componentMaxMarks.put("Practical", p);
            componentMaxMarks.put("Internal", i);
        }
    }
}

