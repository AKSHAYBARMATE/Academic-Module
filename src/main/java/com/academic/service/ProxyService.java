package com.academic.service;

import com.academic.request.ProxyAssignmentRequest;
import com.academic.request.ProxyTemplateRequest;
import com.academic.response.ProxyAssignmentResponse;
import com.academic.response.ProxyTemplateResponse;
import com.academic.response.StandardResponse;
import com.academic.request.TimeSlotDTO;

import java.util.List;
import java.util.Map;

public interface ProxyService {

    // =========================================================================
    //  PROXY TEMPLATE MANAGEMENT
    // =========================================================================
    StandardResponse<ProxyTemplateResponse> createTemplate(ProxyTemplateRequest request);

    StandardResponse<ProxyTemplateResponse> updateTemplate(Long id, ProxyTemplateRequest request);

    StandardResponse<ProxyTemplateResponse> getTemplateById(Long id);

    StandardResponse<Void> deleteTemplate(Long id);

    StandardResponse<Map<String, Object>> listTemplates(Integer page, Integer size, String search);

    // =========================================================================
    //  PROXY ASSIGNMENT SCHEDULE
    // =========================================================================
    StandardResponse<ProxyAssignmentResponse> assignProxySlot(ProxyAssignmentRequest request);

    StandardResponse<Void> deleteAssignment(Long id);

    StandardResponse<Map<String, Object>> listAssignments(
            Integer page,
            Integer size,
            Long templateId,
            Long timetableId,
            String proxyDate,
            String status);

    /** Get all assignments for a specific proxy teacher */
    StandardResponse<List<ProxyAssignmentResponse>> getAssignmentsForTeacher(Long teacherId);

    /** Get available timetable slots for selection based on class, section, and day */
    StandardResponse<List<TimeSlotDTO>> getAvailableTimetableSlots(Long classId, Long sectionId, Integer dayOfWeek);
}
