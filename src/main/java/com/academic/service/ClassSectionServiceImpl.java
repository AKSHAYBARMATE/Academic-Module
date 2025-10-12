package com.academic.service;

import com.academic.entity.ClassSection;
import com.academic.entity.CommonMaster;
import com.academic.exception.CustomException;
import com.academic.exception.ResourceNotFoundException;
import com.academic.mapper.ClassSectionMapper;
import com.academic.repository.ClassSectionRepository;
import com.academic.repository.CommonMasterRepository;
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
    private final CommonMasterRepository commonMasterRepository; // to resolve names

    /**
     * Create new ClassSection
     */
    @Override
    public ClassSectionResponse create(ClassSectionRequest request) {
        log.info("Creating new ClassSection: {}", request);

        // Validate class and section
        validateClassAndSection(request.getClassId(), request.getSection());

        ClassSection entity = ClassSectionMapper.toEntity(request);
        entity.setIsDeleted(false);

        ClassSection saved = repository.save(entity);
        log.info("ClassSection created successfully with id: {}", saved.getId());

        return toResponseWithNames(saved);
    }

    /**
     * Update existing ClassSection
     */
    @Override
    public ClassSectionResponse update(Long id, ClassSectionRequest request) {
        log.info("Updating ClassSection with id: {}", id);

        ClassSection existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSection not found with id: " + id));

        validateClassAndSection(request.getClassId(), request.getSection());

        ClassSectionMapper.updateEntity(existing, request);

        ClassSection updated = repository.save(existing);
        log.info("ClassSection updated successfully with id: {}", updated.getId());

        return toResponseWithNames(updated);
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

        return toResponseWithNames(entity);
    }

    /**
     * Get all (paged)
     */
    @Override
    public Page<ClassSectionResponse> getAll(int page, int size) {
        log.info("Fetching all active ClassSections. Page: {}, Size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<ClassSection> result = repository.findByIsDeletedFalse(pageable);

        return result.map(this::toResponseWithNames);
    }

    /**
     * Convert entity to response including resolved names
     */
    private ClassSectionResponse toResponseWithNames(ClassSection entity) {
        String className = commonMasterRepository.findByIdAndStatusTrue(entity.getClassId())
                .map(cm -> cm.getData())  // ✅ lambda allows Java to infer type
                .orElse("Unknown Class");

        String sectionName = commonMasterRepository.findByIdAndStatusTrue(entity.getSection())
                .map(cm -> cm.getData())
                .orElse("Unknown Section");

        return ClassSectionResponse.builder()
                .id(entity.getId())
                .classId(entity.getClassId())
                .className(className)
                .sectionId(entity.getSection())
                .sectionName(sectionName)
                .classTeacher(entity.getClassTeacher())
                .students(entity.getStudents())
                .roomNo(entity.getRoomNo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Validate that classId and section exist in common master
     */
    private void validateClassAndSection(Integer classId, Integer sectionId) {
        if (classId == null || !commonMasterRepository.existsByIdAndStatusTrue(classId)) {
            throw new CustomException("Invalid Class", "INVALID_CLASS", "Selected class does not exist or is inactive.");
        }
        if (sectionId == null || !commonMasterRepository.existsByIdAndStatusTrue(sectionId)) {
            throw new CustomException("Invalid Section", "INVALID_SECTION", "Selected section does not exist or is inactive.");
        }
    }
}

