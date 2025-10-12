package com.academic.entity;

import com.academic.utility.LongListToJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "academic_calendar_events")
@Data
public class AcademicCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String type;

    @Column(name = "classes_involved", columnDefinition = "json")
    @Convert(converter = LongListToJsonConverter.class)
    private List<Long> classesInvolved;

    private String duration;

    private String status;

    private boolean isDeleted = false;
}
