
package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_punch_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPunchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    private LocalDateTime punchInTime;

    private LocalDateTime punchOutTime;

    @Column(nullable = false)
    private String workDate;
}
