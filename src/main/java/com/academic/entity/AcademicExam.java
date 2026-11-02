package com.academic.entity;

import com.academic.utility.IstClock;
import com.academic.utility.LongListToJsonConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Exam Setup (Screen 3 - Step 1).
 * Extends BaseEntity for WHO audit columns (createdBy, createdOn, modifiedBy, modifiedAt, isDeleted).
 * Does not touch existing legacy exam tables.
 */
@Entity
@Table(name = "academic_exams")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicExam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_name", nullable = false)
    private String examName; // e.g., "Unit Test 1"

    @Column(name = "exam_type", nullable = false)
    private String examType; // e.g., "Unit Test", "Half Yearly", "Annual", "Term 1"

    @Column(name = "academic_session", nullable = false)
    private String academicSession; // e.g., "2026-27"

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "class_ids", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    @Builder.Default
    private List<Long> classIds = new ArrayList<>(); // e.g., [3, 4, 5]

    @Column(name = "assessment_components", columnDefinition = "json")
    @Convert(converter = com.academic.utility.StringListToJsonConverter.class)
    @Builder.Default
    private List<String> assessmentComponents = new ArrayList<>(List.of("Theory", "Practical", "Internal"));

    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "Draft"; // "Draft", "Published", "Completed", "Cancelled"

    @Column(name = "description", length = 1000)
    private String description;

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
        if (status == null) {
            status = "Draft";
        }
        if (classIds == null) {
            classIds = new ArrayList<>();
        }
        if (assessmentComponents == null || assessmentComponents.isEmpty()) {
            assessmentComponents = new ArrayList<>(List.of("Theory", "Practical", "Internal"));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        setModifiedAt(IstClock.nowDateTime());
        if (classIds == null) {
            classIds = new ArrayList<>();
        }
        if (assessmentComponents == null || assessmentComponents.isEmpty()) {
            assessmentComponents = new ArrayList<>(List.of("Theory", "Practical", "Internal"));
        }
    }
}

