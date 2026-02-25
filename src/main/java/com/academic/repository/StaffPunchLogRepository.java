package com.academic.repository;

import com.academic.entity.StaffPunchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StaffPunchLogRepository extends JpaRepository<StaffPunchLog, Long> {
    Optional<StaffPunchLog> findByStaffIdAndWorkDate(Integer employeeId, String workDate);
}
