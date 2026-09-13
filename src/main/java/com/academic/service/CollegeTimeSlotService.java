package com.academic.service;

import com.academic.request.CollegeTimeSlotRequest;
import com.academic.response.CollegeTimeSlotResponse;

import java.util.List;

public interface CollegeTimeSlotService {

    List<CollegeTimeSlotResponse> getAllSlots();

    CollegeTimeSlotResponse getSlotById(Long id);

    CollegeTimeSlotResponse createSlot(CollegeTimeSlotRequest request);

    CollegeTimeSlotResponse updateSlot(Long id, CollegeTimeSlotRequest request);

    void deleteSlot(Long id);

    List<CollegeTimeSlotResponse> bulkSaveSlots(List<CollegeTimeSlotRequest> requests);

    List<CollegeTimeSlotResponse> resetDefaultSlots();
}
