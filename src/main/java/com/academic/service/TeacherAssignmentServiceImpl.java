package com.academic.service;

import com.academic.entity.TeacherAssignment;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.TeacherAssignmentMapper;
import com.academic.repository.CommonMasterRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private final TeacherAssignmentRepository repository;
    private final CommonMasterRepository commonMasterRepository;

    @Override
    public TeacherAssignmentResponse create(TeacherAssignmentRequest request) {
        if (request.getClassIds() == null || request.getClassIds().isEmpty()) {
            throw new CustomException("No classes selected", "NO_CLASSES_SELECTED",
                    "Please select at least one class for the assignment.");
        }

        TeacherAssignment entity = TeacherAssignmentMapper.toEntity(request);
        TeacherAssignment saved = repository.save(entity);
        return TeacherAssignmentMapper.toResponse(saved, commonMasterRepository);
    }

    @Override
    public TeacherAssignmentResponse update(Long id, TeacherAssignmentRequest request) {
        TeacherAssignment existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        TeacherAssignmentMapper.updateEntity(existing, request);
        TeacherAssignment updated = repository.save(existing);
        return TeacherAssignmentMapper.toResponse(updated, commonMasterRepository);
    }

    @Override
    public TeacherAssignmentResponse getById(Long id) {
        TeacherAssignment entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        return TeacherAssignmentMapper.toResponse(entity, commonMasterRepository);
    }

    @Override
    public Page<TeacherAssignmentResponse> getAll(int page, int size, String search, String status, String year) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<TeacherAssignment> spec = withFilters(search, status, year,null);

        return repository.findAll(spec, pageable)
                .map(entity -> TeacherAssignmentMapper.toResponse(entity, commonMasterRepository));
    }


    public static Specification<TeacherAssignment> withFilters(
            String search, String status, String year, List<Integer> classIds) {

        return (root, query, cb) -> {
            // Base condition: only non-deleted
            Predicate predicate = cb.equal(root.get("isDeleted"), false);

            // Search by teacherName, subject, employeeId
            if (search != null && !search.isEmpty()) {
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("teacherName").as(String.class)), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("subject").as(String.class)), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("employeeId").as(String.class)), "%" + search.toLowerCase() + "%")
                );
                predicate = cb.and(predicate, searchPredicate);
            }

            // Filter by status
            if (status != null && !status.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            // Filter by year (assuming stored in classes string like "classId-year")
            if (year != null && !year.isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("classes").as(String.class), "%" + year + "%"));
            }

//            // Filter by classIds list (comma-separated string in DB)
//            if (classIds != null && !classIds.isEmpty()) {
//                Predicate classPredicate = cb.disjunction();
//                for (Integer id : classIds) {
//                    classPredicate = cb.or(classPredicate, cb.like(root.get("classes").as(String.class), "%" + id + "%"));
//                }
//                predicate = cb.and(predicate, classPredicate);
//            }

            return predicate;
        };
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




}
