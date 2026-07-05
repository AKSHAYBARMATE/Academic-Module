package com.academic.repository;

import com.academic.entity.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, Long> {

        List<StudentAttendance> findByStudentIdAndAttendanceDateBetween(Long studentId, LocalDate startDate,
                        LocalDate endDate);

        Optional<StudentAttendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);

        @Query("SELECT COUNT(a) FROM StudentAttendance a WHERE a.studentId = :studentId AND a.status = :status AND a.attendanceDate BETWEEN :startDate AND :endDate")
        long countByStudentIdAndStatusAndDateBetween(@Param("studentId") Long studentId,
                        @Param("status") StudentAttendance.AttendanceStatus status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        boolean existsByStudentIdInAndAttendanceDate(java.util.Collection<Long> studentIds, LocalDate date);

        /**
         * Fetch all attendance records for the given student IDs within a date range.
         * Used for the monthly attendance calendar.
         */
        List<StudentAttendance> findByStudentIdInAndAttendanceDateBetween(
                        java.util.Collection<Long> studentIds,
                        LocalDate startDate,
                        LocalDate endDate);

        /**
         * Count students with a specific status on a specific date from a given set.
         */
        @Query("SELECT COUNT(a) FROM StudentAttendance a WHERE a.studentId IN :studentIds AND a.attendanceDate = :date AND a.status = :status")
        long countByStudentIdInAndAttendanceDateAndStatus(
                        @Param("studentIds") java.util.Collection<Long> studentIds,
                        @Param("date") LocalDate date,
                        @Param("status") StudentAttendance.AttendanceStatus status);
}
