package com.academic.entity;

import com.academic.utility.IstClock;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity tracking submission, approval, lock and unlock status for a marks sheet (Screen 4).
 * Enforces lock rules, audit history, and reopening with permission.
 * Extends BaseEntity for WHO audit columns.
 */
@Entity
@Table(
    name = "academic_exam_marks_submissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_marks_sub_exam_class_sec_sub",
            columnNames = {"exam_id", "class_id", "section_id", "subject_id"}
        )
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicExamMarksSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DRAFT"; // "DRAFT", "VALIDATED", "SUBMITTED", "LOCKED"

    @Column(name = "submitted_by_user")
    private Long submittedByUser;

    @Column(name = "submitted_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime submittedAt;

    @Column(name = "locked_by_user")
    private Long lockedByUser;

    @Column(name = "locked_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime lockedAt;

    @Column(name = "unlocked_by_user")
    private Long unlockedByUser;

    @Column(name = "unlocked_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime unlockedAt;

    @Column(name = "unlock_reason", length = 500)
    private String unlockReason;

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
            status = "DRAFT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        setModifiedAt(IstClock.nowDateTime());
    }
}

