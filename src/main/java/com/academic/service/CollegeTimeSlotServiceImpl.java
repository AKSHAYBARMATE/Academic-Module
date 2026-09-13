package com.academic.service;

import com.academic.entity.CollegeTimeSlot;
import com.academic.exception.ResourceNotFoundException;
import com.academic.repository.CollegeTimeSlotRepository;
import com.academic.request.CollegeTimeSlotRequest;
import com.academic.response.CollegeTimeSlotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeTimeSlotServiceImpl implements CollegeTimeSlotService {

    private final CollegeTimeSlotRepository slotRepository;

    @Override
    @Transactional
    public List<CollegeTimeSlotResponse> getAllSlots() {
        List<CollegeTimeSlot> slots = slotRepository.findByIsDeletedFalseOrderBySlotOrderAscIdAsc();
        if (slots.isEmpty()) {
            log.info("No time slots found in database. Initializing standard default schedule periods...");
            slots = initDefaultSlots();
        }
        return slots.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeTimeSlotResponse getSlotById(Long id) {
        CollegeTimeSlot slot = slotRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College time slot not found with id: " + id));
        return toResponse(slot);
    }

    @Override
    @Transactional
    public CollegeTimeSlotResponse createSlot(CollegeTimeSlotRequest request) {
        log.info("Creating new college time slot: label={}, time={}", request.getSlotLabel(), request.getTimeSlot());

        int nextOrder = request.getSlotOrder() != null ? request.getSlotOrder() :
                (int) (slotRepository.count() + 1);

        CollegeTimeSlot slot = CollegeTimeSlot.builder()
                .slotLabel(request.getSlotLabel().trim())
                .timeSlot(request.getTimeSlot().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() :
                        (Boolean.TRUE.equals(request.getIsBreak()) ? 15 : 60))
                .isBreak(Boolean.TRUE.equals(request.getIsBreak()))
                .slotOrder(nextOrder)
                .slotType(resolveSlotType(request))
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Active")
                .isDeleted(false)
                .build();

        CollegeTimeSlot saved = slotRepository.save(slot);
        log.info("Created college time slot id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CollegeTimeSlotResponse updateSlot(Long id, CollegeTimeSlotRequest request) {
        log.info("Updating college time slot id={}: label={}, time={}", id, request.getSlotLabel(), request.getTimeSlot());

        CollegeTimeSlot slot = slotRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College time slot not found with id: " + id));

        if (request.getSlotLabel() != null && !request.getSlotLabel().isBlank()) {
            slot.setSlotLabel(request.getSlotLabel().trim());
        }
        if (request.getTimeSlot() != null && !request.getTimeSlot().isBlank()) {
            slot.setTimeSlot(request.getTimeSlot().trim());
        }
        if (request.getStartTime() != null) {
            slot.setStartTime(request.getStartTime().trim());
        }
        if (request.getEndTime() != null) {
            slot.setEndTime(request.getEndTime().trim());
        }
        if (request.getDurationMinutes() != null) {
            slot.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getIsBreak() != null) {
            slot.setIsBreak(request.getIsBreak());
        }
        if (request.getSlotOrder() != null) {
            slot.setSlotOrder(request.getSlotOrder());
        }
        if (request.getSlotType() != null && !request.getSlotType().isBlank()) {
            slot.setSlotType(request.getSlotType().trim());
        } else {
            slot.setSlotType(resolveSlotType(request));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            slot.setStatus(request.getStatus().trim());
        }

        CollegeTimeSlot updated = slotRepository.save(slot);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSlot(Long id) {
        log.warn("Soft deleting college time slot id: {}", id);
        CollegeTimeSlot slot = slotRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("College time slot not found with id: " + id));
        slot.setIsDeleted(true);
        slotRepository.save(slot);
    }

    @Override
    @Transactional
    public List<CollegeTimeSlotResponse> bulkSaveSlots(List<CollegeTimeSlotRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return getAllSlots();
        }

        List<CollegeTimeSlot> result = new ArrayList<>();
        int orderIndex = 1;
        for (CollegeTimeSlotRequest req : requests) {
            CollegeTimeSlot slot;
            if (req.getId() != null && req.getId() > 0) {
                slot = slotRepository.findByIdAndIsDeletedFalse(req.getId()).orElse(null);
                if (slot != null) {
                    slot.setSlotLabel(req.getSlotLabel() != null ? req.getSlotLabel().trim() : slot.getSlotLabel());
                    slot.setTimeSlot(req.getTimeSlot() != null ? req.getTimeSlot().trim() : slot.getTimeSlot());
                    slot.setStartTime(req.getStartTime());
                    slot.setEndTime(req.getEndTime());
                    slot.setDurationMinutes(req.getDurationMinutes() != null ? req.getDurationMinutes() : slot.getDurationMinutes());
                    slot.setIsBreak(req.getIsBreak() != null ? req.getIsBreak() : slot.getIsBreak());
                    slot.setSlotOrder(req.getSlotOrder() != null ? req.getSlotOrder() : orderIndex);
                    slot.setSlotType(resolveSlotType(req));
                    slot.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
                    result.add(slotRepository.save(slot));
                    orderIndex++;
                    continue;
                }
            }
            // Create if ID is not present
            slot = CollegeTimeSlot.builder()
                    .slotLabel(req.getSlotLabel().trim())
                    .timeSlot(req.getTimeSlot().trim())
                    .startTime(req.getStartTime())
                    .endTime(req.getEndTime())
                    .durationMinutes(req.getDurationMinutes() != null ? req.getDurationMinutes() : (Boolean.TRUE.equals(req.getIsBreak()) ? 15 : 60))
                    .isBreak(Boolean.TRUE.equals(req.getIsBreak()))
                    .slotOrder(req.getSlotOrder() != null ? req.getSlotOrder() : orderIndex)
                    .slotType(resolveSlotType(req))
                    .status("Active")
                    .isDeleted(false)
                    .build();
            result.add(slotRepository.save(slot));
            orderIndex++;
        }

        return result.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CollegeTimeSlotResponse> resetDefaultSlots() {
        log.warn("Resetting all college time slots to default standard configuration");
        List<CollegeTimeSlot> existing = slotRepository.findByIsDeletedFalseOrderBySlotOrderAscIdAsc();
        for (CollegeTimeSlot s : existing) {
            s.setIsDeleted(true);
            slotRepository.save(s);
        }
        List<CollegeTimeSlot> fresh = initDefaultSlots();
        return fresh.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private List<CollegeTimeSlot> initDefaultSlots() {
        List<CollegeTimeSlot> defaults = List.of(
                CollegeTimeSlot.builder()
                        .slotLabel("Period 1")
                        .timeSlot("09:00 - 10:00 AM")
                        .startTime("09:00")
                        .endTime("10:00")
                        .durationMinutes(60)
                        .isBreak(false)
                        .slotOrder(1)
                        .slotType("Standard Lecture")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Period 2")
                        .timeSlot("10:00 - 11:00 AM")
                        .startTime("10:00")
                        .endTime("11:00")
                        .durationMinutes(60)
                        .isBreak(false)
                        .slotOrder(2)
                        .slotType("Standard Lecture")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Morning Refreshment")
                        .timeSlot("11:00 - 11:15 AM")
                        .startTime("11:00")
                        .endTime("11:15")
                        .durationMinutes(15)
                        .isBreak(true)
                        .slotOrder(3)
                        .slotType("Recess")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Period 3")
                        .timeSlot("11:15 - 12:15 PM")
                        .startTime("11:15")
                        .endTime("12:15")
                        .durationMinutes(60)
                        .isBreak(false)
                        .slotOrder(4)
                        .slotType("Standard Lecture")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Period 4")
                        .timeSlot("12:15 - 01:15 PM")
                        .startTime("12:15")
                        .endTime("01:15")
                        .durationMinutes(60)
                        .isBreak(false)
                        .slotOrder(5)
                        .slotType("Standard Lecture")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Lunch Break")
                        .timeSlot("01:15 - 02:00 PM")
                        .startTime("01:15")
                        .endTime("02:00")
                        .durationMinutes(45)
                        .isBreak(true)
                        .slotOrder(6)
                        .slotType("Recess")
                        .status("Active")
                        .isDeleted(false)
                        .build(),
                CollegeTimeSlot.builder()
                        .slotLabel("Lab / Elective Block")
                        .timeSlot("02:00 - 04:00 PM")
                        .startTime("02:00")
                        .endTime("04:00")
                        .durationMinutes(120)
                        .isBreak(false)
                        .slotOrder(7)
                        .slotType("Extended Block")
                        .status("Active")
                        .isDeleted(false)
                        .build()
        );
        return slotRepository.saveAll(defaults);
    }

    private String resolveSlotType(CollegeTimeSlotRequest req) {
        if (Boolean.TRUE.equals(req.getIsBreak())) {
            return "Recess";
        }
        if (req.getSlotType() != null && !req.getSlotType().isBlank()) {
            return req.getSlotType().trim();
        }
        Integer duration = req.getDurationMinutes();
        if (duration != null && duration > 60) {
            return "Extended Block";
        }
        return "Standard Lecture";
    }

    private CollegeTimeSlotResponse toResponse(CollegeTimeSlot slot) {
        return CollegeTimeSlotResponse.builder()
                .id(slot.getId())
                .slotLabel(slot.getSlotLabel())
                .timeSlot(slot.getTimeSlot())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .durationMinutes(slot.getDurationMinutes())
                .isBreak(slot.getIsBreak())
                .slotOrder(slot.getSlotOrder())
                .slotType(slot.getSlotType())
                .status(slot.getStatus())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}
