package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "college_timetables")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "degree_id")
    private Degree degree;

    private String degreeCode; // e.g. "B.Tech", "BCA", "M.Tech"

    @Column(nullable = false)
    private String program; // e.g. "B.Tech (CSE)", "Data Science"

    @Column(nullable = false)
    private String semester; // e.g. "Semester 1", "Semester 3"

    @Column(nullable = false)
    private String section; // e.g. "CSE-A", "Section B"

    private String academicYear; // e.g. "2025-2026"

    @Column(nullable = false)
    private String day; // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday

    @Column(nullable = false)
    private String timeSlot; // e.g. "09:00 - 10:00 AM"

    private String periodLabel; // e.g. "Period 1", "Morning Refreshment"

    private String startTime; // "09:00"

    private String endTime; // "10:00"

    private Integer durationMinutes; // e.g. 60, 15, 120

    private Boolean isBreak; // true for Recess / Lunch / Refreshment

    // Subject Foreign Key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private CollegeSubject subject;

    private String subjectCode; // e.g. "CS301"

    private String subjectName; // e.g. "DBMS"

    // Staff / Teacher Foreign Key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private String teacherName; // e.g. "Prof. Priya Gupta"

    private String staffCode; // e.g. "STF001"

    @Column(nullable = false)
    private String room; // e.g. "Room 101", "Computer Lab 2"

    private String type; // Theory, Practical, Mentoring, Library, Activity

    private Integer spanPeriods; // 1 for standard 1 hr, 2 for 2 hr lab block

    private String status; // Active, Draft, Cancelled

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "Active";
        }
        if (this.isBreak == null) {
            this.isBreak = false;
        }
        if (this.spanPeriods == null || this.spanPeriods <= 0) {
            this.spanPeriods = 1;
        }
        if (this.type == null || this.type.isBlank()) {
            this.type = "Theory";
        }
        if (this.durationMinutes == null || this.durationMinutes <= 0) {
            this.durationMinutes = 60;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
