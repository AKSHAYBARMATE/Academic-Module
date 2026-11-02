package com.academic.entity;

import com.academic.utility.IstClock;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity for Exam Schedule Slots (Screen 3 - Step 3).
 * Captures Date, Class, Section, Subject, Start Time, End Time, Duration, Room, Invigilator, Max Marks.
 * Extends BaseEntity for WHO audit columns.
 */
@Entity
@Table(name = "academic_exam_schedule_slots")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicExamScheduleSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "exam_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "section_id")
    private Long sectionId; // optional; null means applies to all sections of this class

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "start_time", nullable = false)
    private String startTime; // e.g., "09:00"

    @Column(name = "end_time", nullable = false)
    private String endTime; // e.g., "11:00"

    @Column(name = "duration")
    private String duration; // e.g., "2 Hrs"

    @Column(name = "room")
    private String room; // e.g., "R1", "R2"

    @Column(name = "invigilator_id")
    private Long invigilatorId; // Staff ID

    @Column(name = "invigilator_name")
    private String invigilatorName; // e.g., "Mr. Sharma", "Ms. Verma"

    @Column(name = "max_marks")
    @Builder.Default
    private Double maxMarks = 100.0;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "SCHEDULED"; // "SCHEDULED", "COMPLETED", "CANCELLED"

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
            status = "SCHEDULED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        setModifiedAt(IstClock.nowDateTime());
    }
}

