package com.academic.service;

import com.academic.entity.ClassSection;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.ClassSectionMapper;
import com.academic.repository.ClassSectionRepository;
import com.academic.request.ClassSectionRequest;
import com.academic.response.ClassSectionResponse;
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
public class ClassSectionServiceImpl implements ClassSectionService {

    private final ClassSectionRepository repository;

    /**
     * Create new ClassSection
     */
    @Override
    public ClassSectionResponse create(ClassSectionRequest request) {
        log.info("Creating new ClassSection: {}", request);

        ClassSection entity = ClassSectionMapper.toEntity(request);
        entity.setIsDeleted(false);

        ClassSection saved = repository.save(entity);
        log.info("ClassSection created successfully with id: {}", saved.getId());

        return ClassSectionMapper.toResponse(saved);
    }

    /**
     * Update existing ClassSection
     */
    @Override
    public ClassSectionResponse update(Long id, ClassSectionRequest request) {
        log.info("Updating ClassSection with id: {}", id);

        ClassSection existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection not found with id: " + id));

        ClassSectionMapper.updateEntity(existing, request);

        ClassSection updated = repository.save(existing);
        log.info("ClassSection updated successfully with id: {}", updated.getId());

        return ClassSectionMapper.toResponse(updated);
    }

    /**
     * Soft delete
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Soft deleting ClassSection with id: {}", id);

        ClassSection existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection not found with id: " + id));

        existing.setIsDeleted(true);
        repository.save(existing);

        log.info("ClassSection soft deleted successfully with id: {}", id);
    }

    /**
     * Get by ID
     */
    @Override
    public ClassSectionResponse getById(Long id) {
        log.info("Fetching ClassSection with id: {}", id);

        ClassSection entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection not found with id: " + id));

        return ClassSectionMapper.toResponse(entity);
    }

    /**
     * Get all (paged) - only non-deleted records
     */
    @Override
    public Page<ClassSectionResponse> getAll(int page, int size) {
        log.info("Fetching all active ClassSections. Page: {}, Size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<ClassSection> result = repository.findByIsDeletedFalse(pageable);

        return result.map(ClassSectionMapper::toResponse);
    }

}

