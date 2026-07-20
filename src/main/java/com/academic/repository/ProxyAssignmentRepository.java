package com.academic.repository;

import com.academic.entity.ProxyAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProxyAssignmentRepository extends JpaRepository<ProxyAssignment, Long> {

    Optional<ProxyAssignment> findByIdAndIsDeletedFalse(Long id);

    List<ProxyAssignment> findByIsDeletedFalse();

    boolean existsBySlotIdAndProxyDateAndIsDeletedFalse(Long slotId, LocalDate proxyDate);

    @Query("SELECT pa FROM ProxyAssignment pa " +
           "WHERE pa.isDeleted = false " +
           "AND (:templateId IS NULL OR pa.template.id = :templateId) " +
           "AND (:timetableId IS NULL OR pa.timetableId = :timetableId) " +
           "AND (:proxyDate IS NULL OR pa.proxyDate = :proxyDate) " +
           "AND (:status IS NULL OR pa.status = :status) " +
           "ORDER BY pa.proxyDate DESC, pa.startTime ASC")
    Page<ProxyAssignment> findAllAssignments(
            @Param("templateId") Long templateId,
            @Param("timetableId") Long timetableId,
            @Param("proxyDate") LocalDate proxyDate,
            @Param("status") String status,
            Pageable pageable);
            
    List<ProxyAssignment> findByTemplateSubstituteTeacherIdAndIsDeletedFalse(Long teacherId);


    ProxyAssignment findByProxyDateAndTemplateSubstituteTeacherIdAndIsClassTeacher(LocalDate date, Long staffId, boolean b);
}
