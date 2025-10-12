package com.academic.service;

import com.academic.entity.AcademicCalendarEvent;
import com.academic.entity.CommonMaster;
import com.academic.exception.CustomException;
import com.academic.mapper.AcademicCalendarEventMapper;
import com.academic.repository.AcademicCalendarEventRepository;
import com.academic.repository.CommonMasterRepository;
import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AcademicCalendarEventServiceImpl implements AcademicCalendarEventService {

    private static final Logger log = LoggerFactory.getLogger(AcademicCalendarEventServiceImpl.class);
    private final AcademicCalendarEventRepository repository;
    private final CommonMasterRepository commonMasterRepository;

    public AcademicCalendarEventServiceImpl(AcademicCalendarEventRepository repository, CommonMasterRepository commonMasterRepository) {
        this.repository = repository;
        this.commonMasterRepository = commonMasterRepository;
    }

    @Override
    public AcademicCalendarEventResponse create(AcademicCalendarEventRequest request) {
        log.info("Creating new academic event: {}", request);

        // Duplicate check
        if (repository.existsByEventNameAndDateAndIsDeletedFalse(request.eventName(), request.date())) {
            throw new CustomException(
                    "Event conflict: " + request.eventName(),
                    "DUPLICATE_EVENT_ON_DATE",
                    "An event with the same name already exists on this date."
            );
        }

        if (request.classIds() == null || request.classIds().isEmpty()) {
            throw new CustomException(
                    "No classes selected",
                    "NO_CLASSES_SELECTED",
                    "Please select at least one class for the academic event."
            );
        }

        // Map request to entity
        AcademicCalendarEvent entity = AcademicCalendarEventMapper.toEntity(request);

        // Save entity
        AcademicCalendarEvent saved = repository.save(entity);

        // Fetch class names
        Map<Long, String> classIdToNameMap = commonMasterRepository.findByIdInAndStatusTrue(
                request.classIds().stream().map(Long::intValue).toList()
        ).stream().collect(Collectors.toMap(cm -> cm.getId().longValue(), CommonMaster::getData));

        // Return response with class names
        return AcademicCalendarEventMapper.toResponse(saved, classIdToNameMap);
    }



    @Override
    public AcademicCalendarEventResponse findById(Long id) {
        log.info("Fetching academic event with id: {}", id);

        // Fetch the event
        AcademicCalendarEvent entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Event not found with id: " + id,
                        "EVENT_NOT_FOUND",
                        "The specified academic event does not exist or has been deleted."
                ));

        // Fetch class names from common master if there are any classes
        Map<Long, String> classIdToNameMap = Map.of();
        if (entity.getClassesInvolved() != null && !entity.getClassesInvolved().isEmpty()) {
            classIdToNameMap = commonMasterRepository.findByIdInAndStatusTrue(
                            entity.getClassesInvolved().stream().map(Long::intValue).toList()
                    )
                    .stream()
                    .collect(Collectors.toMap(cm -> cm.getId().longValue(), CommonMaster::getData));
        }

        // Map to response with both IDs and names
        return AcademicCalendarEventMapper.toResponse(entity, classIdToNameMap);
    }



    @Override
    public AcademicCalendarEventResponse update(Long id, AcademicCalendarEventRequest request) {
        log.info("Updating academic event with id: {}. New data: {}", id, request);

        // Fetch existing event
        AcademicCalendarEvent existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Update failed: Event not found with id: " + id,
                        "EVENT_NOT_FOUND",
                        "Cannot update a non-existent academic event."
                ));

        // Validate classes
        if (request.classIds() == null || request.classIds().isEmpty()) {
            log.error("No classes provided for update of event id: {}", id);
            throw new CustomException(
                    "No classes selected",
                    "NO_CLASSES_SELECTED",
                    "Please select at least one class for the academic event."
            );
        }

        // Update fields
        existing.setEventName(request.eventName());
        existing.setDate(request.date());
        existing.setType(request.type());
        existing.setClassesInvolved(request.classIds());
        existing.setDuration(request.duration());
        existing.setStatus(request.status());

        // Save updated entity
        AcademicCalendarEvent updated = repository.save(existing);

        // Fetch class names from common master
        Map<Long, String> classIdToNameMap = commonMasterRepository.findByIdInAndStatusTrue(
                updated.getClassesInvolved().stream().map(Long::intValue).toList()
        ).stream().collect(Collectors.toMap(cm -> cm.getId().longValue(), CommonMaster::getData));

        // Return response with class names
        return AcademicCalendarEventMapper.toResponse(updated, classIdToNameMap);
    }



    @Override
    public void delete(Long id) {
        log.warn("Attempting to soft delete academic event with id: {}", id);

        AcademicCalendarEvent existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Deletion failed: Event not found with id: " + id,
                        "EVENT_NOT_FOUND",
                        "Cannot delete a non-existent academic event."
                ));

        existing.setDeleted(true);
        repository.save(existing);
        log.info("Academic event soft deleted successfully with id: {}", id);
    }



    @Override
    public Page<AcademicCalendarEventResponse> findAll(
            String search,
            String type,
            String status,
            LocalDate date,
            int page,
            int size) {

        log.info("Fetching academic events with filters - search: {}, type: {}, status: {}, date: {}, page: {}, size: {}",
                search, type, status, date, page, size);

        // Build Specification
        Specification<AcademicCalendarEvent> spec = withFilters(search, type, status, date);

        // Pageable with sorting by date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

        // Fetch paginated result
        Page<AcademicCalendarEvent> eventsPage = repository.findAll(spec, pageable);

        // Collect all unique class IDs from the page
        List<Long> allClassIds = eventsPage.stream()
                .flatMap(event -> event.getClassesInvolved().stream())
                .distinct()
                .collect(Collectors.toList());

        // Fetch class names from common master
        Map<Long, String> classIdToNameMap = commonMasterRepository.findByIdInAndStatusTrue(
                allClassIds.stream().map(Long::intValue).toList()
        ).stream().collect(Collectors.toMap(cm -> cm.getId().longValue(), CommonMaster::getData));

        // Map entities to response DTOs with class names
        return eventsPage.map(event -> AcademicCalendarEventMapper.toResponse(event, classIdToNameMap));
    }



    public static Specification<AcademicCalendarEvent> withFilters(
            String search, String type, String status, LocalDate date) {

        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("isDeleted"), false);

            if (search != null && !search.isEmpty()) {
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("eventName")), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("classesInvolved")), "%" + search.toLowerCase() + "%")
                );
                predicate = cb.and(predicate, searchPredicate);
            }

            if (type != null && !type.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("type")), type.toLowerCase()));
            }

            if (status != null && !status.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }

            if (date != null) {
                predicate = cb.and(predicate, cb.equal(root.get("date"), date));
            }

            return predicate;
        };

    }

}
