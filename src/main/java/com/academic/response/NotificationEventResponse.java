package com.academic.response;

import com.academic.entity.NotificationEvent.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Returned after a notification event is saved and FCM is dispatched.
 */
@Data
@Builder
public class NotificationEventResponse {

    /** Total FCM tokens targeted. */
    private int totalTargeted;

    /** Tokens FCM accepted. */
    private int successCount;

    /** Tokens FCM rejected. */
    private int failureCount;

    /** IDs of saved NotificationEvent rows in DB. */
    private List<Long> savedEventIds;

    /** Overall dispatch status. */
    private NotificationStatus status;

    private String message;

    private LocalDateTime dispatchedAt;
}
