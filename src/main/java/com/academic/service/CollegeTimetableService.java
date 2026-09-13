package com.academic.service;

import com.academic.request.CollegeTimetableRequest;
import com.academic.response.CollegeTimetableResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CollegeTimetableService {

    CollegeTimetableResponse createSlot(CollegeTimetableRequest request);

    CollegeTimetableResponse updateSlot(Long id, CollegeTimetableRequest request);

    void deleteSlot(Long id);

    CollegeTimetableResponse getSlotById(Long id);

    List<CollegeTimetableResponse> getClassSchedule(String program, String semester, String section, String academicYear);

    List<CollegeTimetableResponse> getTeacherSchedule(Integer staffId, String academicYear);

    List<CollegeTimetableResponse> bulkSaveSchedule(List<CollegeTimetableRequest> requests);

    Page<CollegeTimetableResponse> getAllSlots(
            int page,
            int size,
            String search,
            String program,
            String semester,
            String section,
            String day,
            String academicYear,
            Integer staffId,
            Long subjectId,
            String type,
            String status
    );
}
