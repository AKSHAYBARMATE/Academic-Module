package com.academic.service;

import com.academic.entity.ExamSetup;
import com.academic.exception.CustomException;
import com.academic.mapper.ExamSetupMapper;
import com.academic.repository.ExamSetupRepository;
import com.academic.request.ExamSetupRequest;
import com.academic.response.ExamSetupResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamSetupServiceImpl implements ExamSetupService {

    private final ExamSetupRepository repository;
    private final ExamSetupMapper mapper;

    @Override
    public ExamSetupResponse create(ExamSetupRequest request) {
        log.info("Creating ExamSetup: {}", request);

        // Check for duplicate examName within same class and academic
        boolean exists = repository.existsByExamNameAndClassIdAndAcademicYearIdAndIsDeletedFalse(
                request.getExamName(),
                request.getClassId(),
                request.getAcademicYearId()
        );
        if (exists) {
            throw new CustomException(
                    "Exam setup with this name already exists for the class and academic year",
                    "EX409",
                    "Duplicate examName: " + request.getExamName()
            );
        }

        ExamSetup entity = mapper.toEntity(request);
        ExamSetup saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public ExamSetupResponse update(Long id, ExamSetupRequest request) {
        log.info("Updating ExamSetup ID: {}", id);

        ExamSetup existing = repository.findById(id)
                .filter(e -> !e.getIsDeleted())
                .orElseThrow(() -> new CustomException("Exam setup not found", "EX404", "Invalid ID: " + id));

        // Check for duplicate examName excluding current record
        boolean exists = repository.existsByExamNameAndClassIdAndAcademicYearIdAndIsDeletedFalseAndIdNot(
                request.getExamName(),
                request.getClassId(),
                request.getAcademicYearId(),
                id
        );
        if (exists) {
            throw new CustomException(
                    "Another exam with this name already exists for the class and academic year",
                    "EX409",
                    "Duplicate examName: " + request.getExamName()
            );
        }

        existing.setExamName(request.getExamName());
        existing.setClassId(request.getClassId());
        existing.setSubjectId(request.getSubjectId());
        existing.setAcademicYearId(request.getAcademicYearId());
        existing.setExamDate(request.getExamDate());
        existing.setMaxMarks(request.getMaxMarks());

        ExamSetup updated = repository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting ExamSetup ID: {}", id);
        ExamSetup entity = repository.findById(id)
                .filter(e -> !e.getIsDeleted())
                .orElseThrow(() -> new CustomException("Exam setup not found", "EX404", "Invalid ID: " + id));

        entity.setIsDeleted(true);
        repository.save(entity);
    }

    @Override
    public ExamSetupResponse getById(Long id) {
        log.info("Fetching ExamSetup ID: {}", id);
        ExamSetup entity = repository.findById(id)
                .filter(e -> !e.getIsDeleted())
                .orElseThrow(() -> new CustomException("Exam setup not found", "EX404", "Invalid ID: " + id));

        return mapper.toResponse(entity);
    }

    @Override
    public Page<ExamSetupResponse> getAllFiltered(String examName, Long classId, Long academicId, int page, int size) {
        log.info("Fetching ExamSetups with filters - examName: {}, classId: {}, academicId: {}, page: {}, size: {}",
                examName, classId, academicId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<ExamSetup> examSetups = repository.findAll(
                filter(examName, classId, academicId),
                pageable
        );

        return examSetups.map(mapper::toResponse);
    }


    public static Specification<ExamSetup> filter(String examName, Long classId, Long academicId) {
        return (root, query, cb) -> {
            Predicate predicate = cb.isFalse(root.get("isDeleted")); // Only non-deleted

            if (examName != null && !examName.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("examName")), "%" + examName.toLowerCase() + "%"));
            }
            if (classId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("classId"), classId));
            }
            if (academicId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("academicId"), academicId));
            }
            return predicate;
        };
    }



}
