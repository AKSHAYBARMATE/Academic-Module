package com.academic.repository;

import com.academic.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    /**
     * Fetch all payments for a student ordered by most recent first.
     * FeePayment.studentId is a @ManyToOne to Student entity.
     */
    @Query("SELECT fp FROM FeePayment fp " +
            "WHERE fp.studentId.id = :studentId " +
            "ORDER BY fp.paidAt DESC")
    List<FeePayment> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * Fetch payments for a student in a specific academic year.
     */
    @Query("SELECT fp FROM FeePayment fp " +
            "WHERE fp.studentId.id = :studentId " +
            "AND fp.academicYear = :academicYear " +
            "ORDER BY fp.paidAt DESC")
    List<FeePayment> findByStudentIdAndAcademicYear(
            @Param("studentId") Integer studentId,
            @Param("academicYear") Integer academicYear);
}
