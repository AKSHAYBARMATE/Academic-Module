package com.academic.repository;

import com.academic.entity.Degree;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DegreeRepository extends JpaRepository<Degree, Integer> {

    Optional<Degree> findByIdAndIsDeletedFalse(Integer id);

    Optional<Degree> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String code, Integer id);

    List<Degree> findByIsDeletedFalseOrderByCodeAsc();

    List<Degree> findByIsDeletedFalseAndStatusOrderByCodeAsc(String status);

    @Query("SELECT d FROM Degree d " +
            "WHERE d.isDeleted = false " +
            "AND (:search IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:degreeType IS NULL OR LOWER(d.degreeType) = LOWER(:degreeType)) " +
            "AND (:status IS NULL OR LOWER(d.status) = LOWER(:status)) " +
            "ORDER BY d.id DESC")
    Page<Degree> searchAndFilter(
            @Param("search") String search,
            @Param("degreeType") String degreeType,
            @Param("status") String status,
            Pageable pageable
    );
}
