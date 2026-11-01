package com.academic.service;

import com.academic.config.LoginUser;
import com.academic.entity.Holiday;
import com.academic.entity.Session;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.HolidayRepository;
import com.academic.repository.SessionRepository;
import com.academic.request.HolidayRequest;
import com.academic.response.HolidayCheckResponse;
import com.academic.response.HolidayResponse;
import com.academic.response.StandardResponse;
import com.academic.utility.IstClock;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final CommonMasterRepository commonMasterRepository;
    private final SessionRepository sessionRepository;
    private final LoginUser loginUser;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @Override
    public StandardResponse<HolidayResponse> createHoliday(HolidayRequest request) {
        log.info("Creating new holiday: {}", request.getHolidayName());
        validateHolidayRequest(request);

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        List<Long> selectedClassIds = "SELECTED_CLASSES".equals(applicableTo) && request.getSelectedClassIds() != null
                ? request.getSelectedClassIds()
                : new ArrayList<>();

        Long currentUserId = resolveCurrentUserId();

        Holiday holiday = Holiday.builder()
                .holidayName(request.getHolidayName().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .holidayType(request.getHolidayType().trim())
                .applicableTo(applicableTo)
                .selectedClassIds(selectedClassIds)
                .description(request.getDescription())
                .sessionId(request.getSessionId())
                .build();

        // Maintain WHO columns
        holiday.setCreatedBy(currentUserId);
        holiday.setModifiedBy(currentUserId);
        holiday.setCreatedOn(IstClock.nowDateTime());
        holiday.setModifiedAt(IstClock.nowDateTime());
        holiday.setIsDeleted(false);

        Holiday saved = holidayRepository.save(holiday);
        log.info("Holiday created successfully with ID: {}", saved.getId());

        return StandardResponse.success(mapToResponse(saved), "Holiday created successfully");
    }

    @Override
    public StandardResponse<HolidayResponse> updateHoliday(Long id, HolidayRequest request) {
        log.info("Updating holiday with ID: {}", id);
        validateHolidayRequest(request);

        Holiday holiday = holidayRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with ID: " + id));

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        List<Long> selectedClassIds = "SELECTED_CLASSES".equals(applicableTo) && request.getSelectedClassIds() != null
                ? request.getSelectedClassIds()
                : new ArrayList<>();

        Long currentUserId = resolveCurrentUserId();

        holiday.setHolidayName(request.getHolidayName().trim());
        holiday.setStartDate(request.getStartDate());
        holiday.setEndDate(request.getEndDate());
        holiday.setHolidayType(request.getHolidayType().trim());
        holiday.setApplicableTo(applicableTo);
        holiday.setSelectedClassIds(selectedClassIds);
        holiday.setDescription(request.getDescription());
        holiday.setSessionId(request.getSessionId());

        // Maintain WHO columns
        holiday.setModifiedBy(currentUserId);
        holiday.setModifiedAt(IstClock.nowDateTime());

        Holiday updated = holidayRepository.save(holiday);
        log.info("Holiday updated successfully with ID: {}", updated.getId());

        return StandardResponse.success(mapToResponse(updated), "Holiday updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<HolidayResponse> getHolidayById(Long id) {
        Holiday holiday = holidayRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with ID: " + id));

        return StandardResponse.success(mapToResponse(holiday), "Holiday fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<HolidayResponse>> getAllHolidays(
            String search,
            String holidayType,
            String applicableTo,
            Long classId,
            Integer year,
            Integer month,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startDate"));

        Specification<Holiday> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("holidayName")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            if (holidayType != null && !holidayType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("holidayType")), holidayType.trim().toLowerCase()));
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
                            cb.lessThanOrEqualTo(root.get("startDate"), endOfMonth),
                            cb.greaterThanOrEqualTo(root.get("endDate"), startOfMonth)
                    ));
                } else {
                    LocalDate startOfYear = LocalDate.of(year, 1, 1);
                    LocalDate endOfYear = LocalDate.of(year, 12, 31);
                    predicates.add(cb.and(
                            cb.lessThanOrEqualTo(root.get("startDate"), endOfYear),
                            cb.greaterThanOrEqualTo(root.get("endDate"), startOfYear)
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Holiday> pageResult = holidayRepository.findAll(spec, pageable);

        // Pre-fetch class names in batch for better performance
        List<Long> allClassIds = pageResult.stream()
                .filter(h -> h.getSelectedClassIds() != null)
                .flatMap(h -> h.getSelectedClassIds().stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> classMap = new HashMap<>();
        if (!allClassIds.isEmpty()) {
            commonMasterRepository.findByIdInAndStatusTrue(allClassIds.stream().map(Long::intValue).toList())
                    .forEach(cm -> classMap.put(cm.getId().longValue(), cm.getData()));
        }

        Page<HolidayResponse> responsePage = pageResult.map(h -> mapToResponseWithClassMap(h, classMap));

        // If classId filter is requested, filter content
        if (classId != null) {
            List<HolidayResponse> filtered = responsePage.getContent().stream()
                    .filter(h -> "ALL_SCHOOL".equals(h.getApplicableTo()) ||
                            (h.getSelectedClassIds() != null && h.getSelectedClassIds().contains(classId)))
                    .collect(Collectors.toList());

            responsePage = new org.springframework.data.domain.PageImpl<>(
                    filtered, pageable, filtered.size()
            );
        }

        return StandardResponse.success(responsePage, "Holidays fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<HolidayResponse>> getAllHolidaysList(Integer year, Integer month, Long classId) {
        List<Holiday> holidays;

        if (year != null && month != null && month >= 1 && month <= 12) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            holidays = holidayRepository.findHolidaysForMonth(year, month, start, end);
        } else {
            holidays = holidayRepository.findByIsDeletedFalseOrderByStartDateAsc();
        }

        List<HolidayResponse> responseList = holidays.stream()
                .filter(h -> {
                    if (classId != null && "SELECTED_CLASSES".equals(h.getApplicableTo())) {
                        return h.getSelectedClassIds() != null && h.getSelectedClassIds().contains(classId);
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return StandardResponse.success(responseList, "Holidays list fetched successfully");
    }

    @Override
    public StandardResponse<Map<String, Object>> deleteHoliday(Long id) {
        log.info("Soft-deleting holiday with ID: {}", id);
        Holiday holiday = holidayRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found with ID: " + id));

        holiday.setIsDeleted(true);
        holiday.setModifiedBy(resolveCurrentUserId());
        holiday.setModifiedAt(IstClock.nowDateTime());
        holidayRepository.save(holiday);

        return StandardResponse.success(null, "Holiday deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<HolidayCheckResponse> checkIsHoliday(LocalDate date, Long classId) {
        if (date == null) {
            date = IstClock.today();
        }

        String classIdStr = classId != null ? String.valueOf(classId) : null;
        Optional<Holiday> holidayOpt = holidayRepository.findHolidayForDateAndClass(date, classId, classIdStr);

        if (holidayOpt.isPresent()) {
            Holiday h = holidayOpt.get();
            HolidayCheckResponse res = HolidayCheckResponse.builder()
                    .date(date)
                    .isHoliday(true)
                    .holidayId(h.getId())
                    .holidayName(h.getHolidayName())
                    .holidayType(h.getHolidayType())
                    .applicableTo(h.getApplicableTo())
                    .description(h.getDescription())
                    .build();
            return StandardResponse.success(res, "Date is a holiday: " + h.getHolidayName());
        }

        HolidayCheckResponse res = HolidayCheckResponse.builder()
                .date(date)
                .isHoliday(false)
                .build();
        return StandardResponse.success(res, "Date is not a holiday");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<HolidayResponse>> getUpcomingHolidays(Integer limit, Long classId) {
        LocalDate today = IstClock.today();
        List<Holiday> holidays = holidayRepository.findUpcomingHolidays(today);

        int max = limit != null && limit > 0 ? limit : 10;

        List<HolidayResponse> list = holidays.stream()
                .filter(h -> {
                    if (classId != null && "SELECTED_CLASSES".equals(h.getApplicableTo())) {
                        return h.getSelectedClassIds() != null && h.getSelectedClassIds().contains(classId);
                    }
                    return true;
                })
                .limit(max)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Upcoming holidays fetched successfully");
    }

    /* ═════════════════════════ HELPERS ═════════════════════════ */

    private void validateHolidayRequest(HolidayRequest request) {
        if (request.getHolidayName() == null || request.getHolidayName().trim().isEmpty()) {
            throw new IllegalArgumentException("Holiday Name is required");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start Date is required");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("End Date is required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start Date cannot be after End Date");
        }
        if (request.getHolidayType() == null || request.getHolidayType().trim().isEmpty()) {
            throw new IllegalArgumentException("Holiday Type is required");
        }

        String applicableTo = normalizeApplicableTo(request.getApplicableTo());
        if ("SELECTED_CLASSES".equals(applicableTo)) {
            if (request.getSelectedClassIds() == null || request.getSelectedClassIds().isEmpty()) {
                throw new IllegalArgumentException("At least one class must be selected when Applicable To is 'Selected Classes'");
            }
        }
    }

    private String normalizeApplicableTo(String input) {
        if (input == null || input.isBlank()) {
            return "ALL_SCHOOL";
        }
        String clean = input.trim().toUpperCase().replace(" ", "_");
        if (clean.contains("SELECTED") || clean.contains("CLASS")) {
            return "SELECTED_CLASSES";
        }
        return "ALL_SCHOOL";
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

    private HolidayResponse mapToResponse(Holiday h) {
        return mapToResponseWithClassMap(h, null);
    }

    private HolidayResponse mapToResponseWithClassMap(Holiday h, Map<Long, String> preloadedClassMap) {
        LocalDate today = IstClock.today();

        String status = "UPCOMING";
        if (h.getEndDate() != null && h.getEndDate().isBefore(today)) {
            status = "COMPLETED";
        } else if (h.getStartDate() != null && (h.getStartDate().isBefore(today) || h.getStartDate().isEqual(today))
                && (h.getEndDate() == null || h.getEndDate().isAfter(today) || h.getEndDate().isEqual(today))) {
            status = "ONGOING";
        }

        long totalDays = 0;
        String formattedRange = "";
        if (h.getStartDate() != null && h.getEndDate() != null) {
            totalDays = ChronoUnit.DAYS.between(h.getStartDate(), h.getEndDate()) + 1;
            if (h.getStartDate().isEqual(h.getEndDate())) {
                formattedRange = h.getStartDate().format(DATE_FORMATTER);
            } else {
                formattedRange = h.getStartDate().format(DATE_FORMATTER) + " -> " + h.getEndDate().format(DATE_FORMATTER);
            }
        }

        List<String> classNames = new ArrayList<>();
        if (h.getSelectedClassIds() != null && !h.getSelectedClassIds().isEmpty()) {
            for (Long cid : h.getSelectedClassIds()) {
                if (cid != null) {
                    if (preloadedClassMap != null && preloadedClassMap.containsKey(cid)) {
                        classNames.add(preloadedClassMap.get(cid));
                    } else {
                        commonMasterRepository.findByIdAndStatusTrue(cid.intValue())
                                .ifPresent(cm -> classNames.add(cm.getData()));
                    }
                }
            }
        }

        String sessionName = null;
        if (h.getSessionId() != null) {
            sessionName = sessionRepository.findById(h.getSessionId().intValue())
                    .map(Session::getSession)
                    .orElse(null);
        }

        return HolidayResponse.builder()
                .id(h.getId())
                .holidayName(h.getHolidayName())
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .formattedDateRange(formattedRange)
                .totalDays(totalDays)
                .holidayType(h.getHolidayType())
                .applicableTo(h.getApplicableTo())
                .selectedClassIds(h.getSelectedClassIds())
                .selectedClassNames(classNames)
                .description(h.getDescription())
                .sessionId(h.getSessionId())
                .sessionName(sessionName)
                .status(status)
                .createdBy(h.getCreatedBy())
                .createdOn(h.getCreatedOn())
                .modifiedBy(h.getModifiedBy())
                .modifiedAt(h.getModifiedAt())
                .isDeleted(h.getIsDeleted())
                .build();
    }
}
