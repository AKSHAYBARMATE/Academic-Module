package com.academic.mapper;

import com.academic.entity.CommonMaster;
import com.academic.entity.Staff;
import com.academic.entity.TeacherAssignment;
import com.academic.repository.CommonMasterRepository;
import com.academic.repository.StaffRepository;
import com.academic.request.TeacherAssignmentRequest;
import com.academic.response.TeacherAssignmentResponse;

import java.util.List;
import java.util.stream.Collectors;

public class TeacherAssignmentMapper {

    // Convert Request DTO → Entity
    public static TeacherAssignment toEntity(TeacherAssignmentRequest request) {
        TeacherAssignment entity = TeacherAssignment.builder()
                .teacherName(request.getTeacherName())
                .employeeId(request.getEmployeeId())
                .subject(request.getSubject())
                .classId(request.getClassId())
                .sectionId(request.getSectionId())
                .loadHours(request.getLoadHours())
                .status(request.getStatus())
                .isDeleted(false)
                .build();

        // Store class IDs directly in the JSON column
        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            entity.setClassesInvolved(request.getClassIds());
        }

        return entity;
    }

    // Update existing entity
    public static void updateEntity(TeacherAssignment entity, TeacherAssignmentRequest request) {
        entity.setTeacherName(request.getTeacherName());
        entity.setEmployeeId(request.getEmployeeId());
        entity.setSubject(request.getSubject());
        entity.setClassId(request.getClassId());
        entity.setSectionId(request.getSectionId());
        entity.setLoadHours(request.getLoadHours());
        entity.setStatus(request.getStatus());

        if (request.getClassIds() != null && !request.getClassIds().isEmpty()) {
            entity.setClassesInvolved(request.getClassIds());
        }
    }

    // Convert Entity → Response DTO
    public static TeacherAssignmentResponse toResponse(TeacherAssignment entity,
                                                       CommonMasterRepository commonMasterRepository, StaffRepository staffRepository) {
        List<Long> classIds = entity.getClassesInvolved(); // Already stored as List<Long>

        // Fetch human-readable names from CommonMaster
        List<String> classNames = classIds.stream()
                .map(id -> commonMasterRepository.findById(Math.toIntExact(id))
                        .map(CommonMaster::getData)
                        .orElse("Unknown"))
                .collect(Collectors.toList());

        Staff staff = staffRepository.findById(Long.parseLong(entity.getEmployeeId())).get();

        return TeacherAssignmentResponse.builder()
                .id(entity.getId())
                .teacherName(entity.getTeacherName())
                .employeeId(staff.getStaffCode())
                .subject(entity.getSubject())
                .classIds(classIds)
                .classNames(classNames)
                .classId(entity.getClassId())
                .sectionId(entity.getSectionId())
                .loadHours(entity.getLoadHours())
                .status(entity.getStatus())
                .build();
    }
}
