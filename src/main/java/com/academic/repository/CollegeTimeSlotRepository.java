package com.academic.repository;

import com.academic.entity.CollegeTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeTimeSlotRepository extends JpaRepository<CollegeTimeSlot, Long> {

    List<CollegeTimeSlot> findByIsDeletedFalseOrderBySlotOrderAscIdAsc();

    List<CollegeTimeSlot> findByIsDeletedFalseAndStatusOrderBySlotOrderAscIdAsc(String status);

    Optional<CollegeTimeSlot> findByIdAndIsDeletedFalse(Long id);

    boolean existsByIsDeletedFalse();
}
