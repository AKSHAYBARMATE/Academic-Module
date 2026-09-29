package com.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Persists every notification event dispatched from the system.
 * Acts as an audit log / notification history for students and staff.
 *
 * One row = one notification attempt against one user.
 * If a notification is broadcast to 50 users, 50 rows are created.
 */
@Entity
@Table(name = "notification_events", indexes = {
    @Index(name = "idx_ne_target_user",  columnList = "target_user_id"),
    @Index(name = "idx_ne_event_type",   columnList = "event_type"),
    @Index(name = "idx_ne_status",       columnList = "status"),
    @Index(name = "idx_ne_created_at",   columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── What type of event ────────────────────────────────────────────────────

    /**
     * Broad event category.
     * e.g. FEE_PAYMENT | EXAM_RESULT | ATTENDANCE | TIMETABLE | GENERAL | LEAVE | etc.
     * Kept as String for extensibility – new types can be added without code change.
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /**
     * Optional sub-type / action within the event.
     * e.g. FEE_COLLECTED, FEE_REMINDER, RESULT_PUBLISHED, LEAVE_APPROVED
     */
    @Column(name = "event_sub_type", length = 80)
    private String eventSubType;

    // ── Notification content ──────────────────────────────────────────────────

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    // ── Who is targeted ───────────────────────────────────────────────────────

    /**
     * The user_id (from users table) of the recipient.
     * Null if this is a broadcast row (stored separately per-user in that case).
     */
    @Column(name = "target_user_id")
    private Integer targetUserId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "staff_id")
    private Integer staffId;

    /** TRUE → staff, FALSE → student */
    @Column(name = "is_staff")
    private Boolean isStaff;

    /**
     * For broadcasts: label stored here instead of per-row duplication.
     * e.g. "ALL" | "STUDENTS" | "STAFF"
     */
    @Column(name = "target_group", length = 20)
    private String targetGroup;

    // ── FCM dispatch result ───────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /** FCM message ID returned on success. */
    @Column(name = "fcm_message_id", length = 512)
    private String fcmMessageId;

    /** Error message if FCM rejected the token. */
    @Column(name = "fcm_error", length = 512)
    private String fcmError;

    /** When the FCM call actually completed. */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // ── Extra contextual data (source reference IDs, etc.) ───────────────────

    /**
     * Reference ID in the originating service.
     * e.g. fee_payment_id, exam_result_id, leave_application_id
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /**
     * Name of the module / service that triggered this event.
     * e.g. "ACCOUNTS", "ACADEMIC", "ADMIN"
     */
    @Column(name = "source_module", length = 50)
    private String sourceModule;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Status enum ──────────────────────────────────────────────────────────

    public enum NotificationStatus {
        PENDING,   // saved but not yet sent
        SENT,      // FCM accepted
        FAILED,    // FCM rejected
        SKIPPED    // no active tokens found
    }
}
