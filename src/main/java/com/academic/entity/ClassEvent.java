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
 * Entity representing a Class/School Event (e.g., Parent-Teacher Meeting, Annual Function, Sports Day).
 * Extends BaseEntity to maintain WHO columns (createdBy, createdOn, modifiedBy, modifiedAt, isDeleted).
 */
@Entity
@Table(name = "class_events")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType; // e.g. "Parent-Teacher Meeting", "Annual Function", "Sports Day"

    @Column(name = "event_title", nullable = false)
    private String eventTitle; // e.g. "Parent Meeting"

    @Column(name = "event_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "applicable_to", nullable = false)
    private String applicableTo; // "ENTIRE_SCHOOL", "SELECTED_CLASSES", "SELECTED_SECTIONS"

    @Column(name = "class_ids", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    @Builder.Default
    private List<Long> classIds = new ArrayList<>();

    @Column(name = "section_ids", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    @Builder.Default
    private List<Long> sectionIds = new ArrayList<>();

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "academic_session")
    private String academicSession; // e.g. "2026-27"

    @Column(name = "session_id")
    private Long sessionId;

    @PrePersist
    protected void onEventCreate() {
        if (getCreatedOn() == null) {
            setCreatedOn(IstClock.nowDateTime());
        }
        if (getModifiedAt() == null) {
            setModifiedAt(IstClock.nowDateTime());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        if (endDate == null) {
            endDate = eventDate;
        }
        if (classIds == null) {
            classIds = new ArrayList<>();
        }
        if (sectionIds == null) {
            sectionIds = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onEventUpdate() {
        setModifiedAt(IstClock.nowDateTime());
        if (endDate == null) {
            endDate = eventDate;
        }
        if (classIds == null) {
            classIds = new ArrayList<>();
        }
        if (sectionIds == null) {
            sectionIds = new ArrayList<>();
        }
    }
}

