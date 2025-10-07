package com.academic.repository;

import com.academic.entity.TeacherAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    Optional<TeacherAssignment> findByIdAndIsDeletedFalse(Long id);
    Page<TeacherAssignment> findByIsDeletedFalse(Pageable pageable);
    Page<TeacherAssignment> findAll(Specification<TeacherAssignment> spec, Pageable pageable);
}
