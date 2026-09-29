package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Common request to save a notification event AND send FCM push.
 *
 * Used by ANY service (Accounts, Academic, Admin, etc.) to:
 *   1. Persist the event in notification_events table (record keeping)
 *   2. Fire the FCM push to the target user(s)
 *
 * Target resolution (choose ONE):
 *   - targetUserIds  → send to specific users
 *   - targetGroup    → "ALL" | "STUDENTS" | "STAFF"
 */
@Data
public class NotificationEventRequest {

    // ── Event metadata (for record keeping) ──────────────────────────────────

    /**
     * Event category. Examples:
     * FEE_PAYMENT | FEE_REMINDER | EXAM_RESULT | ATTENDANCE |
     * TIMETABLE_CHANGE | LEAVE_STATUS | HOLIDAY | GENERAL
     */
    @NotBlank(message = "eventType is required")
    private String eventType;

    /**
     * Optional sub-type. Examples:
     * FEE_COLLECTED | FEE_OVERDUE | RESULT_PUBLISHED | LEAVE_APPROVED
     */
    private String eventSubType;

    /**
     * Reference ID from the originating service.
     * e.g. fee_payment_id=101, exam_result_id=55, leave_application_id=7
     */
    private String referenceId;

    /**
     * Module / service triggering this notification.
     * e.g. "ACCOUNTS" | "ACADEMIC" | "ADMIN" | "TRANSPORT"
     */
    private String sourceModule;

    // ── Notification content ──────────────────────────────────────────────────

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "body is required")
    private String body;

    /** Optional image URL to show in the notification. */
    private String imageUrl;

    // ── Targeting (choose one) ────────────────────────────────────────────────

    /**
     * List of user IDs (from users table) to notify.
     * Their active FCM tokens are resolved automatically.
     */
    private List<Integer> targetUserIds;

    /**
     * Broad group target.
     * Accepted: ALL | STUDENTS | STAFF
     */
    private String targetGroup;

    // ── Optional FCM data payload ─────────────────────────────────────────────

    /**
     * Extra key-value data forwarded to the mobile app.
     * e.g. { "screen": "FEE_RECEIPT", "receiptId": "101" }
     */
    private Map<String, String> data;
}
