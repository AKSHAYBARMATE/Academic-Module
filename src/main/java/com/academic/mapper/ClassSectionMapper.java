package com.academic.mapper;

import com.academic.entity.ClassSection;
import com.academic.request.ClassSectionRequest;
import com.academic.response.ClassSectionResponse;

public class ClassSectionMapper {

    public static ClassSection toEntity(ClassSectionRequest request) {
        return ClassSection.builder()
                .className(request.getClassName())
                .classId(request.getClassId())
                .section(request.getSection())
                .classTeacher(request.getClassTeacher())
                .students(request.getStudents())
                .isDeleted(false)
                .roomNo(request.getRoomNo())
                .build();
    }

    public static void updateEntity(ClassSection entity, ClassSectionRequest request) {
        entity.setClassName(request.getClassName());
        entity.setClassId(request.getClassId());
        entity.setSection(request.getSection());
        entity.setClassTeacher(request.getClassTeacher());
        entity.setStudents(request.getStudents());
        entity.setRoomNo(request.getRoomNo());
    }

    public static ClassSectionResponse toResponse(ClassSection entity) {
        return ClassSectionResponse.builder()
                .id(entity.getId())
                .className(entity.getClassName())
                .classId(entity.getClassId())
                .section(entity.getSection())
                .classTeacher(entity.getClassTeacher())
                .students(entity.getStudents())
                .roomNo(entity.getRoomNo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
