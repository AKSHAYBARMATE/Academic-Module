package com.academic.service;

import com.academic.request.DeviceTokenRequest;
import com.academic.request.FeePaymentNotificationRequest;
import com.academic.request.SendNotificationRequest;
import com.academic.response.NotificationResponse;
import com.academic.response.StandardResponse;

public interface FirebaseNotificationService {

    /**
     * Save (or refresh) a device FCM token for the currently logged-in user.
     * The user type (student / staff) is determined from LoginUser context.
     */
    StandardResponse<?> saveDeviceToken(Integer userId, DeviceTokenRequest request);

    /**
     * Remove / deactivate a device token (e.g., on logout).
     */
    StandardResponse<?> removeDeviceToken(Integer userId, String deviceToken);

    /**
     * Generic send notification.  Targets are resolved from the request body.
     */
    StandardResponse<NotificationResponse> sendNotification(SendNotificationRequest request);

    /**
     * Fee-payment notification execute API.
     * Called by the Accounts service after a payment is recorded & saved.
     * This method ONLY sends the FCM push – it does NOT save any payment data.
     */
    StandardResponse<NotificationResponse> sendFeePaymentNotification(FeePaymentNotificationRequest request);
}
