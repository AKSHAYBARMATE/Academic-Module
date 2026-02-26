package com.academic.utility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Centralised clock utility that always returns the current date/time
 * in Indian Standard Time (IST = UTC+5:30), regardless of the JVM or
 * server OS timezone setting.
 *
 * Use this instead of plain LocalDate.now() / LocalTime.now() throughout
 * the application to guarantee IST correctness.
 */
public final class IstClock {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private IstClock() {
    }

    /** Current date in IST. */
    public static LocalDate today() {
        return LocalDate.now(IST);
    }

    /** Current time-of-day in IST. */
    public static LocalTime nowTime() {
        return LocalTime.now(IST);
    }

    /** Current date-time in IST. */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(IST);
    }
}
