package com.academic.mapper;

import com.academic.entity.Degree;
import com.academic.request.DegreeRequest;
import com.academic.response.DegreeResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DegreeMapper {

    public static Degree toEntity(DegreeRequest request) {
        if (request == null) {
            return null;
        }
        return Degree.builder()
                .code(request.getCode() != null ? request.getCode().trim() : null)
                .name(request.getName() != null ? request.getName().trim() : null)
                .degreeType(request.getDegreeType() != null ? request.getDegreeType().trim() : null)
                .duration(request.getDuration() != null ? request.getDuration().trim() : null)
                .totalSemesters(request.getTotalSemesters())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Active")
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(Degree entity, DegreeRequest request) {
        if (entity == null || request == null) {
            return;
        }
        if (request.getCode() != null) {
            entity.setCode(request.getCode().trim());
        }
        if (request.getName() != null) {
            entity.setName(request.getName().trim());
        }
        if (request.getDegreeType() != null) {
            entity.setDegreeType(request.getDegreeType().trim());
        }
        if (request.getDuration() != null) {
            entity.setDuration(request.getDuration().trim());
        }
        if (request.getTotalSemesters() != null) {
            entity.setTotalSemesters(request.getTotalSemesters());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus().trim());
        }
    }

    public static DegreeResponse toResponse(Degree entity) {
        if (entity == null) {
            return null;
        }
        return DegreeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .degreeType(entity.getDegreeType())
                .duration(entity.getDuration())
                .totalSemesters(entity.getTotalSemesters())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static List<DegreeResponse> toResponseList(List<Degree> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DegreeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
