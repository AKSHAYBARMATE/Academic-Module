package com.academic.service;

import com.academic.entity.CollegeSubject;
import com.academic.entity.Degree;
import com.academic.entity.Department;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.CollegeSubjectMapper;
import com.academic.repository.CollegeSubjectRepository;
import com.academic.repository.DegreeRepository;
import com.academic.repository.DepartmentRepository;
import com.academic.request.CollegeSubjectRequest;
import com.academic.response.CollegeSubjectResponse;
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
public class CollegeSubjectServiceImpl implements CollegeSubjectService {

    private final CollegeSubjectRepository repository;
    private final DepartmentRepository departmentRepository;
    private final DegreeRepository degreeRepository;

    @Override
    @Transactional
    public CollegeSubjectResponse create(CollegeSubjectRequest request) {
        log.info("Request received to create CollegeSubject with code: {}",
                request != null ? request.getResolvedCode() : null);

        validateRequest(request, false, null);

        String normalizedCode = request.getResolvedCode().trim();
        if (repository.existsBySubjectCodeIgnoreCaseAndIsDeletedFalse(normalizedCode)) {
            log.warn("Subject creation failed: Subject code '{}' already exists", normalizedCode);
            throw new CustomException(
                    "Subject with code '" + normalizedCode + "' already exists",
                    "DUPLICATE_SUBJECT_CODE",
                    "Subject code must be unique"
            );
        }

        Department department = resolveDepartment(request);
        Degree degree = resolveDegree(request);

        CollegeSubject entity = CollegeSubjectMapper.toEntity(request, degree, department);
        CollegeSubject saved = repository.save(entity);

        log.info("CollegeSubject created successfully with id: {} and code: {}", saved.getId(), saved.getSubjectCode());
        return CollegeSubjectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CollegeSubjectResponse update(Long id, CollegeSubjectRequest request) {
        log.info("Request received to update CollegeSubject with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid subject id provided for update: {}", id);
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive number");
        }

        CollegeSubject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("CollegeSubject update failed: Subject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        validateRequest(request, true, id);

        String normalizedCode = request.getResolvedCode().trim();
        if (repository.existsBySubjectCodeIgnoreCaseAndIdNotAndIsDeletedFalse(normalizedCode, id)) {
            log.warn("CollegeSubject update failed: Duplicate subject code '{}' already in use", normalizedCode);
            throw new CustomException(
                    "Subject code '" + normalizedCode + "' is already in use by another course",
                    "DUPLICATE_SUBJECT_CODE",
                    "Please choose a different subject code"
            );
        }

        Department department = resolveDepartment(request);
        Degree degree = resolveDegree(request);

        CollegeSubjectMapper.updateEntity(existing, request, degree, department);
        CollegeSubject updated = repository.save(existing);

        log.info("CollegeSubject updated successfully with id: {} and code: {}", updated.getId(), updated.getSubjectCode());
        return CollegeSubjectMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Request received to soft delete CollegeSubject with id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid subject id provided for deletion: {}", id);
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive number");
        }

        CollegeSubject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("CollegeSubject deletion failed: Subject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("CollegeSubject soft-deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeSubjectResponse getById(Long id) {
        log.info("Fetching CollegeSubject by id: {}", id);

        if (id == null || id <= 0) {
            throw new CustomException("Invalid subject id provided", "INVALID_ID", "Subject id must be a positive number");
        }

        CollegeSubject entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("CollegeSubject not found with id: {}", id);
                    return new ResourceNotFoundException("Subject not found with id: " + id);
                });

        return CollegeSubjectMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeSubjectResponse> getAllActive() {
        log.info("Fetching all active CollegeSubjects");
        List<CollegeSubject> list = repository.findByIsDeletedFalseAndStatusOrderBySubjectCodeAsc("Active");
        return CollegeSubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeSubjectResponse> getByProgram(String program) {
        log.info("Fetching CollegeSubjects by program: {}", program);
        if (program == null || program.isBlank()) {
            throw new CustomException("Program is required", "REQUIRED_FIELD", "program cannot be empty");
        }
        List<CollegeSubject> list = repository.findByProgramIgnoreCaseAndIsDeletedFalseOrderBySubjectCodeAsc(program.trim());
        return CollegeSubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeSubjectResponse> getByDepartment(Integer departmentId) {
        log.info("Fetching CollegeSubjects by departmentId: {}", departmentId);
        if (departmentId == null || departmentId <= 0) {
            throw new CustomException("Department ID is required", "REQUIRED_FIELD", "departmentId must be positive");
        }
        List<CollegeSubject> list = repository.findByDepartment_IdAndIsDeletedFalseOrderBySubjectCodeAsc(departmentId);
        return CollegeSubjectMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollegeSubjectResponse> getAll(
            int page,
            int size,
            String search,
            Integer departmentId,
            Integer degreeId,
            String department,
            String program,
            String semester,
            String academicYear,
            String type,
            String status,
            Integer credits
    ) {
        log.info("Fetching paginated CollegeSubjects - page: {}, size: {}, search: {}, deptId: {}, degId: {}, program: {}, sem: {}, type: {}, status: {}",
                page, size, search, departmentId, degreeId, program, semester, type, status);

        if (page < 0) page = 0;
        if (size <= 0 || size > 200) size = 10;

        Pageable pageable = PageRequest.of(page, size);

        String cleanSearch = (search != null && !search.trim().isBlank()) ? search.trim() : null;
        String cleanDept = (department != null && !department.trim().isBlank() && !department.equalsIgnoreCase("all")) ? department.trim() : null;
        String cleanProgram = (program != null && !program.trim().isBlank() && !program.equalsIgnoreCase("all")) ? program.trim() : null;
        String cleanSem = (semester != null && !semester.trim().isBlank() && !semester.equalsIgnoreCase("all")) ? semester.trim() : null;
        String cleanYear = (academicYear != null && !academicYear.trim().isBlank() && !academicYear.equalsIgnoreCase("all")) ? academicYear.trim() : null;
        String cleanType = (type != null && !type.trim().isBlank() && !type.equalsIgnoreCase("all")) ? type.trim() : null;
        String cleanStatus = (status != null && !status.trim().isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<CollegeSubject> pageResult = repository.searchAndFilter(
                cleanSearch, departmentId, degreeId, cleanDept, cleanProgram, cleanSem, cleanYear, cleanType, cleanStatus, credits, pageable
        );
        return pageResult.map(CollegeSubjectMapper::toResponse);
    }

    private Department resolveDepartment(CollegeSubjectRequest request) {
        if (request.getDepartmentId() != null && request.getDepartmentId() > 0) {
            return departmentRepository.findByIdAndIsDeleteFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
        }
        if (request.getDepartment() != null && !request.getDepartment().trim().isBlank() && !request.getDepartment().equalsIgnoreCase("all")) {
            return departmentRepository.findByNameIgnoreCase(request.getDepartment().trim())
                    .orElse(null);
        }
        return null;
    }

    private Degree resolveDegree(CollegeSubjectRequest request) {
        if (request.getDegreeId() != null && request.getDegreeId() > 0) {
            return degreeRepository.findByIdAndIsDeletedFalse(request.getDegreeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Degree not found with id: " + request.getDegreeId()));
        }
        if (request.getDegree() != null && !request.getDegree().trim().isBlank() && !request.getDegree().equalsIgnoreCase("all")) {
            return degreeRepository.findByCodeIgnoreCaseAndIsDeletedFalse(request.getDegree().trim())
                    .orElse(null);
        }
        return null;
    }

    private void validateRequest(CollegeSubjectRequest request, boolean isUpdate, Long existingId) {
        if (request == null) {
            log.warn("Validation failed: CollegeSubjectRequest body is null");
            throw new CustomException("Request body cannot be null", "INVALID_REQUEST", "Please provide a valid subject payload");
        }

        String resolvedCode = request.getResolvedCode();
        if (resolvedCode == null || resolvedCode.isBlank()) {
            log.warn("Validation failed: Subject code is missing");
            throw new CustomException("Subject code is required", "REQUIRED_FIELD", "Field 'code' / 'subjectCode' cannot be empty");
        }

        String resolvedName = request.getResolvedName();
        if (resolvedName == null || resolvedName.isBlank()) {
            log.warn("Validation failed: Subject name is missing");
            throw new CustomException("Subject name is required", "REQUIRED_FIELD", "Field 'name' / 'subjectName' cannot be empty");
        }

        if (request.getHrsPerWeek() != null && request.getHrsPerWeek() < 0) {
            log.warn("Validation failed: Invalid hrsPerWeek {}", request.getHrsPerWeek());
            throw new CustomException("Teaching hours per week must be zero or positive", "INVALID_HOURS", "Field 'hrsPerWeek' must be >= 0");
        }

        if (request.getCredits() != null && request.getCredits() < 0) {
            log.warn("Validation failed: Invalid credits {}", request.getCredits());
            throw new CustomException("Credits must be zero or positive", "INVALID_CREDITS", "Field 'credits' must be >= 0");
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String status = request.getStatus().trim();
            if (!status.equalsIgnoreCase("Active") && !status.equalsIgnoreCase("Inactive") && !status.equalsIgnoreCase("Draft")) {
                log.warn("Validation failed: Invalid status value '{}'", status);
                throw new CustomException("Status must be 'Active', 'Inactive', or 'Draft'", "INVALID_STATUS", "Invalid value for status");
            }
        }
    }
}
