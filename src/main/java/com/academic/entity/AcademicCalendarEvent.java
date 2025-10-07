package com.academic.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "academic_calendar_events")
@Data // Lombok annotation for getters, setters, toString, etc.
public class AcademicCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDate date; // Use LocalDate for the event date

    @Column(nullable = false)
    private String type; // e.g., Examination, Event, Meeting, Holiday

    private String classesInvolved; // e.g., All Classes, Class 6-12

    private String duration; // e.g., 2 weeks, 1 day, 2 months

    private String status;

    private boolean isDeleted = false; // Soft delete flag, matching the reference style
}
