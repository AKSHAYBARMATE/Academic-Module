package com.academic.mapper;


import com.academic.entity.Subject;
import com.academic.request.SubjectRequest;
import com.academic.response.SubjectResponse;

public class SubjectMapper {

    public static Subject toEntity(SubjectRequest request) {
        return Subject.builder()
                .subjectCode(request.getSubjectCode())
                .subjectName(request.getSubjectName())
                .department(request.getDepartment())
                .credits(request.getCredits())
                .type(request.getType())
                .status(request.getStatus())
                .isDeleted(false)
                .build();
    }

    public static void updateEntity(Subject entity, SubjectRequest request) {
        entity.setSubjectCode(request.getSubjectCode());
        entity.setSubjectName(request.getSubjectName());
        entity.setDepartment(request.getDepartment());
        entity.setCredits(request.getCredits());
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
    }

    public static SubjectResponse toResponse(Subject entity) {
        return SubjectResponse.builder()
                .id(entity.getId())
                .subjectCode(entity.getSubjectCode())
                .subjectName(entity.getSubjectName())
                .department(entity.getDepartment())
                .credits(entity.getCredits())
                .type(entity.getType())
                .status(entity.getStatus())
                .build();
    }
}
