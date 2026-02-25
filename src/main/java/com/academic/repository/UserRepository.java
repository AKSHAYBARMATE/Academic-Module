package com.academic.repository;

import com.academic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT student_id FROM users WHERE id = ?1", nativeQuery = true)
    Optional<Long> findStudentIdByUserId(Long userId);

    @Query(value = "SELECT staff_id FROM users WHERE id = ?1", nativeQuery = true)
    Optional<Long> findStaffIdByUserId(Long userId);
}
