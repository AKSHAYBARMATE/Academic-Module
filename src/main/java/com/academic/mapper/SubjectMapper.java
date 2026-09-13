package com.academic.mapper;

import com.academic.entity.Subject;
import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectMapper {

    public static Subject toEntity(SubjectRequest request) {
        if (request == null) return null;

        String resolvedCode = request.getResolvedCode();
        String resolvedName = request.getResolvedName();
        String resolvedDept = request.getResolvedDepartment();

        return Subject.builder()
                .subjectCode(resolvedCode)
                .subjectName(resolvedName)
                .department(resolvedDept)
                .credits(request.getCredits() != null ? request.getCredits() : 4)
                .type(request.getType() != null && !request.getType().isBlank() ? request.getType().trim() : "Core")
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Active")
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(Subject entity, SubjectRequest request) {
        if (entity == null || request == null) return;

        String resolvedCode = request.getResolvedCode();
        if (resolvedCode != null && !resolvedCode.isBlank()) {
            entity.setSubjectCode(resolvedCode);
        }

        String resolvedName = request.getResolvedName();
        if (resolvedName != null && !resolvedName.isBlank()) {
            entity.setSubjectName(resolvedName);
        }

        if (request.getDepartment() != null) {
            entity.setDepartment(request.getDepartment().trim());
        }

        if (request.getCredits() != null) {
            entity.setCredits(request.getCredits());
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            entity.setType(request.getType().trim());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus().trim());
        }
    }

    public static SubjectResponse toResponse(Subject entity) {
        if (entity == null) return null;

        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getSubjectCode())
                .subjectCode(entity.getSubjectCode())
                .name(entity.getSubjectName())
                .subjectName(entity.getSubjectName())
                .department(entity.getDepartment())
                .departmentName(entity.getDepartment())
                .credits(entity.getCredits())
                .type(entity.getType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static List<SubjectResponse> toResponseList(List<Subject> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(SubjectMapper::toResponse).collect(Collectors.toList());
    }
}
