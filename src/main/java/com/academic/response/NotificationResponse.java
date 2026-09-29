package com.academic.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Summary returned after a send-notification operation.
 */
@Data
@Builder
public class NotificationResponse {

    /** Total tokens targeted. */
    private int totalTargeted;

    /** Number of tokens FCM accepted. */
    private int successCount;

    /** Number of tokens FCM rejected. */
    private int failureCount;

    /** FCM tokens that were invalid / caused failures. */
    private List<String> failedTokens;

    /** Human-readable status message. */
    private String message;
}
