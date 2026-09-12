package com.academic.service;

import com.academic.entity.Degree;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.DegreeMapper;
import com.academic.repository.DegreeRepository;
import com.academic.request.DegreeRequest;
import com.academic.response.DegreeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DegreeServiceImpl implements DegreeService {

    private final DegreeRepository repository;

    @Override
    @Transactional
    public DegreeResponse create(DegreeRequest request) {
        log.info("Request received to create Degree with code: {}", request != null ? request.getCode() : null);

        validateRequest(request, false, null);

        // Check for duplicate code
        String normalizedCode = request.getCode().trim();
        if (repository.existsByCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)) {
            log.warn("Degree creation failed: Duplicate degree code '{}'", normalizedCode);
            throw new CustomException(
                    "Degree with code '" + normalizedCode + "' already exists",
                    "DUPLICATE_DEGREE_CODE",
                    "Please provide a unique degree code"
            );
        }

        Degree entity = DegreeMapper.toEntity(request);
        Degree saved = repository.save(entity);

        log.info("Degree created successfully with id: {} and code: {}", saved.getId(), saved.getCode());
        return DegreeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DegreeResponse update(Integer id, DegreeRequest request) {
        log.info("Request received to update Degree with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid degree id provided for update: {}", id);
            throw new CustomException("Invalid degree id provided", "INVALID_ID", "Degree id must be a positive integer");
        }

        Degree existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Degree update failed: Degree not found with id: {}", id);
                    return new ResourceNotFoundException("Degree not found with id: " + id);
                });

        validateRequest(request, true, id);

        // Check for duplicate code if code is changed
        String normalizedCode = request.getCode().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(normalizedCode, id)) {
            log.warn("Degree update failed: Duplicate degree code '{}' already in use by another record", normalizedCode);
            throw new CustomException(
                    "Degree code '" + normalizedCode + "' is already in use",
                    "DUPLICATE_DEGREE_CODE",
                    "Please choose a different degree code"
            );
        }

        DegreeMapper.updateEntity(existing, request);
        Degree updated = repository.save(existing);

        log.info("Degree updated successfully with id: {} and code: {}", updated.getId(), updated.getCode());
        return DegreeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.warn("Request received to soft delete Degree with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid degree id provided for deletion: {}", id);
            throw new CustomException("Invalid degree id provided", "INVALID_ID", "Degree id must be a positive integer");
        }

        Degree existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Degree deletion failed: Degree not found with id: {}", id);
                    return new ResourceNotFoundException("Degree not found with id: " + id);
                });

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("Degree soft-deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DegreeResponse getById(Integer id) {
        log.info("Fetching Degree by id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid degree id provided for fetch: {}", id);
            throw new CustomException("Invalid degree id provided", "INVALID_ID", "Degree id must be a positive integer");
        }

        Degree entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Degree not found with id: {}", id);
                    return new ResourceNotFoundException("Degree not found with id: " + id);
                });

        return DegreeMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeResponse> getAllActive() {
        log.info("Fetching all active degrees");
        List<Degree> activeDegrees = repository.findByIsDeletedFalseAndStatusOrderByCodeAsc("Active");
        return DegreeMapper.toResponseList(activeDegrees);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DegreeResponse> getAll(int page, int size, String search, String degreeType, String status) {
        log.info("Fetching degrees with page: {}, size: {}, search: '{}', degreeType: '{}', status: '{}'",
                page, size, search, degreeType, status);

        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanType = (degreeType != null && !degreeType.isBlank() && !degreeType.equalsIgnoreCase("all")) ? degreeType.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<Degree> pageResult = repository.searchAndFilter(cleanSearch, cleanType, cleanStatus, pageable);
        return pageResult.map(DegreeMapper::toResponse);
    }

    /**
     * Basic manual validation logic to avoid database constraint errors
     */
    private void validateRequest(DegreeRequest request, boolean isUpdate, Integer id) {
        if (request == null) {
            log.error("Degree validation failed: Request body is null");
            throw new CustomException("Request body cannot be null", "VALIDATION_FAILED", "Please provide degree details");
        }

        if (request.getCode() == null || request.getCode().trim().isBlank()) {
            log.warn("Degree validation failed: Degree code is blank");
            throw new CustomException("Degree code is required", "VALIDATION_FAILED", "Degree code cannot be empty");
        }

        if (request.getName() == null || request.getName().trim().isBlank()) {
            log.warn("Degree validation failed: Degree full name is blank");
            throw new CustomException("Degree full name is required", "VALIDATION_FAILED", "Degree name cannot be empty");
        }

        if (request.getDegreeType() == null || request.getDegreeType().trim().isBlank()) {
            log.warn("Degree validation failed: Degree level/type is blank");
            throw new CustomException("Degree level/type is required", "VALIDATION_FAILED", "Please specify a degree level (e.g., UG, PG, Diploma, Doctorate, Integrated)");
        }

        if (request.getDuration() == null || request.getDuration().trim().isBlank()) {
            log.warn("Degree validation failed: Duration is blank");
            throw new CustomException("Standard duration is required", "VALIDATION_FAILED", "Please specify duration (e.g. 4 Years)");
        }

        if (request.getTotalSemesters() == null || request.getTotalSemesters() <= 0) {
            log.warn("Degree validation failed: Invalid total semesters: {}", request.getTotalSemesters());
            throw new CustomException("Total semesters must be greater than 0", "VALIDATION_FAILED", "Total semesters must be a positive number");
        }
    }
}
