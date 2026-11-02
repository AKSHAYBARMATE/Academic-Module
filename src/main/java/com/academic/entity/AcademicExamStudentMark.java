package com.academic.entity;

import com.academic.utility.IstClock;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for Marks Entry & Result Processing per Student (Screen 4).
 * Captures student theory, practical, internal, total, grade, result, attendance status (AB, EX, NA), remarks.
 * Extends BaseEntity for WHO audit columns.
 */
@Entity
@Table(
    name = "academic_exam_student_marks",
    indexes = {
        @Index(name = "idx_esm_exam_class_sec_sub", columnList = "exam_id, class_id, section_id, subject_id"),
        @Index(name = "idx_esm_student", columnList = "student_id")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicExamStudentMark extends BaseEntity {

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

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "roll_no")
    private String rollNo; // Admission No or Roll No e.g. "1", "2"

    @Column(name = "student_name")
    private String studentName; // e.g. "AARAV", "AASTHA YADAV"

    @Column(name = "theory_marks")
    private Double theoryMarks;

    @Column(name = "practical_marks")
    private Double practicalMarks;

    @Column(name = "internal_marks")
    private Double internalMarks;

    @Column(name = "component_marks", columnDefinition = "json")
    @Convert(converter = com.academic.utility.MapStringDoubleToJsonConverter.class)
    @Builder.Default
    private java.util.Map<String, Double> componentMarks = new java.util.HashMap<>();

    @Column(name = "total_marks")
    private Double totalMarks;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "attendance_status")
    @Builder.Default
    private String attendanceStatus = "PRESENT"; // "PRESENT", "AB" (Absent), "EX" (Exempt), "NA" (Not Applicable)

    @Column(name = "grade")
    private String grade; // "A+", "A", "B", "C", "D", "F", "-"

    @Column(name = "result")
    private String result; // "Pass", "Fail", "Absent", "Exempt"

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "entry_status", nullable = false)
    @Builder.Default
    private String entryStatus = "DRAFT"; // "DRAFT", "VALIDATED", "SUBMITTED", "LOCKED"

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
        if (entryStatus == null) {
            entryStatus = "DRAFT";
        }
        if (attendanceStatus == null) {
            attendanceStatus = "PRESENT";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        setModifiedAt(IstClock.nowDateTime());
    }
}

