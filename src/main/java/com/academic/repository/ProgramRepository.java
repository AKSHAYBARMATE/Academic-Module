package com.academic.repository;

import com.academic.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    Optional<Program> findByIdAndIsDeletedFalse(Integer id);

    Optional<Program> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String code, Integer id);

    List<Program> findByIsDeletedFalseOrderByCodeAsc();

    List<Program> findByDegreeCodeIgnoreCaseAndIsDeletedFalseOrderByCodeAsc(String degreeCode);

    List<Program> findByIsDeletedFalseAndStatusOrderByCodeAsc(String status);

    @Query("SELECT p FROM Program p " +
            "WHERE p.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.degreeCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:degreeCode IS NULL OR LOWER(p.degreeCode) = LOWER(:degreeCode)) " +
            "AND (:degreeType IS NULL OR LOWER(p.degreeType) = LOWER(:degreeType)) " +
            "AND (:status IS NULL OR LOWER(p.status) = LOWER(:status)) " +
            "ORDER BY p.id DESC")
    Page<Program> searchAndFilter(
            @Param("search") String search,
            @Param("degreeCode") String degreeCode,
            @Param("degreeType") String degreeType,
            @Param("status") String status,
            Pageable pageable
    );
}
