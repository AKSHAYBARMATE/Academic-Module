package com.academic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_slot_subject_mapper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotSubjectMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_id", nullable = false)
    @JsonIgnore
    private TimeTable timeTable;

    @Column(nullable = false)
    private String startTime; // "09:00"

    @Column(nullable = false)
    private String endTime;   // "10:00"

    @Column(nullable = false)
    private Long subjectId; // from common_master (commonMasterKey="SUBJECT")

    @Column(nullable = false)
    private String teacherName;

    @Column
    private String room;

    @Column
    private Integer day;

    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

