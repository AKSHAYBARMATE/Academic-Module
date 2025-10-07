package com.academic.mapper;

import com.academic.entity.TeacherAssignment;
import com.academic.request.TeacherAssignmentRequest;
import com.academic.response.TeacherAssignmentResponse;

public class TeacherAssignmentMapper {

    public static TeacherAssignment toEntity(TeacherAssignmentRequest request) {
        return TeacherAssignment.builder()
                .teacherName(request.getTeacherName())
                .employeeId(request.getEmployeeId())
                .subject(request.getSubject())
                .classes(request.getClasses())
                .loadHours(request.getLoadHours())
                .status(request.getStatus())
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(TeacherAssignment entity, TeacherAssignmentRequest request) {
        entity.setTeacherName(request.getTeacherName());
        entity.setEmployeeId(request.getEmployeeId());
        entity.setSubject(request.getSubject());
        entity.setClasses(request.getClasses());
        entity.setLoadHours(request.getLoadHours());
        entity.setStatus(request.getStatus());
    }

    public static TeacherAssignmentResponse toResponse(TeacherAssignment entity) {
        return TeacherAssignmentResponse.builder()
                .id(entity.getId())
                .teacherName(entity.getTeacherName())
                .employeeId(entity.getEmployeeId())
                .subject(entity.getSubject())
                .classes(entity.getClasses())
                .loadHours(entity.getLoadHours())
                .status(entity.getStatus())
                .build();
    }
}
