package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProxyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ProxyTemplate template;

    @Column(nullable = false)
    private Long timetableId; // target timetable plan

    @Column(nullable = false)
    private Long slotId; // the selected time block slot

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Column(nullable = false)
    private Integer dayOfWeek; // 1-7 or name representation

    @Column(nullable = false)
    private LocalDate proxyDate; // proxy active date

    @Column(nullable = false)
    private Long originalTeacherId;

    @Column(nullable = false)
    private String originalTeacherName;

    // subject context
    private Long subjectId;
    private String subjectName;

    // class/section context
    private Long classId;
    private Long sectionId;

    @Column(length = 1000)
    private String remarks; // assignment remarks

    @Builder.Default
    private String status = "ACTIVE"; // e.g., ACTIVE, INACTIVE

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
