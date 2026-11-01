package com.academic.service;

import com.academic.config.LoginUser;
import com.academic.entity.ClassEvent;
import com.academic.repository.ClassEventRepository;
import com.academic.repository.CommonMasterRepository;
import com.academic.request.ClassEventRequest;
import com.academic.response.ClassEventCheckResponse;
import com.academic.response.ClassEventResponse;
import com.academic.response.StandardResponse;
import com.academic.utility.IstClock;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassEventServiceImpl implements ClassEventService {

    private final ClassEventRepository classEventRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final LoginUser loginUser;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Override
    public StandardResponse<ClassEventResponse> createEvent(ClassEventRequest request) {
        log.info("Creating new class event: {}", request.getEventTitle());
        validateEventRequest(request);

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        List<Long> classIds = request.getClassIds() != null ? request.getClassIds() : new ArrayList<>();
        List<Long> sectionIds = request.getSectionIds() != null ? request.getSectionIds() : new ArrayList<>();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : request.getEventDate();

        Long currentUserId = resolveCurrentUserId();

        ClassEvent event = ClassEvent.builder()
                .eventType(request.getEventType().trim())
                .eventTitle(request.getEventTitle().trim())
                .eventDate(request.getEventDate())
                .endDate(endDate)
                .applicableTo(applicableTo)
                .classIds(classIds)
                .sectionIds(sectionIds)
                .description(request.getDescription())
                .academicSession(request.getAcademicSession())
                .sessionId(request.getSessionId())
                .build();

        // Maintain WHO columns from BaseEntity
        event.setCreatedBy(currentUserId);
        event.setModifiedBy(currentUserId);
        event.setCreatedOn(IstClock.nowDateTime());
        event.setModifiedAt(IstClock.nowDateTime());
        event.setIsDeleted(false);

        ClassEvent saved = classEventRepository.save(event);
        log.info("Class event created successfully with ID: {}", saved.getId());

        return StandardResponse.success(mapToResponse(saved), "Class event created successfully");
    }

    @Override
    public StandardResponse<ClassEventResponse> updateEvent(Long id, ClassEventRequest request) {
        log.info("Updating class event with ID: {}", id);
        validateEventRequest(request);

        ClassEvent event = classEventRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Class event not found with ID: " + id));

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        List<Long> classIds = request.getClassIds() != null ? request.getClassIds() : new ArrayList<>();
        List<Long> sectionIds = request.getSectionIds() != null ? request.getSectionIds() : new ArrayList<>();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : request.getEventDate();

        Long currentUserId = resolveCurrentUserId();

        event.setEventType(request.getEventType().trim());
        event.setEventTitle(request.getEventTitle().trim());
        event.setEventDate(request.getEventDate());
        event.setEndDate(endDate);
        event.setApplicableTo(applicableTo);
        event.setClassIds(classIds);
        event.setSectionIds(sectionIds);
        event.setDescription(request.getDescription());
        event.setAcademicSession(request.getAcademicSession());
        event.setSessionId(request.getSessionId());

        // Maintain WHO columns from BaseEntity
        event.setModifiedBy(currentUserId);
        event.setModifiedAt(IstClock.nowDateTime());

        ClassEvent updated = classEventRepository.save(event);
        log.info("Class event updated successfully with ID: {}", updated.getId());

        return StandardResponse.success(mapToResponse(updated), "Class event updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ClassEventResponse> getEventById(Long id) {
        ClassEvent event = classEventRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Class event not found with ID: " + id));

        return StandardResponse.success(mapToResponse(event), "Class event fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<ClassEventResponse>> getAllEvents(
            String search,
            String eventType,
            String applicableTo,
            Long classId,
            Long sectionId,
            Integer year,
            Integer month,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventDate"));

        Specification<ClassEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("eventTitle")), pattern),
                        cb.like(cb.lower(root.get("eventType")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("eventType")), eventType.trim().toLowerCase()));
            }

            if (applicableTo != null && !applicableTo.isBlank()) {
                String norm = normalizeApplicableTo(applicableTo);
                predicates.add(cb.equal(root.get("applicableTo"), norm));
            }

            if (year != null && year > 0) {
                if (month != null && month >= 1 && month <= 12) {
                    LocalDate startOfMonth = LocalDate.of(year, month, 1);
                    LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
                    predicates.add(cb.and(
                            cb.lessThanOrEqualTo(root.get("eventDate"), endOfMonth),
                            cb.greaterThanOrEqualTo(cb.coalesce(root.get("endDate"), root.get("eventDate")), startOfMonth)
                    ));
                } else {
                    LocalDate startOfYear = LocalDate.of(year, 1, 1);
                    LocalDate endOfYear = LocalDate.of(year, 12, 31);
                    predicates.add(cb.and(
                            cb.lessThanOrEqualTo(root.get("eventDate"), endOfYear),
                            cb.greaterThanOrEqualTo(cb.coalesce(root.get("endDate"), root.get("eventDate")), startOfYear)
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ClassEvent> pageResult = classEventRepository.findAll(spec, pageable);

        // Batch pre-load master names for classes & sections
        Set<Long> masterIds = new HashSet<>();
        pageResult.forEach(e -> {
            if (e.getClassIds() != null) masterIds.addAll(e.getClassIds());
            if (e.getSectionIds() != null) masterIds.addAll(e.getSectionIds());
        });

        Map<Long, String> masterMap = fetchMasterNames(masterIds);

        List<ClassEventResponse> list = pageResult.getContent().stream()
                .filter(e -> matchesClassAndSection(e, classId, sectionId))
                .map(e -> mapToResponseWithMap(e, masterMap))
                .collect(Collectors.toList());

        Page<ClassEventResponse> responsePage = new PageImpl<>(list, pageable, pageResult.getTotalElements());
        return StandardResponse.success(responsePage, "Class events fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ClassEventResponse>> getEventsList(
            Integer year,
            Integer month,
            Long classId,
            Long sectionId,
            String academicSession
    ) {
        List<ClassEvent> events;

        if (year != null && month != null && month >= 1 && month <= 12) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            events = classEventRepository.findEventsForMonth(year, month, start, end);
        } else {
            events = classEventRepository.findByIsDeletedFalseOrderByEventDateAsc();
        }

        Set<Long> masterIds = new HashSet<>();
        events.forEach(e -> {
            if (e.getClassIds() != null) masterIds.addAll(e.getClassIds());
            if (e.getSectionIds() != null) masterIds.addAll(e.getSectionIds());
        });
        Map<Long, String> masterMap = fetchMasterNames(masterIds);

        List<ClassEventResponse> list = events.stream()
                .filter(e -> {
                    if (academicSession != null && !academicSession.isBlank()) {
                        if (e.getAcademicSession() != null && !e.getAcademicSession().equalsIgnoreCase(academicSession.trim())) {
                            return false;
                        }
                    }
                    return matchesClassAndSection(e, classId, sectionId);
                })
                .map(e -> mapToResponseWithMap(e, masterMap))
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Class events list fetched successfully");
    }

    @Override
    public StandardResponse<Map<String, Object>> deleteEvent(Long id) {
        log.info("Soft-deleting class event with ID: {}", id);
        ClassEvent event = classEventRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Class event not found with ID: " + id));

        event.setIsDeleted(true);
        event.setModifiedBy(resolveCurrentUserId());
        event.setModifiedAt(IstClock.nowDateTime());
        classEventRepository.save(event);

        return StandardResponse.success(null, "Class event deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ClassEventCheckResponse> checkIsEvent(LocalDate date, Long classId, Long sectionId) {
        if (date == null) {
            date = IstClock.today();
        }

        String classIdStr = classId != null ? String.valueOf(classId) : null;
        String sectionIdStr = sectionId != null ? String.valueOf(sectionId) : null;

        Optional<ClassEvent> eventOpt = classEventRepository.findEventForDateAndClassSection(
                date, classId, classIdStr, sectionId, sectionIdStr
        );

        if (eventOpt.isPresent()) {
            ClassEvent e = eventOpt.get();
            ClassEventCheckResponse res = ClassEventCheckResponse.builder()
                    .date(date)
                    .isEvent(true)
                    .eventTag("E")
                    .eventId(e.getId())
                    .eventTitle(e.getEventTitle())
                    .eventType(e.getEventType())
                    .applicableTo(e.getApplicableTo())
                    .description(e.getDescription())
                    .build();
            return StandardResponse.success(res, "Event on this date: " + e.getEventTitle());
        }

        ClassEventCheckResponse res = ClassEventCheckResponse.builder()
                .date(date)
                .isEvent(false)
                .build();
        return StandardResponse.success(res, "No event on this date");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Map<String, ClassEventCheckResponse>> getMonthlyEventDates(
            int year, int month, Long classId, Long sectionId
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<ClassEvent> monthEvents = classEventRepository.findEventsForMonth(year, month, start, end);

        Map<String, ClassEventCheckResponse> dateMap = new LinkedHashMap<>();

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate current = ym.atDay(day);

            for (ClassEvent e : monthEvents) {
                LocalDate eStart = e.getEventDate();
                LocalDate eEnd = e.getEndDate() != null ? e.getEndDate() : e.getEventDate();

                if (!current.isBefore(eStart) && !current.isAfter(eEnd)) {
                    if (matchesClassAndSection(e, classId, sectionId)) {
                        dateMap.put(current.toString(), ClassEventCheckResponse.builder()
                                .date(current)
                                .isEvent(true)
                                .eventTag("E")
                                .eventId(e.getId())
                                .eventTitle(e.getEventTitle())
                                .eventType(e.getEventType())
                                .applicableTo(e.getApplicableTo())
                                .description(e.getDescription())
                                .build());
                        break;
                    }
                }
            }
        }

        return StandardResponse.success(dateMap, "Monthly event dates fetched successfully");
    }

    /* ═════════════════════════ HELPERS ═════════════════════════ */

    private void validateEventRequest(ClassEventRequest request) {
        if (request.getEventType() == null || request.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("Event Type is required");
        }
        if (request.getEventTitle() == null || request.getEventTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event Title is required");
        }
        if (request.getEventDate() == null) {
            throw new IllegalArgumentException("Event Date is required");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getEventDate())) {
            throw new IllegalArgumentException("End Date cannot be before Event Date");
        }

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        if ("SELECTED_CLASSES".equals(applicableTo)) {
            if (request.getClassIds() == null || request.getClassIds().isEmpty()) {
                throw new IllegalArgumentException("At least one class must be selected for 'Selected Classes'");
            }
        } else if ("SELECTED_SECTIONS".equals(applicableTo)) {
            if (request.getClassIds() == null || request.getClassIds().isEmpty()) {
                throw new IllegalArgumentException("Class must be selected for 'Selected Sections'");
            }
            if (request.getSectionIds() == null || request.getSectionIds().isEmpty()) {
                throw new IllegalArgumentException("At least one section must be selected for 'Selected Sections'");
            }
        }
    }

    private String normalizeApplicableTo(String input) {
        if (input == null || input.isBlank()) {
            return "ENTIRE_SCHOOL";
        }
        String clean = input.trim().toUpperCase().replace(" ", "_");
        if (clean.contains("SECTION")) {
            return "SELECTED_SECTIONS";
        }
        if (clean.contains("CLASS")) {
            return "SELECTED_CLASSES";
        }
        return "ENTIRE_SCHOOL";
    }

    private boolean matchesClassAndSection(ClassEvent e, Long classId, Long sectionId) {
        String app = e.getApplicableTo();
        if ("ENTIRE_SCHOOL".equals(app)) {
            return true;
        }
        if ("SELECTED_CLASSES".equals(app)) {
            return classId == null || (e.getClassIds() != null && e.getClassIds().contains(classId));
        }
        if ("SELECTED_SECTIONS".equals(app)) {
            boolean classMatch = classId == null || (e.getClassIds() != null && e.getClassIds().contains(classId));
            boolean sectionMatch = sectionId == null || (e.getSectionIds() != null && e.getSectionIds().contains(sectionId));
            return classMatch && sectionMatch;
        }
        return true;
    }

    private Long resolveCurrentUserId() {
        try {
            if (loginUser != null && loginUser.getUserId() != null) {
                return loginUser.getUserId();
            }
        } catch (Exception ignored) {
        }
        return 1L; // System default admin user ID
    }

    private Map<Long, String> fetchMasterNames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        List<Integer> intIds = ids.stream().filter(Objects::nonNull).map(Long::intValue).toList();
        Map<Long, String> map = new HashMap<>();
        commonMasterRepository.findByIdInAndStatusTrue(intIds)
                .forEach(cm -> map.put(cm.getId().longValue(), cm.getData()));
        return map;
    }

    private ClassEventResponse mapToResponse(ClassEvent e) {
        Set<Long> ids = new HashSet<>();
        if (e.getClassIds() != null) ids.addAll(e.getClassIds());
        if (e.getSectionIds() != null) ids.addAll(e.getSectionIds());
        return mapToResponseWithMap(e, fetchMasterNames(ids));
    }

    private ClassEventResponse mapToResponseWithMap(ClassEvent e, Map<Long, String> masterMap) {
        LocalDate today = IstClock.today();
        LocalDate endDate = e.getEndDate() != null ? e.getEndDate() : e.getEventDate();

        String status = "UPCOMING";
        if (endDate.isBefore(today)) {
            status = "COMPLETED";
        } else if (!today.isBefore(e.getEventDate()) && !today.isAfter(endDate)) {
            status = "TODAY";
        }

        String formattedDate = e.getEventDate().format(DATE_FORMATTER);
        if (e.getEndDate() != null && !e.getEndDate().isEqual(e.getEventDate())) {
            formattedDate = formattedDate + " -> " + e.getEndDate().format(DATE_FORMATTER);
        }

        List<String> classNames = new ArrayList<>();
        if (e.getClassIds() != null) {
            for (Long cid : e.getClassIds()) {
                if (masterMap != null && masterMap.containsKey(cid)) {
                    classNames.add(masterMap.get(cid));
                }
            }
        }

        List<String> sectionNames = new ArrayList<>();
        if (e.getSectionIds() != null) {
            for (Long sid : e.getSectionIds()) {
                if (masterMap != null && masterMap.containsKey(sid)) {
                    sectionNames.add(masterMap.get(sid));
                }
            }
        }

        return ClassEventResponse.builder()
                .id(e.getId())
                .eventType(e.getEventType())
                .eventTitle(e.getEventTitle())
                .eventDate(e.getEventDate())
                .endDate(e.getEndDate())
                .formattedDate(formattedDate)
                .applicableTo(e.getApplicableTo())
                .classIds(e.getClassIds())
                .classNames(classNames)
                .sectionIds(e.getSectionIds())
                .sectionNames(sectionNames)
                .description(e.getDescription())
                .academicSession(e.getAcademicSession())
                .sessionId(e.getSessionId())
                .status(status)
                .createdBy(e.getCreatedBy())
                .createdOn(e.getCreatedOn())
                .modifiedBy(e.getModifiedBy())
                .modifiedAt(e.getModifiedAt())
                .isDeleted(e.getIsDeleted())
                .build();
    }
}

