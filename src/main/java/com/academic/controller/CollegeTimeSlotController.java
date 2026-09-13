package com.academic.controller;

import com.academic.request.CollegeTimeSlotRequest;
import com.academic.response.CollegeTimeSlotResponse;
import com.academic.response.StandardResponse;
import com.academic.service.CollegeTimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic-module/college-time-slots")
@RequiredArgsConstructor
@Slf4j
public class CollegeTimeSlotController {

    private final CollegeTimeSlotService timeSlotService;

    /**
     * Get all active college time slots / recess periods
     */
    @GetMapping({"", "/getAll"})
    public ResponseEntity<StandardResponse<List<CollegeTimeSlotResponse>>> getAllSlots() {
        log.info("API call: GET /college-time-slots/getAll");
        List<CollegeTimeSlotResponse> slots = timeSlotService.getAllSlots();
        return ResponseEntity.ok(StandardResponse.success(slots, "College time slots retrieved successfully"));
    }

    /**
     * Get a specific time slot by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<CollegeTimeSlotResponse>> getSlotById(@PathVariable Long id) {
        log.info("API call: GET /college-time-slots/{}", id);
        CollegeTimeSlotResponse slot = timeSlotService.getSlotById(id);
        return ResponseEntity.ok(StandardResponse.success(slot, "Time slot retrieved successfully"));
    }

    /**
     * Create a new time slot or recess break
     */
    @PostMapping("/create")
    public ResponseEntity<StandardResponse<CollegeTimeSlotResponse>> createSlot(
            @Valid @RequestBody CollegeTimeSlotRequest request) {
        log.info("API call: POST /college-time-slots/create - label: {}, time: {}", request.getSlotLabel(), request.getTimeSlot());
        CollegeTimeSlotResponse created = timeSlotService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(created, "College time slot created successfully"));
    }

    /**
     * Update an existing time slot
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<StandardResponse<CollegeTimeSlotResponse>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody CollegeTimeSlotRequest request) {
        log.info("API call: PUT /college-time-slots/update/{} - label: {}, time: {}", id, request.getSlotLabel(), request.getTimeSlot());
        CollegeTimeSlotResponse updated = timeSlotService.updateSlot(id, request);
        return ResponseEntity.ok(StandardResponse.success(updated, "College time slot updated successfully"));
    }

    /**
     * Soft delete a time slot
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteSlot(@PathVariable Long id) {
        log.warn("API call: DELETE /college-time-slots/delete/{}", id);
        timeSlotService.deleteSlot(id);
        return ResponseEntity.ok(StandardResponse.success("College time slot deleted successfully"));
    }

    /**
     * Bulk save or reorder slots
     */
    @PostMapping("/bulkSave")
    public ResponseEntity<StandardResponse<List<CollegeTimeSlotResponse>>> bulkSave(
            @RequestBody List<CollegeTimeSlotRequest> requests) {
        log.info("API call: POST /college-time-slots/bulkSave - count: {}", requests != null ? requests.size() : 0);
        List<CollegeTimeSlotResponse> saved = timeSlotService.bulkSaveSlots(requests);
        return ResponseEntity.ok(StandardResponse.success(saved, "College time slots saved successfully"));
    }

    /**
     * Reset time slots to standard 5 periods + 2 breaks
     */
    @PostMapping("/resetDefaults")
    public ResponseEntity<StandardResponse<List<CollegeTimeSlotResponse>>> resetDefaults() {
        log.warn("API call: POST /college-time-slots/resetDefaults");
        List<CollegeTimeSlotResponse> defaults = timeSlotService.resetDefaultSlots();
        return ResponseEntity.ok(StandardResponse.success(defaults, "College time slots reset to default configuration successfully"));
    }
}
