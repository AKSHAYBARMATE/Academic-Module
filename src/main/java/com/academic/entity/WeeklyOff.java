package com.academic.entity;

import com.academic.utility.IstClock;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

/**
 * Entity representing Weekly Off Days configuration.
 * Extends BaseEntity to maintain WHO columns (createdBy, createdOn, modifiedBy, modifiedAt, isDeleted).
 */
@Entity
@Table(name = "weekly_off_configs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyOff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, unique = true)
    private DayOfWeek dayOfWeek; // SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY

    @Column(name = "day_name", nullable = false)
    private String dayName; // "Sunday", "Monday", etc.

    @PrePersist
    protected void onWeeklyOffCreate() {
        if (getCreatedOn() == null) {
            setCreatedOn(IstClock.nowDateTime());
        }
        if (getModifiedAt() == null) {
            setModifiedAt(IstClock.nowDateTime());
        }
        if (getIsDeleted() == null) {
            setIsDeleted(false);
        }
        if (dayOfWeek != null && (dayName == null || dayName.isBlank())) {
            String name = dayOfWeek.name();
            dayName = name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    @PreUpdate
    protected void onWeeklyOffUpdate() {
        setModifiedAt(IstClock.nowDateTime());
        if (dayOfWeek != null && (dayName == null || dayName.isBlank())) {
            String name = dayOfWeek.name();
            dayName = name.charAt(0) + name.substring(1).toLowerCase();
        }
    }
}
