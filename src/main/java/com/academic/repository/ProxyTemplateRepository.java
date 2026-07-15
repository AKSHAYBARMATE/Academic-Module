package com.academic.repository;

import com.academic.entity.ProxyTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProxyTemplateRepository extends JpaRepository<ProxyTemplate, Long> {

    Optional<ProxyTemplate> findByIdAndIsDeletedFalse(Long id);

    List<ProxyTemplate> findByIsDeletedFalse();

    @Query("SELECT pt FROM ProxyTemplate pt " +
           "WHERE pt.isDeleted = false " +
           "AND (:search IS NULL OR LOWER(pt.templateName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(pt.substituteTeacherName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProxyTemplate> findAllTemplates(@Param("search") String search, Pageable pageable);
}
