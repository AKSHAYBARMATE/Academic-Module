package com.academic.mapper;

import com.academic.entity.Program;
import com.academic.request.ProgramRequest;
import com.academic.response.ProgramResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramMapper {

    public static Program toEntity(ProgramRequest request) {
        if (request == null) {
            return null;
        }
        return Program.builder()
                .code(request.getCode() != null ? request.getCode().trim() : null)
                .name(request.getName() != null ? request.getName().trim() : null)
                .degreeCode(request.getDegreeCode() != null ? request.getDegreeCode().trim() : null)
                .degreeType(request.getDegreeType() != null ? request.getDegreeType().trim() : null)
                .duration(request.getDuration() != null ? request.getDuration().trim() : null)
                .totalSemesters(request.getTotalSemesters())
                .intake(request.getIntake() != null ? request.getIntake() : 60)
                .accreditation(request.getAccreditation() != null ? request.getAccreditation().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Active")
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(Program entity, ProgramRequest request) {
        if (entity == null || request == null) {
            return;
        }
        if (request.getCode() != null) {
            entity.setCode(request.getCode().trim());
        }
        if (request.getName() != null) {
            entity.setName(request.getName().trim());
        }
        if (request.getDegreeCode() != null) {
            entity.setDegreeCode(request.getDegreeCode().trim());
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
        if (request.getIntake() != null) {
            entity.setIntake(request.getIntake());
        }
        if (request.getAccreditation() != null) {
            entity.setAccreditation(request.getAccreditation().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus().trim());
        }
    }

    public static ProgramResponse toResponse(Program entity) {
        if (entity == null) {
            return null;
        }
        return ProgramResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .degreeCode(entity.getDegreeCode())
                .degreeType(entity.getDegreeType())
                .duration(entity.getDuration())
                .totalSemesters(entity.getTotalSemesters())
                .intake(entity.getIntake())
                .accreditation(entity.getAccreditation())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static List<ProgramResponse> toResponseList(List<Program> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(ProgramMapper::toResponse)
                .collect(Collectors.toList());
    }
}
