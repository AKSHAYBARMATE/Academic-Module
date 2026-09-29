package com.academic.service;

import com.academic.entity.NotificationEvent;
import com.academic.entity.NotificationEvent.NotificationStatus;
import com.academic.entity.User;
import com.academic.entity.UserDeviceToken;
import com.academic.repository.NotificationEventRepository;
import com.academic.repository.UserDeviceTokenRepository;
import com.academic.repository.UserRepository;
import com.academic.request.NotificationEventRequest;
import com.academic.response.NotificationEventResponse;
import com.academic.response.StandardResponse;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventServiceImpl implements NotificationEventService {

    private final NotificationEventRepository eventRepository;
    private final UserDeviceTokenRepository    deviceTokenRepository;
    private final UserRepository               userRepository;

    private static final int FCM_BATCH_SIZE = 500;

    // =========================================================================
    // Common save + send
    // =========================================================================

    @Override
    @Transactional
    public StandardResponse<NotificationEventResponse> saveAndSend(NotificationEventRequest req) {

        // ── 1. Resolve target users & their FCM tokens ────────────────────────
        // Map: userId → UserDeviceToken list
        Map<Integer, List<UserDeviceToken>> userTokenMap = resolveUserTokenMap(req);

        if (userTokenMap.isEmpty()) {
            log.warn("No active tokens found for eventType={}, targetGroup={}, userIds={}",
                    req.getEventType(), req.getTargetGroup(), req.getTargetUserIds());

            // Save a single SKIPPED record for audit
            NotificationEvent skippedEvent = buildBaseEvent(req, null, null)
                    .status(NotificationStatus.SKIPPED)
                    .build();
            eventRepository.save(skippedEvent);

            return StandardResponse.success(
                    NotificationEventResponse.builder()
                            .totalTargeted(0).successCount(0).failureCount(0)
                            .status(NotificationStatus.SKIPPED)
                            .savedEventIds(List.of(skippedEvent.getId()))
                            .message("No active device tokens found")
                            .dispatchedAt(LocalDateTime.now())
                            .build(),
                    "No tokens to send"
            );
        }

        // ── 2. Save PENDING event records (one per user) ──────────────────────
        Map<Integer, NotificationEvent> userEventMap = new LinkedHashMap<>();
        for (Integer userId : userTokenMap.keySet()) {
            User user = userRepository.findById(userId).orElse(null);
            NotificationEvent event = buildBaseEvent(req, userId, user)
                    .status(NotificationStatus.PENDING)
                    .build();
            userEventMap.put(userId, eventRepository.save(event));
        }

        // ── 3. Flatten all tokens, keeping track of which token → userId ──────
        // token → userId (for per-token result mapping)
        Map<String, Integer> tokenUserMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<UserDeviceToken>> entry : userTokenMap.entrySet()) {
            for (UserDeviceToken dt : entry.getValue()) {
                tokenUserMap.put(dt.getDeviceToken(), entry.getKey());
            }
        }
        List<String> allTokens = new ArrayList<>(tokenUserMap.keySet());

        // ── 4. Build FCM notification ──────────────────────────────────────────
        Notification notification = Notification.builder()
                .setTitle(req.getTitle())
                .setBody(req.getBody())
                .build();

        if (StringUtils.hasText(req.getImageUrl())) {
            notification = Notification.builder()
                    .setTitle(req.getTitle())
                    .setBody(req.getBody())
                    .setImage(req.getImageUrl())
                    .build();
        }

        Map<String, String> dataPayload = req.getData() != null
                ? new LinkedHashMap<>(req.getData()) : Collections.emptyMap();

        // ── 5. Send in batches & collect per-token results ────────────────────
        // userId → (successCount, failureCount, fcmMessageId, fcmError)
        Map<Integer, String> userFcmMessageId = new HashMap<>();
        Map<Integer, String> userFcmError     = new HashMap<>();
        Set<Integer> succeededUsers            = new HashSet<>();
        Set<Integer> failedUsers               = new HashSet<>();

        int totalSuccess = 0, totalFailure = 0;

        List<List<String>> batches = partitionList(allTokens, FCM_BATCH_SIZE);
        for (List<String> batch : batches) {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(batch)
                    .putAllData(dataPayload)
                    .build();
            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                List<SendResponse> responses = response.getResponses();

                for (int i = 0; i < responses.size(); i++) {
                    String token  = batch.get(i);
                    Integer uid   = tokenUserMap.get(token);
                    SendResponse sr = responses.get(i);

                    if (sr.isSuccessful()) {
                        totalSuccess++;
                        succeededUsers.add(uid);
                        userFcmMessageId.put(uid, sr.getMessageId());
                    } else {
                        totalFailure++;
                        failedUsers.add(uid);
                        String err = sr.getException() != null
                                ? sr.getException().getMessage() : "FCM_ERROR";
                        userFcmError.put(uid, err);
                        log.warn("FCM failed token={}, userId={}, err={}", token, uid, err);
                    }
                }
                log.info("FCM batch: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());

            } catch (FirebaseMessagingException e) {
                log.error("FCM batch exception for eventType={}: {}", req.getEventType(), e.getMessage(), e);
                // Mark all in this batch as failed
                for (String token : batch) {
                    Integer uid = tokenUserMap.get(token);
                    totalFailure++;
                    failedUsers.add(uid);
                    userFcmError.put(uid, e.getMessage());
                }
            }
        }

        // ── 6. Update event records with FCM results ──────────────────────────
        LocalDateTime now = LocalDateTime.now();
        List<Long> savedIds = new ArrayList<>();

        for (Map.Entry<Integer, NotificationEvent> entry : userEventMap.entrySet()) {
            Integer uid   = entry.getKey();
            NotificationEvent ev = entry.getValue();

            if (succeededUsers.contains(uid)) {
                ev.setStatus(NotificationStatus.SENT);
                ev.setFcmMessageId(userFcmMessageId.get(uid));
            } else {
                ev.setStatus(NotificationStatus.FAILED);
                ev.setFcmError(userFcmError.getOrDefault(uid, "UNKNOWN"));
            }
            ev.setSentAt(now);
            eventRepository.save(ev);
            savedIds.add(ev.getId());
        }

        NotificationStatus overallStatus = (totalFailure == 0) ? NotificationStatus.SENT
                : (totalSuccess == 0) ? NotificationStatus.FAILED
                : NotificationStatus.SENT; // partial success → SENT

        log.info("Notification event saved & sent: type={}, total={}, success={}, failure={}",
                req.getEventType(), allTokens.size(), totalSuccess, totalFailure);

        return StandardResponse.success(
                NotificationEventResponse.builder()
                        .totalTargeted(allTokens.size())
                        .successCount(totalSuccess)
                        .failureCount(totalFailure)
                        .savedEventIds(savedIds)
                        .status(overallStatus)
                        .message(String.format("Event saved & FCM sent – %d succeeded, %d failed",
                                totalSuccess, totalFailure))
                        .dispatchedAt(now)
                        .build(),
                "Notification event dispatched"
        );
    }

    // =========================================================================
    // History queries
    // =========================================================================

    @Override
    public StandardResponse<?> getHistoryByUser(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEvent> result =
                eventRepository.findByTargetUserIdOrderByCreatedAtDesc(userId, pageable);
        return StandardResponse.success(result, "Notification history fetched");
    }

    @Override
    public StandardResponse<?> getHistoryByEventType(String eventType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEvent> result =
                eventRepository.findByEventTypeOrderByCreatedAtDesc(eventType, pageable);
        return StandardResponse.success(result, "Events fetched for type: " + eventType);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Resolves userId → [UserDeviceToken] map from the request targets.
     */
    private Map<Integer, List<UserDeviceToken>> resolveUserTokenMap(NotificationEventRequest req) {
        Map<Integer, List<UserDeviceToken>> result = new LinkedHashMap<>();

        if (!CollectionUtils.isEmpty(req.getTargetUserIds())) {
            // Per-user targeting
            for (Integer uid : req.getTargetUserIds()) {
                List<UserDeviceToken> tokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(uid);
                if (!tokens.isEmpty()) {
                    result.put(uid, tokens);
                }
            }
        } else if (StringUtils.hasText(req.getTargetGroup())) {
            // Group targeting
            List<UserDeviceToken> tokens = switch (req.getTargetGroup().toUpperCase()) {
                case "STUDENTS" -> deviceTokenRepository.findAllActiveStudentTokens();
                case "STAFF"    -> deviceTokenRepository.findAllActiveStaffTokens();
                case "ALL"      -> deviceTokenRepository.findByIsActiveTrue();
                default -> Collections.emptyList();
            };
            // Group by userId
            for (UserDeviceToken dt : tokens) {
                result.computeIfAbsent(dt.getUserId(), k -> new ArrayList<>()).add(dt);
            }
        }
        return result;
    }

    /** Builds the base NotificationEvent builder with all common fields. */
    private NotificationEvent.NotificationEventBuilder buildBaseEvent(
            NotificationEventRequest req, Integer userId, User user) {

        return NotificationEvent.builder()
                .eventType(req.getEventType())
                .eventSubType(req.getEventSubType())
                .title(req.getTitle())
                .body(req.getBody())
                .imageUrl(req.getImageUrl())
                .targetUserId(userId)
                .studentId(user != null ? user.getStudentId() : null)
                .staffId(user != null ? user.getStaffId() : null)
                .isStaff(user != null ? user.isStaff() : null)
                .targetGroup(req.getTargetGroup())
                .referenceId(req.getReferenceId())
                .sourceModule(req.getSourceModule());
    }

    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return parts;
    }
}
