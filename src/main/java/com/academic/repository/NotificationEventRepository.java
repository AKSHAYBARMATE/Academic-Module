package com.academic.repository;

import com.academic.entity.NotificationEvent;
import com.academic.entity.NotificationEvent.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    /** All notifications for a specific user (paginated for mobile history). */
    Page<NotificationEvent> findByTargetUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    /** Notifications for a user filtered by event type. */
    Page<NotificationEvent> findByTargetUserIdAndEventTypeOrderByCreatedAtDesc(
            Integer userId, String eventType, Pageable pageable);

    /** All notifications for a student by studentId. */
    Page<NotificationEvent> findByStudentIdOrderByCreatedAtDesc(Integer studentId, Pageable pageable);

    /** All notifications for a staff by staffId. */
    Page<NotificationEvent> findByStaffIdOrderByCreatedAtDesc(Integer staffId, Pageable pageable);

    /** Notifications by event type (admin dashboard). */
    Page<NotificationEvent> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);

    /** Notifications by status (e.g. find all FAILED to retry). */
    List<NotificationEvent> findByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);

    /** Count by status (for dashboard stats). */
    long countByStatus(NotificationStatus status);

    /** Notifications by source module. */
    Page<NotificationEvent> findBySourceModuleOrderByCreatedAtDesc(String sourceModule, Pageable pageable);

    /** Notifications linked to a specific reference (e.g. fee payment id). */
    List<NotificationEvent> findByReferenceIdAndEventType(String referenceId, String eventType);

    /** Recent notifications sent after a given timestamp. */
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.createdAt >= :since ORDER BY ne.createdAt DESC")
    List<NotificationEvent> findRecentEvents(LocalDateTime since);
}
