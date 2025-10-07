package com.academic.service;

import com.academic.entity.Subject;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.SubjectMapper;
import com.academic.repository.SubjectRepository;
import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository repository;

    @Override
    public SubjectResponse create(SubjectRequest request) {
        log.info("Creating new Subject: {}", request);

        // Check duplicate subjectCode
        if (repository.existsBySubjectCodeAndIsDeletedFalse(request.getSubjectCode())) {
            log.error("Duplicate subject code found: {}", request.getSubjectCode());
            throw new CustomException(
                    "Duplicate subject code: " + request.getSubjectCode(),
                    "DUPLICATE_SUBJECT_CODE",
                    "Each subject must have a unique subject code"
            );
        }

        Subject entity = SubjectMapper.toEntity(request);
        Subject saved = repository.save(entity);

        log.info("Subject created successfully with id: {}", saved.getId());
        return SubjectMapper.toResponse(saved);
    }

    @Override
    public SubjectResponse update(Long id, SubjectRequest request) {
        log.info("Updating Subject with id: {}", id);

        Subject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        // Check duplicate subjectCode (only if updating subjectCode)
        if (!existing.getSubjectCode().equals(request.getSubjectCode())
                && repository.existsBySubjectCodeAndIsDeletedFalse(request.getSubjectCode())) {
            log.error("Duplicate subject code found while updating: {}", request.getSubjectCode());
            throw new CustomException(
                    "Duplicate subject code: " + request.getSubjectCode(),
                    "DUPLICATE_SUBJECT_CODE",
                    "Each subject must have a unique subject code"
            );
        }

        SubjectMapper.updateEntity(existing, request);
        Subject updated = repository.save(existing);

        log.info("Subject updated successfully with id: {}", updated.getId());
        return SubjectMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Soft deleting Subject with id: {}", id);
        Subject existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("Subject soft deleted successfully with id: {}", id);
    }

    @Override
    public SubjectResponse getById(Long id) {
        log.info("Fetching Subject with id: {}", id);
        Subject entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return SubjectMapper.toResponse(entity);
    }

    @Override
    public Page<SubjectResponse> getAll(int page, int size, String search, String type, String status, Integer credits) {
        log.info("Fetching Subjects. Page: {}, Size: {}, Search: {}, Type: {}, Status: {}, Credits: {}",
                page, size, search, type, status, credits);

        Pageable pageable = PageRequest.of(page, size);

        Page<Subject> result = repository.searchAndFilter(
                (search == null || search.isBlank()) ? null : search,
                (type == null || type.isBlank()) ? null : type,
                (status == null || status.isBlank()) ? null : status,
                credits,
                pageable
        );

        return result.map(SubjectMapper::toResponse);
    }
}
