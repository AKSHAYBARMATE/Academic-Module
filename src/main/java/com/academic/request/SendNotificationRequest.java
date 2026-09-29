package com.academic.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request body for sending a Firebase push notification.
 *
 * Target resolution priority:
 *   1. tokens        – send directly to these FCM tokens
 *   2. userIds       – send to all active tokens of these users
 *   3. targetGroup   – "STUDENTS" | "STAFF" | "ALL"
 *
 * At least one target field must be provided.
 */
@Data
public class SendNotificationRequest {

    // ── Notification content ──────────────────────────────────────────────────

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Body must not be blank")
    private String body;

    /** Optional image URL to show in the notification. */
    private String imageUrl;

    // ── Targeting (choose one) ────────────────────────────────────────────────

    /** Direct FCM device tokens. */
    private List<String> tokens;

    /** Send to all active tokens of these user IDs. */
    private List<Integer> userIds;

    /**
     * Broad target group.
     * Accepted values: STUDENTS | STAFF | ALL
     */
    private String targetGroup;

    // ── Optional extra data payload ───────────────────────────────────────────

    /** Arbitrary key-value pairs forwarded as FCM data payload. */
    private java.util.Map<String, String> data;
}
