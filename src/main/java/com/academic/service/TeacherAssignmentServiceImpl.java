package com.academic.service;

import com.academic.entity.TeacherAssignment;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.TeacherAssignmentMapper;
import com.academic.repository.TeacherAssignmentRepository;
import com.academic.request.TeacherAssignmentRequest;
import com.academic.response.TeacherAssignmentResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {
    private final TeacherAssignmentRepository repository;

    @Override
    public TeacherAssignmentResponse create(TeacherAssignmentRequest request) {
        log.info("Creating new TeacherAssignment: {}", request);
        TeacherAssignment entity = TeacherAssignmentMapper.toEntity(request);
        TeacherAssignment saved = repository.save(entity);
        log.info("TeacherAssignment created successfully with id: {}", saved.getId());
        return TeacherAssignmentMapper.toResponse(saved);
    }

    @Override
    public TeacherAssignmentResponse update(Long id, TeacherAssignmentRequest request) {
        log.info("Updating TeacherAssignment with id: {}", id);
        TeacherAssignment existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        TeacherAssignmentMapper.updateEntity(existing, request);
        TeacherAssignment updated = repository.save(existing);
        log.info("Assignment updated successfully with id: {}", updated.getId());
        return TeacherAssignmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Soft deleting Assignment with id: {}", id);
        TeacherAssignment existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("Assignment soft deleted successfully with id: {}", id);
    }

    @Override
    public TeacherAssignmentResponse getById(Long id) {
        log.info("Fetching Assignment with id: {}", id);
        TeacherAssignment entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        return TeacherAssignmentMapper.toResponse(entity);
    }

    @Override
    public Page<TeacherAssignmentResponse> getAll(int page, int size, String search, String status, String year) {
        log.info("Fetching filtered Teacher Assignments. Search: {}, Status: {}, Year: {}, Page: {}, Size: {}",
                search, status, year, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Specification<TeacherAssignment> spec = withFilters(search, status, year);

        return repository.findAll(spec, pageable)
                .map(TeacherAssignmentMapper::toResponse);
    }

    public static Specification<TeacherAssignment> withFilters(
            String search, String status, String year) {
        return (root, query, cb) -> {
            // base condition
            Predicate predicate = cb.equal(root.get("isDeleted"), false);

            if (search != null && !search.isEmpty()) {
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("teacherName").as(String.class)), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("subject").as(String.class)), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("classes").as(String.class)), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("employeeId").as(String.class)), "%" + search.toLowerCase() + "%")
                );
                predicate = cb.and(predicate, searchPredicate);
            }

            if (status != null && !status.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (year != null && !year.isEmpty()) {
                predicate = cb.and(predicate,
                        cb.like(root.get("classes").as(String.class), "%" + year + "%")); // adjust column if needed
            }

            return predicate;
        };
    }

}
