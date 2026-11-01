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
 * Entity representing a school holiday.
 * Extends BaseEntity to maintain WHO columns (createdBy, createdOn, modifiedBy, modifiedAt, isDeleted).
 */
@Entity
@Table(name = "holidays")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "holiday_type", nullable = false)
    private String holidayType; // e.g. "Public Holiday", "National Holiday", "Festival", "Institutional Holiday"

    @Column(name = "applicable_to", nullable = false)
    private String applicableTo; // "ALL_SCHOOL" or "SELECTED_CLASSES"

    @Column(name = "selected_class_ids", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    @Builder.Default
    private List<Long> selectedClassIds = new ArrayList<>();

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "session_id")
    private Long sessionId;

    @PrePersist
    protected void onHolidayCreate() {
        if (getCreatedOn() == null) {
            setCreatedOn(IstClock.nowDateTime());
        }
        if (getModifiedAt() == null) {
            setModifiedAt(IstClock.nowDateTime());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        if (selectedClassIds == null) {
            selectedClassIds = new ArrayList<>();
        }
    }

    @PreUpdate
    protected void onHolidayUpdate() {
        setModifiedAt(IstClock.nowDateTime());
        if (selectedClassIds == null) {
            selectedClassIds = new ArrayList<>();
        }
    }
}

