package com.academic.repository;

import com.academic.entity.WeeklyOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyOffRepository extends JpaRepository<WeeklyOff, Long> {

    List<WeeklyOff> findByIsDeletedFalseOrderByDayOfWeekAsc();

    Optional<WeeklyOff> findByDayOfWeekAndIsDeletedFalse(DayOfWeek dayOfWeek);

    Optional<WeeklyOff> findByDayOfWeek(DayOfWeek dayOfWeek);

    boolean existsByDayOfWeekAndIsDeletedFalse(DayOfWeek dayOfWeek);
}
