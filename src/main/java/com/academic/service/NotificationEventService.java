package com.academic.service;

import com.academic.request.NotificationEventRequest;
import com.academic.response.NotificationEventResponse;
import com.academic.response.StandardResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationEventService {

    /**
     * Common execute API:
     * 1. Save notification event record(s) in DB (one row per target user)
     * 2. Send FCM push to all resolved tokens
     * 3. Update each record with FCM result (SENT / FAILED / SKIPPED)
     *
     * Called by any service: Accounts, Academic, Admin, etc.
     */
    StandardResponse<NotificationEventResponse> saveAndSend(NotificationEventRequest request);

    /**
     * Fetch notification history for a user (for mobile notification inbox).
     */
    StandardResponse<?> getHistoryByUser(Integer userId, int page, int size);

    /**
     * Fetch all events of a specific type (admin dashboard).
     */
    StandardResponse<?> getHistoryByEventType(String eventType, int page, int size);
}
