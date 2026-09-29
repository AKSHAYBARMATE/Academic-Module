package com.academic.controller;

import com.academic.config.LoginUser;
import com.academic.request.DeviceTokenRequest;
import com.academic.request.FeePaymentNotificationRequest;
import com.academic.request.NotificationEventRequest;
import com.academic.request.SendNotificationRequest;
import com.academic.response.NotificationEventResponse;
import com.academic.response.NotificationResponse;
import com.academic.response.StandardResponse;
import com.academic.service.FirebaseNotificationService;
import com.academic.service.NotificationEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academic-module/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final FirebaseNotificationService firebaseNotificationService;
    private final NotificationEventService    notificationEventService;

    @Autowired
    private LoginUser loginUser;

    @PostMapping("/device-token")
    public ResponseEntity<StandardResponse<?>> saveDeviceToken(
            @Valid @RequestBody DeviceTokenRequest request) {

        Integer userId = getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("User ID not found in session", "USER_NOT_FOUND", null));
        }
        return ResponseEntity.ok(firebaseNotificationService.saveDeviceToken(userId, request));
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<StandardResponse<?>> removeDeviceToken(
            @RequestParam String deviceToken) {

        Integer userId = getUserId();
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(StandardResponse.error("User ID not found in session", "USER_NOT_FOUND", null));
        }
        return ResponseEntity.ok(firebaseNotificationService.removeDeviceToken(userId, deviceToken));
    }

    @PostMapping("/send")
    public ResponseEntity<StandardResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {

        return ResponseEntity.ok(firebaseNotificationService.sendNotification(request));
    }

    @PostMapping("/execute")
    public ResponseEntity<StandardResponse<NotificationEventResponse>> executeNotification(
            @Valid @RequestBody NotificationEventRequest request) {

        return ResponseEntity.ok(notificationEventService.saveAndSend(request));
    }

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<StandardResponse<?>> getHistoryByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationEventService.getHistoryByUser(userId, page, size));
    }

    @GetMapping("/history/type/{eventType}")
    public ResponseEntity<StandardResponse<?>> getHistoryByEventType(
            @PathVariable String eventType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(notificationEventService.getHistoryByEventType(eventType, page, size));
    }

    @PostMapping("/execute/fee-payment")
    public ResponseEntity<StandardResponse<NotificationResponse>> sendFeePaymentNotification(
            @Valid @RequestBody FeePaymentNotificationRequest request) {

        return ResponseEntity.ok(firebaseNotificationService.sendFeePaymentNotification(request));
    }
    private Integer getUserId() {
        Long uid = loginUser.getUserId();
        return uid != null ? uid.intValue() : null;
    }
}

