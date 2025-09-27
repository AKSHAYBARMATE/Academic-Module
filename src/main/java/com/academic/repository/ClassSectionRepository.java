package com.academic.repository;

import com.academic.entity.ClassSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    Page<ClassSection> findByIsDeletedFalse(Pageable pageable);

    Optional<ClassSection> findByIdAndIsDeletedFalse(Long id);
}
