package com.academic.repository;

import com.academic.entity.TimeTable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeTableRepository extends JpaRepository<TimeTable, Long> {

    @Query("SELECT t FROM TimeTable t " +
            "WHERE t.isDeleted = false " +
            "AND (:classId IS NULL OR t.classId = :classId) " +
            "AND (:sectionId IS NULL OR t.sectionId = :sectionId) " +
            "AND (:search IS NULL OR LOWER(t.timetableName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TimeTable> findAllByFilters(
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("search") String search,
            Pageable pageable);

    List<TimeTable> findByIsDeletedFalse();

    Optional<TimeTable> findByIdAndIsDeletedFalse(Long id);

    boolean existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalseAndIdNot(@NotBlank String timetableName, @NotNull Long classId, Long sectionId, Long id);

    boolean existsByTimetableNameAndIsDeletedFalse(@NotBlank String timetableName);

    boolean existsByTimetableNameAndClassIdAndSectionIdAndIsDeletedFalse(@NotBlank String timetableName, @NotNull Long classId, Long sectionId);
}