package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Stores Firebase Cloud Messaging (FCM) device tokens per user.
 * A single user can have multiple tokens (multiple devices / reinstalls).
 */
@Entity
@Table(
    name = "user_device_tokens",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_device_token",
        columnNames = {"user_id", "device_token"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** Convenience – denormalized for quick lookups */
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "staff_id")
    private Integer staffId;

    @Column(name = "is_staff", nullable = false)
    private boolean isStaff;


    @Column(name = "device_token", nullable = false, length = 512)
    private String deviceToken;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
