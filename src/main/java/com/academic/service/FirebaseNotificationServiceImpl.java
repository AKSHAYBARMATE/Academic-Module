package com.academic.service;

import com.academic.entity.User;
import com.academic.entity.UserDeviceToken;
import com.academic.repository.UserDeviceTokenRepository;
import com.academic.repository.UserRepository;
import com.academic.request.DeviceTokenRequest;
import com.academic.request.FeePaymentNotificationRequest;
import com.academic.request.SendNotificationRequest;
import com.academic.response.NotificationResponse;
import com.academic.response.StandardResponse;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

    private final UserDeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    // ── FCM multicast batch size limit ────────────────────────────────────────
    private static final int FCM_BATCH_SIZE = 500;

    // =========================================================================
    // Save device token
    // =========================================================================

    @Override
    @Transactional
    public StandardResponse<?> saveDeviceToken(Integer userId, DeviceTokenRequest request) {

        // Fetch user to know student/staff mapping
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return StandardResponse.error("User not found", "USER_NOT_FOUND", null);
        }
        User user = userOpt.get();

        String token = request.getDeviceToken().trim();

        // Upsert: if already exists, just make sure it is active
        Optional<UserDeviceToken> existing =
                deviceTokenRepository.findByUserIdAndDeviceToken(userId, token);

        if (existing.isPresent()) {
            UserDeviceToken dt = existing.get();
            dt.setActive(true);
            if (StringUtils.hasText(request.getDeviceType())) {
                dt.setDeviceType(request.getDeviceType());
            }
            deviceTokenRepository.save(dt);
            log.info("Device token refreshed for userId={}", userId);
            return StandardResponse.success("Device token refreshed successfully");
        }

        // New token – persist
        UserDeviceToken newToken = UserDeviceToken.builder()
                .userId(userId)
                .studentId(user.getStudentId())
                .staffId(user.getStaffId())
                .isStaff(user.isStaff())
                .deviceToken(token)
                .deviceType(request.getDeviceType())
                .isActive(true)
                .build();

        deviceTokenRepository.save(newToken);
        log.info("New device token saved for userId={}, isStaff={}", userId, user.isStaff());

        return StandardResponse.success("Device token saved successfully");
    }

    // =========================================================================
    // Remove device token
    // =========================================================================

    @Override
    @Transactional
    public StandardResponse<?> removeDeviceToken(Integer userId, String deviceToken) {
        int updated = deviceTokenRepository.deactivateToken(userId, deviceToken);
        if (updated == 0) {
            return StandardResponse.error("Token not found for this user", "TOKEN_NOT_FOUND", null);
        }
        log.info("Device token deactivated for userId={}", userId);
        return StandardResponse.success("Device token removed successfully");
    }

    // =========================================================================
    // Send notification
    // =========================================================================

    @Override
    public StandardResponse<NotificationResponse> sendNotification(SendNotificationRequest request) {

        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            log.warn("Cannot send notification: Firebase is not initialized");
            return StandardResponse.error("Firebase is not initialized on the server", "FIREBASE_NOT_CONFIGURED", null);
        }

        List<String> tokens = resolveTargetTokens(request);

        if (tokens.isEmpty()) {
            return StandardResponse.success(
                    NotificationResponse.builder()
                            .totalTargeted(0)
                            .successCount(0)
                            .failureCount(0)
                            .message("No active device tokens found for the given target")
                            .build(),
                    "No tokens to send"
            );
        }

        // Remove duplicates
        tokens = tokens.stream().distinct().collect(Collectors.toList());

        // Build notification object
        Notification notification = buildNotification(request);

        // FCM allows max 500 tokens per multicast – batch if needed
        int totalSuccess = 0;
        int totalFailure = 0;
        List<String> failedTokens = new ArrayList<>();

        List<List<String>> batches = partitionList(tokens, FCM_BATCH_SIZE);

        for (List<String> batch : batches) {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(batch)
                    .putAllData(request.getData() != null ? request.getData() : Collections.emptyMap())
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                totalSuccess += response.getSuccessCount();
                totalFailure += response.getFailureCount();

                // Collect failed tokens for logging / cleanup
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String failedToken = batch.get(i);
                        failedTokens.add(failedToken);
                        log.warn("FCM send failed for token={}, error={}",
                                failedToken,
                                responses.get(i).getException() != null
                                        ? responses.get(i).getException().getMessage()
                                        : "unknown");
                    }
                }

            } catch (FirebaseMessagingException e) {
                log.error("Firebase batch send error: {}", e.getMessage(), e);
                totalFailure += batch.size();
                failedTokens.addAll(batch);
            }
        }

        log.info("Notification sent: total={}, success={}, failure={}",
                tokens.size(), totalSuccess, totalFailure);

        NotificationResponse result = NotificationResponse.builder()
                .totalTargeted(tokens.size())
                .successCount(totalSuccess)
                .failureCount(totalFailure)
                .failedTokens(failedTokens.isEmpty() ? null : failedTokens)
                .message(String.format("Notification sent – %d succeeded, %d failed", totalSuccess, totalFailure))
                .build();

        return StandardResponse.success(result, "Notification dispatched");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Determine the list of FCM tokens to target based on the request.
     * Priority: explicit tokens > userIds > targetGroup
     */
    private List<String> resolveTargetTokens(SendNotificationRequest request) {

        // 1. Explicit token list
        if (!CollectionUtils.isEmpty(request.getTokens())) {
            return new ArrayList<>(request.getTokens());
        }

        // 2. Resolve by user ID list
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            return deviceTokenRepository
                    .findActiveTokensByUserIds(request.getUserIds())
                    .stream()
                    .map(UserDeviceToken::getDeviceToken)
                    .collect(Collectors.toList());
        }

        // 3. Resolve by group
        if (StringUtils.hasText(request.getTargetGroup())) {
            return switch (request.getTargetGroup().toUpperCase()) {
                case "STUDENTS" -> deviceTokenRepository.findAllActiveStudentTokens()
                        .stream().map(UserDeviceToken::getDeviceToken).collect(Collectors.toList());
                case "STAFF" -> deviceTokenRepository.findAllActiveStaffTokens()
                        .stream().map(UserDeviceToken::getDeviceToken).collect(Collectors.toList());
                case "ALL" -> deviceTokenRepository.findByIsActiveTrue()
                        .stream().map(UserDeviceToken::getDeviceToken).collect(Collectors.toList());
                default -> {
                    log.warn("Unknown targetGroup '{}', no tokens resolved", request.getTargetGroup());
                    yield Collections.emptyList();
                }
            };
        }

        return Collections.emptyList();
    }

    private Notification buildNotification(SendNotificationRequest request) {
        Notification.Builder builder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (StringUtils.hasText(request.getImageUrl())) {
            builder.setImage(request.getImageUrl());
        }

        return builder.build();
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    // =========================================================================
    // Fee-payment notification  (called by Accounts service)
    // =========================================================================

    @Override
    public StandardResponse<NotificationResponse> sendFeePaymentNotification(
            FeePaymentNotificationRequest req) {

        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            log.warn("Cannot send fee notification: Firebase is not initialized");
            return StandardResponse.error("Firebase is not initialized on the server", "FIREBASE_NOT_CONFIGURED", null);
        }

        // 1. Resolve student's active FCM tokens
        List<String> tokens = deviceTokenRepository
                .findByUserIdAndIsActiveTrue(req.getStudentUserId())
                .stream()
                .map(UserDeviceToken::getDeviceToken)
                .distinct()
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            log.warn("No active FCM tokens for studentUserId={}", req.getStudentUserId());
            return StandardResponse.success(
                    NotificationResponse.builder()
                            .totalTargeted(0)
                            .successCount(0)
                            .failureCount(0)
                            .message("No active device tokens found for this student")
                            .build(),
                    "No tokens to send"
            );
        }

        // 2. Build notification title & body
        String title = buildFeeNotificationTitle(req);
        String body  = buildFeeNotificationBody(req);

        // 3. Build FCM data payload (for in-app handling)
        Map<String, String> dataPayload = new LinkedHashMap<>();
        dataPayload.put("type",          "FEE_PAYMENT");
        dataPayload.put("studentUserId", String.valueOf(req.getStudentUserId()));
        if (req.getReceiptNumber()   != null) dataPayload.put("receiptNumber",   req.getReceiptNumber());
        if (req.getAmountPaid()      != null) dataPayload.put("amountPaid",      req.getAmountPaid().toPlainString());
        if (req.getRemainingBalance() != null) dataPayload.put("remainingBalance", req.getRemainingBalance().toPlainString());
        if (req.getDeepLinkScreen()  != null) dataPayload.put("screen",          req.getDeepLinkScreen());
        else                                   dataPayload.put("screen",          "FEE_RECEIPT");
        if (req.getExtraData() != null)        dataPayload.putAll(req.getExtraData());

        // 4. Send via FCM multicast
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        int totalSuccess = 0, totalFailure = 0;
        List<String> failedTokens = new ArrayList<>();

        for (List<String> batch : partitionList(tokens, FCM_BATCH_SIZE)) {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(batch)
                    .putAllData(dataPayload)
                    .build();
            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                totalSuccess += response.getSuccessCount();
                totalFailure += response.getFailureCount();

                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(batch.get(i));
                        log.warn("FCM fee-notification failed token={}, err={}",
                                batch.get(i),
                                responses.get(i).getException() != null
                                        ? responses.get(i).getException().getMessage() : "unknown");
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("Firebase fee-notification batch error: {}", e.getMessage(), e);
                totalFailure += batch.size();
                failedTokens.addAll(batch);
            }
        }

        log.info("Fee payment notification sent to studentUserId={}: success={}, failure={}",
                req.getStudentUserId(), totalSuccess, totalFailure);

        NotificationResponse result = NotificationResponse.builder()
                .totalTargeted(tokens.size())
                .successCount(totalSuccess)
                .failureCount(totalFailure)
                .failedTokens(failedTokens.isEmpty() ? null : failedTokens)
                .message(String.format("Fee notification sent – %d succeeded, %d failed",
                        totalSuccess, totalFailure))
                .build();

        return StandardResponse.success(result, "Fee payment notification dispatched");
    }

    // ── Fee notification message builders ─────────────────────────────────────

    private String buildFeeNotificationTitle(FeePaymentNotificationRequest req) {
        String name = StringUtils.hasText(req.getStudentName())
                ? req.getStudentName() : "Student";
        return "✅ Fee Payment Received – " + name;
    }

    private String buildFeeNotificationBody(FeePaymentNotificationRequest req) {
        StringBuilder sb = new StringBuilder();

        // Fee heads
        if (!CollectionUtils.isEmpty(req.getPaidFeeHeads())) {
            sb.append("Fee Heads: ").append(String.join(", ", req.getPaidFeeHeads())).append(".\n");
        }

        // Amount paid
        if (req.getAmountPaid() != null) {
            sb.append("Amount Paid: ₹").append(formatAmount(req.getAmountPaid())).append(".");
        }

        // Paid via (bank/mode)
        if (StringUtils.hasText(req.getPaidVia())) {
            sb.append(" Via ").append(req.getPaidVia()).append(".");
        }

        // Remaining balance
        if (req.getRemainingBalance() != null) {
            BigDecimal remaining = req.getRemainingBalance();
            if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                sb.append("\nBalance: ₹0 – Fully Paid! 🎉");
            } else {
                sb.append("\nRemaining Balance: ₹").append(formatAmount(remaining)).append(".");
            }
        }

        // Receipt number
        if (StringUtils.hasText(req.getReceiptNumber())) {
            sb.append("\nReceipt No: ").append(req.getReceiptNumber());
        }

        // Payment date
        if (req.getPaymentDate() != null) {
            sb.append(" (")
              .append(req.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
              .append(")");
        }

        return sb.toString().trim();
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }
}
