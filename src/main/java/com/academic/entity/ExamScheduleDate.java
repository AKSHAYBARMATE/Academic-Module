package com.academic.entity;

import com.academic.utility.IstClock;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_schedule_date")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScheduleDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Reference to Exam Schedule Header (Optional / Parent Group) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_schedule_id")
    private ExamSchedule examSchedule;

    /* Academic Session */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    /* Class (Reference to CommonMaster or Class ID) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private CommonMaster classMaster;


    /* Subject */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /* Exam Date (kis din exam hai) */
    @Column(nullable = false)
    private LocalDate examDate;

    /* Start Time & End Time */
    @Column(nullable = false, length = 20)
    private String startTime; // e.g. "09:00 AM" or "09:00"

    @Column(nullable = false, length = 20)
    private String endTime; // e.g. "12:00 PM" or "12:00"

    /* Invigilator Staff Mapping & Name */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invigilator_id")
    private Staff invigilator;

    /* Room Number */
    @Column(length = 50)
    private String roomNo;

    /* Marks Details (Optional) */
    private Integer maxMarks;
    private Integer passingMarks;

    /* Status: SCHEDULED / ONGOING / COMPLETED / CANCELLED */
    @Builder.Default
    private String status = "SCHEDULED";

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDeleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = IstClock.nowDateTime();
        this.updatedAt = IstClock.nowDateTime();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = IstClock.nowDateTime();
    }
}
