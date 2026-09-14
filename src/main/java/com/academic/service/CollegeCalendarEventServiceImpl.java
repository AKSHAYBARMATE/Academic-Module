package com.academic.service;

import com.academic.entity.CollegeCalendarEvent;
import com.academic.exception.ResourceNotFoundException;
import com.academic.repository.CollegeCalendarEventRepository;
import com.academic.request.CollegeCalendarEventRequest;
import com.academic.response.CollegeCalendarEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeCalendarEventServiceImpl implements CollegeCalendarEventService {

    private final CollegeCalendarEventRepository repository;

    @Override
    @Transactional
    public List<CollegeCalendarEventResponse> getAllEvents() {
        List<CollegeCalendarEvent> events = repository.findByIsDeletedFalseOrderByStartDateAscIdAsc();
        if (events.isEmpty()) {
            log.info("No college calendar events found. Initializing standard defaults...");
            events = seedDefaultEvents();
        }
        return events.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CollegeCalendarEventResponse> listEvents(String academicYear, String eventType, String semester, String targetProgram, String status) {
        List<CollegeCalendarEvent> all = repository.findByIsDeletedFalseOrderByStartDateAscIdAsc();
        if (all.isEmpty()) {
            all = seedDefaultEvents();
        }
        List<CollegeCalendarEvent> filtered = repository.filterEvents(academicYear, eventType, semester, targetProgram, status);
        return filtered.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Page<CollegeCalendarEventResponse> searchEvents(int page, int size, String search, String academicYear, String eventType, String semester, String targetProgram, String status) {
        if (repository.count() == 0) {
            seedDefaultEvents();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending().and(Sort.by("id").ascending()));
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<CollegeCalendarEvent> pageResult = repository.searchEvents(cleanSearch, academicYear, eventType, semester, targetProgram, status, pageable);
        return pageResult.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeCalendarEventResponse getEventById(Long id) {
        CollegeCalendarEvent event = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College calendar event not found with id: " + id));
        return toResponse(event);
    }

    @Override
    @Transactional
    public CollegeCalendarEventResponse createEvent(CollegeCalendarEventRequest request) {
        log.info("Creating college calendar event: title={}, type={}, startDate={}",
                request.getTitle(), request.getEventType(), request.getEffectiveStartDate());

        String startDate = request.getEffectiveStartDate();
        if (startDate.isBlank()) {
            throw new IllegalArgumentException("Start date is required for calendar event");
        }

        CollegeCalendarEvent event = CollegeCalendarEvent.builder()
                .title(request.getTitle().trim())
                .eventType(request.getEventType() != null ? request.getEventType().trim() : "Academic")
                .academicYear(request.getAcademicYear() != null && !request.getAcademicYear().isBlank() ? request.getAcademicYear().trim() : "2025-2026")
                .semester(request.getSemester() != null && !request.getSemester().isBlank() ? request.getSemester().trim() : "All Semesters")
                .startDate(startDate)
                .endDate(request.getEndDate() != null && !request.getEndDate().isBlank() ? request.getEndDate().trim() : null)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Scheduled")
                .targetProgram(request.getTargetProgram() != null && !request.getTargetProgram().isBlank() ? request.getTargetProgram().trim() : "All Programs")
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .isDeleted(false)
                .build();

        CollegeCalendarEvent saved = repository.save(event);
        log.info("College calendar event created successfully with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollegeCalendarEventResponse updateEvent(Long id, CollegeCalendarEventRequest request) {
        log.info("Updating college calendar event id: {}", id);

        CollegeCalendarEvent existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College calendar event not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            existing.setTitle(request.getTitle().trim());
        }
        if (request.getEventType() != null && !request.getEventType().isBlank()) {
            existing.setEventType(request.getEventType().trim());
        }
        if (request.getAcademicYear() != null && !request.getAcademicYear().isBlank()) {
            existing.setAcademicYear(request.getAcademicYear().trim());
        }
        if (request.getSemester() != null && !request.getSemester().isBlank()) {
            existing.setSemester(request.getSemester().trim());
        }
        String effectiveStart = request.getEffectiveStartDate();
        if (!effectiveStart.isBlank()) {
            existing.setStartDate(effectiveStart);
        }
        if (request.getEndDate() != null) {
            existing.setEndDate(request.getEndDate().isBlank() ? null : request.getEndDate().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            existing.setStatus(request.getStatus().trim());
        }
        if (request.getTargetProgram() != null && !request.getTargetProgram().isBlank()) {
            existing.setTargetProgram(request.getTargetProgram().trim());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription().trim());
        }

        CollegeCalendarEvent updated = repository.save(existing);
        log.info("College calendar event updated successfully with id: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Soft-deleting college calendar event id: {}", id);
        CollegeCalendarEvent existing = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College calendar event not found with id: " + id));
        existing.setIsDeleted(true);
        repository.save(existing);
        log.info("College calendar event soft-deleted with id: {}", id);
    }

    @Override
    @Transactional
    public List<CollegeCalendarEventResponse> bulkCreateEvents(List<CollegeCalendarEventRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<CollegeCalendarEventResponse> result = new ArrayList<>();
        for (CollegeCalendarEventRequest req : requests) {
            result.add(createEvent(req));
        }
        return result;
    }

    private List<CollegeCalendarEvent> seedDefaultEvents() {
        List<CollegeCalendarEvent> defaults = List.of(
                CollegeCalendarEvent.builder()
                        .title("Orientation & Induction Program (Batch 2025)")
                        .eventType("Academic")
                        .academicYear("2025-2026")
                        .semester("Semester 1")
                        .startDate("2025-09-10")
                        .endDate("2025-09-12")
                        .status("Completed")
                        .targetProgram("All Freshers")
                        .description("Welcome orientation and briefing for new students")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("Semester Classes Commencement")
                        .eventType("Academic")
                        .academicYear("2025-2026")
                        .semester("All Semesters")
                        .startDate("2025-09-15")
                        .endDate(null)
                        .status("Active")
                        .targetProgram("All Programs")
                        .description("Formal start of odd semester classroom teaching")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("Mid-Term Assessment Examination 1")
                        .eventType("Examination")
                        .academicYear("2025-2026")
                        .semester("Semester 3")
                        .startDate("2025-10-20")
                        .endDate("2025-10-25")
                        .status("Scheduled")
                        .targetProgram("B.Tech (CSE)")
                        .description("Internal assessment examination (Units 1 & 2)")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("Diwali & Autumn Mid-Semester Break")
                        .eventType("Holiday")
                        .academicYear("2025-2026")
                        .semester("All Semesters")
                        .startDate("2025-10-29")
                        .endDate("2025-11-03")
                        .status("Scheduled")
                        .targetProgram("All Programs")
                        .description("College closed for festive autumn holidays")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("Major Project Synopsis & Literature Submission")
                        .eventType("Submission")
                        .academicYear("2025-2026")
                        .semester("Semester 7")
                        .startDate("2025-11-10")
                        .endDate(null)
                        .status("Scheduled")
                        .targetProgram("B.Tech (CSE)")
                        .description("Final year capstone project phase-1 submission")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("National Annual Technical Symposium & Hackathon")
                        .eventType("Activity")
                        .academicYear("2025-2026")
                        .semester("All Semesters")
                        .startDate("2025-11-21")
                        .endDate("2025-11-22")
                        .status("Scheduled")
                        .targetProgram("All Programs")
                        .description("24-hour coding hackathon & robotics arena")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("Practical Laboratory & Viva-Voce Examinations")
                        .eventType("Examination")
                        .academicYear("2025-2026")
                        .semester("All Semesters")
                        .startDate("2025-12-05")
                        .endDate("2025-12-10")
                        .status("Scheduled")
                        .targetProgram("All Programs")
                        .description("External evaluation for laboratory courses")
                        .isDeleted(false)
                        .build(),
                CollegeCalendarEvent.builder()
                        .title("End-Term University Theory Examinations")
                        .eventType("Examination")
                        .academicYear("2025-2026")
                        .semester("All Semesters")
                        .startDate("2025-12-15")
                        .endDate("2025-12-30")
                        .status("Scheduled")
                        .targetProgram("All Programs")
                        .description("Final university theory board examinations")
                        .isDeleted(false)
                        .build()
        );
        return repository.saveAll(defaults);
    }

    private CollegeCalendarEventResponse toResponse(CollegeCalendarEvent e) {
        return CollegeCalendarEventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .eventType(e.getEventType())
                .academicYear(e.getAcademicYear())
                .semester(e.getSemester())
                .startDate(e.getStartDate())
                .date(e.getStartDate())
                .endDate(e.getEndDate())
                .status(e.getStatus())
                .targetProgram(e.getTargetProgram())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
