package com.academic.service;

import com.academic.entity.AcademicCalendarEvent;
import com.academic.exception.CustomException;
import com.academic.mapper.AcademicCalendarEventMapper;
import com.academic.repository.AcademicCalendarEventRepository;
import com.academic.request.AcademicCalendarEventRequest;
import com.academic.response.AcademicCalendarEventResponse;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicCalendarEventServiceImpl implements AcademicCalendarEventService {

    private static final Logger log = LoggerFactory.getLogger(AcademicCalendarEventServiceImpl.class);
    private final AcademicCalendarEventRepository repository;

    public AcademicCalendarEventServiceImpl(AcademicCalendarEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AcademicCalendarEventResponse create(AcademicCalendarEventRequest request) {
        log.info("Creating new academic event: {}", request);

        if (repository.existsByEventNameAndDateAndIsDeletedFalse(request.eventName(), request.date())) {
            log.error("Duplicate event found: {} on {}", request.eventName(), request.date());
            throw new CustomException(
                    "Event conflict: " + request.eventName(),
                    "DUPLICATE_EVENT_ON_DATE",
                    "An event with the same name already exists on this date."
            );
        }

        AcademicCalendarEvent entity = AcademicCalendarEventMapper.toEntity(request);
        AcademicCalendarEvent saved = repository.save(entity);

        log.info("Academic event created successfully with id: {}", saved.getId());
        return AcademicCalendarEventMapper.toResponse(saved);
    }


    @Override
    public AcademicCalendarEventResponse findById(Long id) {
        log.info("Fetching academic event with id: {}", id);
        AcademicCalendarEvent entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Event not found with id: " + id,
                        "EVENT_NOT_FOUND",
                        "The specified academic event does not exist or has been deleted."
                ));
        return AcademicCalendarEventMapper.toResponse(entity);
    }

    @Override
    public AcademicCalendarEventResponse update(Long id, AcademicCalendarEventRequest request) {
        log.info("Updating academic event with id: {}. New data: {}", id, request);

        AcademicCalendarEvent existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Update failed: Event not found with id: " + id,
                        "EVENT_NOT_FOUND",
                        "Cannot update a non-existent academic event."
                ));

        existing.setEventName(request.eventName());
        existing.setDate(request.date());
        existing.setType(request.type());
        existing.setClassesInvolved(request.classesInvolved());
        existing.setDuration(request.duration());
        existing.setStatus(request.status());

        AcademicCalendarEvent updated = repository.save(existing);
        log.info("Academic event updated successfully with id: {}", updated.getId());
        return AcademicCalendarEventMapper.toResponse(updated);
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
    public List<AcademicCalendarEventResponse> findAll(
            String search, String type, String status, LocalDate date) {

        log.info("Fetching academic events with filters - search: {}, type: {}, status: {}, date: {}",
                search, type, status, date);

        // Build the Specification
        Specification<AcademicCalendarEvent> spec = withFilters(search, type, status, date);

        // Optional sorting: by date descending
        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        // Fetch from repository
        List<AcademicCalendarEvent> events = repository.findAll(spec, sort);

        // Map to Response DTO
        return events.stream()
                .map(AcademicCalendarEventMapper::toResponse)
                .collect(Collectors.toList());
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
