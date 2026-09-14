package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "college_calendar_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String eventType; // Academic, Examination, Submission, Holiday, Activity

    @Column(name = "academic_year")
    private String academicYear; // e.g. "2025-2026"

    private String semester; // e.g. "All Semesters", "Semester 1", "Semester 3"

    @Column(nullable = false, name = "start_date")
    private String startDate; // YYYY-MM-DD

    @Column(name = "end_date")
    private String endDate; // YYYY-MM-DD (optional)

    private String status; // Scheduled, Active, Completed

    @Column(name = "target_program")
    private String targetProgram; // e.g. "All Programs", "B.Tech (CSE)", "BCA"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "Scheduled";
        }
        if (this.eventType == null || this.eventType.isBlank()) {
            this.eventType = "Academic";
        }
        if (this.semester == null || this.semester.isBlank()) {
            this.semester = "All Semesters";
        }
        if (this.targetProgram == null || this.targetProgram.isBlank()) {
            this.targetProgram = "All Programs";
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
