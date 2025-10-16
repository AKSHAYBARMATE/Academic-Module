package com.academic.service;

import com.academic.entity.ExamSetup;
import com.academic.exception.CustomException;
import com.academic.mapper.ExamSetupMapper;
import com.academic.repository.ExamSetupRepository;
import com.academic.request.ExamSetupRequest;
import com.academic.response.ExamSetupResponse;
import com.academic.response.StandardResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public StandardResponse<Map<String, Object>> getAllFiltered(
            String examName, Long classId, Long academicId, int page, int size) {

        log.info("Fetching ExamSetups with filters - examName: {}, classId: {}, academicId: {}, page: {}, size: {}",
                examName, classId, academicId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        // Fetch paginated data
        Page<ExamSetup> examSetups = repository.findAll(
                filter(examName, classId, academicId),
                pageable
        );

        // Map entities to response DTOs
        List<ExamSetupResponse> responseList = examSetups.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        // --- Pageable Metadata ---
        Map<String, Object> sortMap = Map.of(
                "empty", examSetups.getSort().isEmpty(),
                "sorted", examSetups.getSort().isSorted(),
                "unsorted", examSetups.getSort().isUnsorted()
        );

        Map<String, Object> pageableMap = Map.of(
                "pageNumber", examSetups.getNumber(),
                "pageSize", examSetups.getSize(),
                "sort", sortMap,
                "offset", examSetups.getPageable().getOffset(),
                "paged", examSetups.getPageable().isPaged(),
                "unpaged", examSetups.getPageable().isUnpaged()
        );

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("content", responseList);
        dataMap.put("pageable", pageableMap);
        dataMap.put("last", examSetups.isLast());
        dataMap.put("totalPages", examSetups.getTotalPages());
        dataMap.put("totalElements", examSetups.getTotalElements());
        dataMap.put("size", examSetups.getSize());
        dataMap.put("number", examSetups.getNumber());
        dataMap.put("first", examSetups.isFirst());
        dataMap.put("numberOfElements", examSetups.getNumberOfElements());
        dataMap.put("sort", sortMap);
        dataMap.put("empty", examSetups.isEmpty());

        // Return as StandardResponse with Map
        return  StandardResponse.success(dataMap, "Subjects fetched successfully");
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
