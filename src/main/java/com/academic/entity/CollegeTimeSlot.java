package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "college_time_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String slotLabel; // e.g. "Period 1", "Morning Refreshment", "Recess", "Lunch Break"

    @Column(nullable = false)
    private String timeSlot; // e.g. "09:00 - 10:00 AM"

    private String startTime; // "09:00"

    private String endTime; // "10:00"

    private Integer durationMinutes; // e.g. 60, 15, 45, 120

    private Boolean isBreak; // true for Recess / Lunch / Refreshment

    private Integer slotOrder; // order in timetable matrix: 1, 2, 3...

    private String slotType; // "Standard Lecture", "Extended Block", "Recess", etc.

    private String status; // "Active", "Inactive"

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
        if (this.durationMinutes == null || this.durationMinutes <= 0) {
            this.durationMinutes = Boolean.TRUE.equals(this.isBreak) ? 15 : 60;
        }
        if (this.slotType == null || this.slotType.isBlank()) {
            this.slotType = Boolean.TRUE.equals(this.isBreak) ? "Recess" : (this.durationMinutes > 60 ? "Extended Block" : "Standard Lecture");
        }
        if (this.slotOrder == null) {
            this.slotOrder = 0;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
