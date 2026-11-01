package com.academic.service;

import com.academic.config.LoginUser;
import com.academic.entity.WeeklyOff;
import com.academic.repository.WeeklyOffRepository;
import com.academic.request.WeeklyOffRequest;
import com.academic.response.StandardResponse;
import com.academic.response.WeeklyOffCheckResponse;
import com.academic.response.WeeklyOffResponse;
import com.academic.utility.IstClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WeeklyOffServiceImpl implements WeeklyOffService {

    private final WeeklyOffRepository weeklyOffRepository;
    private final LoginUser loginUser;

    // Ordered list matching the UI cards from Sun to Sat
    private static final List<DayOfWeek> ORDERED_DAYS = List.of(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    @Override
    public StandardResponse<WeeklyOffResponse> saveWeeklyOff(WeeklyOffRequest request) {
        log.info("Saving weekly off configuration with request: {}", request);

        Set<DayOfWeek> targetDays = new HashSet<>();
        if (request != null) {
            if (request.getDays() != null) {
                for (String d : request.getDays()) {
                    targetDays.add(parseDayOfWeek(d));
                }
            } else if (request.getDayOfWeek() != null && !request.getDayOfWeek().isBlank()) {
                targetDays.add(parseDayOfWeek(request.getDayOfWeek()));
            }
        }

        Long currentUserId = resolveCurrentUserId();

        for (DayOfWeek day : ORDERED_DAYS) {
            Optional<WeeklyOff> existingOpt = weeklyOffRepository.findByDayOfWeek(day);
            boolean shouldBeOff = targetDays.contains(day);

            if (shouldBeOff) {
                if (existingOpt.isPresent()) {
                    WeeklyOff w = existingOpt.get();
                    if (Boolean.TRUE.equals(w.getIsDeleted())) {
                        w.setIsDeleted(false);
                        w.setModifiedBy(currentUserId);
                        w.setModifiedAt(IstClock.nowDateTime());
                        weeklyOffRepository.save(w);
                    }
                } else {
                    WeeklyOff newOff = WeeklyOff.builder()
                            .dayOfWeek(day)
                            .dayName(formatDayName(day))
                            .build();
                    newOff.setCreatedBy(currentUserId);
                    newOff.setModifiedBy(currentUserId);
                    newOff.setCreatedOn(IstClock.nowDateTime());
                    newOff.setModifiedAt(IstClock.nowDateTime());
                    newOff.setIsDeleted(false);
                    weeklyOffRepository.save(newOff);
                }
            } else {
                if (existingOpt.isPresent()) {
                    WeeklyOff w = existingOpt.get();
                    if (!Boolean.TRUE.equals(w.getIsDeleted())) {
                        w.setIsDeleted(true);
                        w.setModifiedBy(currentUserId);
                        w.setModifiedAt(IstClock.nowDateTime());
                        weeklyOffRepository.save(w);
                    }
                }
            }
        }

        return StandardResponse.success(buildResponse(), "Weekly off days configured successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<WeeklyOffResponse> getWeeklyOffConfiguration() {
        return StandardResponse.success(buildResponse(), "Weekly off configuration fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<WeeklyOffCheckResponse> checkIsWeeklyOff(LocalDate date) {
        if (date == null) {
            date = IstClock.today();
        }

        DayOfWeek dow = date.getDayOfWeek();
        boolean isOff = weeklyOffRepository.existsByDayOfWeekAndIsDeletedFalse(dow);

        WeeklyOffCheckResponse res = WeeklyOffCheckResponse.builder()
                .date(date)
                .dayOfWeek(dow)
                .dayName(formatDayName(dow))
                .isWeeklyOff(isOff)
                .build();

        return StandardResponse.success(res, isOff ? "Date is a weekly off" : "Date is a working day");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<LocalDate>> getWeeklyOffDatesForMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        List<WeeklyOff> activeOffs = weeklyOffRepository.findByIsDeletedFalseOrderByDayOfWeekAsc();
        Set<DayOfWeek> offDays = activeOffs.stream()
                .map(WeeklyOff::getDayOfWeek)
                .collect(Collectors.toSet());

        YearMonth ym = YearMonth.of(year, month);
        List<LocalDate> dates = new ArrayList<>();

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            if (offDays.contains(date.getDayOfWeek())) {
                dates.add(date);
            }
        }

        return StandardResponse.success(dates, "Monthly weekly off dates fetched successfully");
    }

    /* ═════════════════════════ HELPERS ═════════════════════════ */

    private WeeklyOffResponse buildResponse() {
        List<WeeklyOff> activeOffs = weeklyOffRepository.findByIsDeletedFalseOrderByDayOfWeekAsc();
        Set<DayOfWeek> activeDays = activeOffs.stream()
                .map(WeeklyOff::getDayOfWeek)
                .collect(Collectors.toSet());

        List<WeeklyOffResponse.DayCardDto> cards = new ArrayList<>();
        List<String> configuredDayNames = new ArrayList<>();

        for (DayOfWeek day : ORDERED_DAYS) {
            boolean isOff = activeDays.contains(day);
            String dayName = formatDayName(day);

            cards.add(WeeklyOffResponse.DayCardDto.builder()
                    .shortName(formatShortName(day))
                    .dayName(dayName)
                    .dayOfWeek(day)
                    .isWeeklyOff(isOff)
                    .build());

            if (isOff) {
                configuredDayNames.add(dayName);
            }
        }

        String summary = configuredDayNames.isEmpty() ? "None" : String.join(", ", configuredDayNames);

        return WeeklyOffResponse.builder()
                .configuredWeeklyOff(summary)
                .configuredDays(activeDays.stream().map(DayOfWeek::name).toList())
                .days(cards)
                .build();
    }

    private DayOfWeek parseDayOfWeek(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Day cannot be empty");
        }
        String clean = input.trim().toUpperCase();
        return switch (clean) {
            case "SUN", "SUNDAY" -> DayOfWeek.SUNDAY;
            case "MON", "MONDAY" -> DayOfWeek.MONDAY;
            case "TUE", "TUES", "TUESDAY" -> DayOfWeek.TUESDAY;
            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "THU", "THUR", "THURSDAY" -> DayOfWeek.THURSDAY;
            case "FRI", "FRIDAY" -> DayOfWeek.FRIDAY;
            case "SAT", "SATURDAY" -> DayOfWeek.SATURDAY;
            default -> {
                try {
                    int dowInt = Integer.parseInt(clean);
                    yield DayOfWeek.of(dowInt);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid day: " + input);
                }
            }
        };
    }

    private String formatDayName(DayOfWeek dow) {
        String name = dow.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private String formatShortName(DayOfWeek dow) {
        return switch (dow) {
            case SUNDAY -> "Sun";
            case MONDAY -> "Mon";
            case TUESDAY -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu";
            case FRIDAY -> "Fri";
            case SATURDAY -> "Sat";
        };
    }

    private Long resolveCurrentUserId() {
        try {
            if (loginUser != null && loginUser.getUserId() != null) {
                return loginUser.getUserId();
            }
        } catch (Exception ignored) {
        }
        return 1L;
    }
}
