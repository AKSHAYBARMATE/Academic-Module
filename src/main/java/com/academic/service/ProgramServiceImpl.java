package com.academic.service;

import com.academic.entity.Program;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.ProgramMapper;
import com.academic.repository.ProgramRepository;
import com.academic.request.ProgramRequest;
import com.academic.response.ProgramResponse;
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
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository repository;

    @Override
    @Transactional
    public ProgramResponse create(ProgramRequest request) {
        log.info("Request received to create Program with code: {}", request != null ? request.getCode() : null);

        validateRequest(request, false, null);

        // Check for duplicate program code
        String normalizedCode = request.getCode().trim();
        if (repository.existsByCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)) {
            log.warn("Program creation failed: Duplicate program code '{}'", normalizedCode);
            throw new CustomException(
                    "Program with code '" + normalizedCode + "' already exists",
                    "DUPLICATE_PROGRAM_CODE",
                    "Please provide a unique program code"
            );
        }

        Program entity = ProgramMapper.toEntity(request);
        Program saved = repository.save(entity);

        log.info("Program created successfully with id: {} and code: {}", saved.getId(), saved.getCode());
        return ProgramMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProgramResponse update(Integer id, ProgramRequest request) {
        log.info("Request received to update Program with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid program id provided for update: {}", id);
            throw new CustomException("Invalid program id provided", "INVALID_ID", "Program id must be a positive integer");
        }

        Program existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Program update failed: Program not found with id: {}", id);
                    return new ResourceNotFoundException("Program not found with id: " + id);
                });

        validateRequest(request, true, id);

        // Check for duplicate code if code is changed
        String normalizedCode = request.getCode().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(normalizedCode, id)) {
            log.warn("Program update failed: Duplicate program code '{}' already in use by another record", normalizedCode);
            throw new CustomException(
                    "Program code '" + normalizedCode + "' is already in use",
                    "DUPLICATE_PROGRAM_CODE",
                    "Please choose a different program code"
            );
        }

        ProgramMapper.updateEntity(existing, request);
        Program updated = repository.save(existing);

        log.info("Program updated successfully with id: {} and code: {}", updated.getId(), updated.getCode());
        return ProgramMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.warn("Request received to soft delete Program with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid program id provided for deletion: {}", id);
            throw new CustomException("Invalid program id provided", "INVALID_ID", "Program id must be a positive integer");
        }

        Program existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Program deletion failed: Program not found with id: {}", id);
                    return new ResourceNotFoundException("Program not found with id: " + id);
                });

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("Program soft-deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramResponse getById(Integer id) {
        log.info("Fetching Program by id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid program id provided", "INVALID_ID", "Program id must be a positive integer");
        }

        Program entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Program not found with id: {}", id);
                    return new ResourceNotFoundException("Program not found with id: " + id);
                });

        return ProgramMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramResponse> getAllActive() {
        log.info("Fetching all active Programs");
        List<Program> list = repository.findByIsDeletedFalseAndStatusOrderByCodeAsc("Active");
        return ProgramMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgramResponse> getByDegreeCode(String degreeCode) {
        log.info("Fetching Programs by degreeCode: {}", degreeCode);
        if (degreeCode == null || degreeCode.isBlank()) {
            throw new CustomException("Degree code is required", "REQUIRED_FIELD", "degreeCode cannot be empty");
        }
        List<Program> list = repository.findByDegreeCodeIgnoreCaseAndIsDeletedFalseOrderByCodeAsc(degreeCode.trim());
        return ProgramMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgramResponse> getAll(int page, int size, String search, String degreeCode, String degreeType, String status) {
        log.info("Fetching paginated Programs - page: {}, size: {}, search: {}, degreeCode: {}, degreeType: {}, status: {}",
                page, size, search, degreeCode, degreeType, status);

        if (page < 0) page = 0;
        if (size <= 0 || size > 200) size = 10;

        Pageable pageable = PageRequest.of(page, size);

        String cleanSearch = (search != null && !search.trim().isBlank()) ? search.trim() : null;
        String cleanDegreeCode = (degreeCode != null && !degreeCode.trim().isBlank() && !degreeCode.equalsIgnoreCase("all")) ? degreeCode.trim() : null;
        String cleanDegreeType = (degreeType != null && !degreeType.trim().isBlank() && !degreeType.equalsIgnoreCase("all")) ? degreeType.trim() : null;
        String cleanStatus = (status != null && !status.trim().isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<Program> pageResult = repository.searchAndFilter(cleanSearch, cleanDegreeCode, cleanDegreeType, cleanStatus, pageable);
        return pageResult.map(ProgramMapper::toResponse);
    }

    /**
     * Manual validation logic in accordance with guidelines (no @NotNull or unique in entities)
     */
    private void validateRequest(ProgramRequest request, boolean isUpdate, Integer existingId) {
        if (request == null) {
            log.warn("Validation failed: ProgramRequest body is null");
            throw new CustomException("Request body cannot be null", "INVALID_REQUEST", "Please provide a valid program payload");
        }

        if (request.getCode() == null || request.getCode().trim().isBlank()) {
            log.warn("Validation failed: Program code is missing");
            throw new CustomException("Program code is required", "REQUIRED_FIELD", "Field 'code' cannot be empty");
        }

        if (request.getName() == null || request.getName().trim().isBlank()) {
            log.warn("Validation failed: Program name is missing");
            throw new CustomException("Program name is required", "REQUIRED_FIELD", "Field 'name' cannot be empty");
        }

        if (request.getDegreeCode() == null || request.getDegreeCode().trim().isBlank()) {
            log.warn("Validation failed: Parent degree code is missing");
            throw new CustomException("Parent degree code is required", "REQUIRED_FIELD", "Field 'degreeCode' cannot be empty");
        }

        if (request.getIntake() != null && request.getIntake() < 0) {
            log.warn("Validation failed: Invalid intake count {}", request.getIntake());
            throw new CustomException("Intake seats must be zero or positive", "INVALID_INTAKE", "Field 'intake' must be >= 0");
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String status = request.getStatus().trim();
            if (!status.equalsIgnoreCase("Active") && !status.equalsIgnoreCase("Inactive")) {
                log.warn("Validation failed: Invalid status value '{}'", status);
                throw new CustomException("Status must be either 'Active' or 'Inactive'", "INVALID_STATUS", "Invalid value for status");
            }
        }
    }
}
