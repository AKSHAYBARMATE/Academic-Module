package com.academic.mapper;

import com.academic.entity.Degree;
import com.academic.entity.Department;
import com.academic.entity.Subject;
import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectMapper {

    public static Subject toEntity(SubjectRequest request, Degree degree, Department department) {
        if (request == null) return null;

        String resolvedCode = request.getResolvedCode();
        String resolvedName = request.getResolvedName();

        // Resolve faculties string
        String facultiesStr = null;
        String primaryFaculty = request.getFaculty();

        if (request.getFaculties() != null && !request.getFaculties().isEmpty()) {
            List<String> filtered = request.getFaculties().stream()
                    .filter(f -> f != null && !f.trim().isBlank())
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                facultiesStr = String.join(", ", filtered);
                if (primaryFaculty == null || primaryFaculty.trim().isBlank()) {
                    primaryFaculty = filtered.get(0);
                }
            }
        } else if (primaryFaculty != null && !primaryFaculty.trim().isBlank()) {
            facultiesStr = primaryFaculty.trim();
        }

        return Subject.builder()
                .subjectCode(resolvedCode)
                .subjectName(resolvedName)
                .degree(degree)
                .department(department)
                .program(request.getProgram() != null ? request.getProgram().trim() : null)
                .semester(request.getSemester() != null ? request.getSemester().trim() : null)
                .credits(request.getCredits() != null ? request.getCredits() : 4)
                .type(request.getType() != null && !request.getType().isBlank() ? request.getType().trim() : "Theory")
                .hrsPerWeek(request.getHrsPerWeek() != null ? request.getHrsPerWeek() : 4)
                .faculty(primaryFaculty != null ? primaryFaculty.trim() : null)
                .faculties(facultiesStr)
                .academicYear(request.getAcademicYear() != null ? request.getAcademicYear().trim() : "2025-2026")
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "Active")
                .isDeleted(false)
                .build();
    }

    public static Subject toEntity(SubjectRequest request) {
        return toEntity(request, null, null);
    }

    public static void updateEntity(Subject entity, SubjectRequest request, Degree degree, Department department) {
        if (entity == null || request == null) return;

        String resolvedCode = request.getResolvedCode();
        if (resolvedCode != null && !resolvedCode.isBlank()) {
            entity.setSubjectCode(resolvedCode);
        }

        String resolvedName = request.getResolvedName();
        if (resolvedName != null && !resolvedName.isBlank()) {
            entity.setSubjectName(resolvedName);
        }

        if (degree != null) {
            entity.setDegree(degree);
        }
        if (department != null) {
            entity.setDepartment(department);
        }

        if (request.getProgram() != null) {
            entity.setProgram(request.getProgram().trim());
        }
        if (request.getSemester() != null) {
            entity.setSemester(request.getSemester().trim());
        }
        if (request.getCredits() != null) {
            entity.setCredits(request.getCredits());
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            entity.setType(request.getType().trim());
        }
        if (request.getHrsPerWeek() != null) {
            entity.setHrsPerWeek(request.getHrsPerWeek());
        }
        if (request.getAcademicYear() != null && !request.getAcademicYear().isBlank()) {
            entity.setAcademicYear(request.getAcademicYear().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus().trim());
        }

        // Faculty & faculties mapping
        if (request.getFaculties() != null) {
            List<String> filtered = request.getFaculties().stream()
                    .filter(f -> f != null && !f.trim().isBlank())
                    .map(String::trim)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                entity.setFaculties(String.join(", ", filtered));
                entity.setFaculty(filtered.get(0));
            } else {
                entity.setFaculties(null);
                if (request.getFaculty() != null) {
                    entity.setFaculty(request.getFaculty().trim());
                }
            }
        } else if (request.getFaculty() != null) {
            entity.setFaculty(request.getFaculty().trim());
            if (entity.getFaculties() == null || entity.getFaculties().isBlank()) {
                entity.setFaculties(request.getFaculty().trim());
            }
        }
    }

    public static void updateEntity(Subject entity, SubjectRequest request) {
        updateEntity(entity, request, null, null);
    }

    public static SubjectResponse toResponse(Subject entity) {
        if (entity == null) return null;

        List<String> facultyList = new ArrayList<>();
        if (entity.getFaculties() != null && !entity.getFaculties().isBlank()) {
            facultyList = Arrays.stream(entity.getFaculties().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else if (entity.getFaculty() != null && !entity.getFaculty().isBlank()) {
            facultyList.add(entity.getFaculty().trim());
        }

        Integer degreeId = entity.getDegree() != null ? entity.getDegree().getId() : null;
        String degreeCode = entity.getDegree() != null ? entity.getDegree().getCode() : null;
        String degreeName = entity.getDegree() != null ? entity.getDegree().getName() : null;

        Integer deptId = entity.getDepartment() != null ? entity.getDepartment().getId() : null;
        String deptName = entity.getDepartment() != null ? entity.getDepartment().getName() : null;
        String deptCode = entity.getDepartment() != null ? entity.getDepartment().getCode() : null;

        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getSubjectCode())
                .subjectCode(entity.getSubjectCode())
                .name(entity.getSubjectName())
                .subjectName(entity.getSubjectName())
                .degreeId(degreeId)
                .degreeCode(degreeCode)
                .degreeName(degreeName)
                .departmentId(deptId)
                .departmentName(deptName)
                .departmentCode(deptCode)
                .department(deptName) // Alias for backward compatibility
                .program(entity.getProgram())
                .semester(entity.getSemester())
                .credits(entity.getCredits())
                .type(entity.getType())
                .hrsPerWeek(entity.getHrsPerWeek())
                .faculty(entity.getFaculty())
                .faculties(facultyList)
                .academicYear(entity.getAcademicYear())
                .description(entity.getDescription())
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
