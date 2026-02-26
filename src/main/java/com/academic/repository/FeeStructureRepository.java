package com.academic.repository;

import com.academic.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    /**
     * Find the active fee structure for a given class (CommonMaster id) and
     * academic year (CommonMaster id).
     * Falls back to any un-deleted structure for the class if academic_year_id is
     * not matched.
     */
    @Query("SELECT fs FROM FeeStructure fs " +
            "WHERE fs.classId.id = :classId " +
            "AND fs.academicYear.id = :academicYearId " +
            "AND (fs.isDeleted IS NULL OR fs.isDeleted = false)")
    Optional<FeeStructure> findByClassIdAndAcademicYearId(
            @Param("classId") Long classId,
            @Param("academicYearId") Long academicYearId);

    /**
     * Fallback: find any active fee structure for a class regardless of academic
     * year.
     */
    @Query("SELECT fs FROM FeeStructure fs " +
            "WHERE fs.classId.id = :classId " +
            "AND (fs.isDeleted IS NULL OR fs.isDeleted = false) " +
            "ORDER BY fs.effectiveFrom DESC")
    List<FeeStructure> findByClassIdOrderByEffectiveFromDesc(@Param("classId") Long classId);
}
