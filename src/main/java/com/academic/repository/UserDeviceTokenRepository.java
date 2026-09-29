package com.academic.repository;

import com.academic.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    /** All active tokens for a given user (multiple devices). */
    List<UserDeviceToken> findByUserIdAndIsActiveTrue(Integer userId);

    /** All active student tokens. */
    List<UserDeviceToken> findByStudentIdAndIsActiveTrue(Integer studentId);

    /** All active staff tokens. */
    List<UserDeviceToken> findByStaffIdAndIsActiveTrue(Integer staffId);

    /** Exact token lookup to avoid duplicates. */
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Integer userId, String deviceToken);

    /** All active tokens for a list of user-ids (bulk send). */
    @Query("SELECT udt FROM UserDeviceToken udt WHERE udt.userId IN :userIds AND udt.isActive = true")
    List<UserDeviceToken> findActiveTokensByUserIds(List<Integer> userIds);

    /** All active tokens for students (broadcast). */
    @Query("SELECT udt FROM UserDeviceToken udt WHERE udt.isStaff = false AND udt.isActive = true")
    List<UserDeviceToken> findAllActiveStudentTokens();

    /** All active tokens for staff (broadcast). */
    @Query("SELECT udt FROM UserDeviceToken udt WHERE udt.isStaff = true AND udt.isActive = true")
    List<UserDeviceToken> findAllActiveStaffTokens();

    /** All active tokens regardless of role (broadcast to everyone). */
    List<UserDeviceToken> findByIsActiveTrue();

    /** Deactivate old tokens when a user logs out. */
    @Modifying
    @Transactional
    @Query("UPDATE UserDeviceToken udt SET udt.isActive = false WHERE udt.userId = :userId AND udt.deviceToken = :deviceToken")
    int deactivateToken(Integer userId, String deviceToken);
}
