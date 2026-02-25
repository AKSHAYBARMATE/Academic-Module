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
}
