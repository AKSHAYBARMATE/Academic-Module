package com.academic.mapper;

import com.academic.entity.ClassSection;
import com.academic.entity.CommonMaster;
import com.academic.repository.CommonMasterRepository;
import com.academic.request.ClassSectionRequest;
import com.academic.response.ClassSectionResponse;

public class ClassSectionMapper {

    public static ClassSection toEntity(ClassSectionRequest request) {
        return ClassSection.builder()
                .classId(request.getClassId())
                .section(request.getSection())
                .classTeacher(request.getClassTeacher())
                .students(request.getStudents())
                .isDeleted(false)
                .roomNo(request.getRoomNo())
                .build();
    }

    public static void updateEntity(ClassSection entity, ClassSectionRequest request) {
        entity.setClassId(request.getClassId());
        entity.setSection(request.getSection());
        entity.setClassTeacher(request.getClassTeacher());
        entity.setStudents(request.getStudents());
        entity.setRoomNo(request.getRoomNo());
    }

    public static ClassSectionResponse toResponse(ClassSection entity, CommonMasterRepository commonMasterRepository) {
        // Fetch class name from CommonMaster
        String className = commonMasterRepository.findByIdAndStatusTrue(entity.getClassId())
                .map(CommonMaster::getData)
                .orElse("Unknown Class");

        // Fetch section name from CommonMaster
        String sectionName = commonMasterRepository.findByIdAndStatusTrue(entity.getSection())
                .map(CommonMaster::getData)
                .orElse("Unknown Section");

        return ClassSectionResponse.builder()
                .id(entity.getId())
                .className(className)          // Name from CommonMaster
                .classId(entity.getClassId())
                .sectionName(sectionName)
                .sectionId(entity.getSection())// Section name from CommonMaster
                .classTeacher(entity.getClassTeacher())
                .students(entity.getStudents())
                .roomNo(entity.getRoomNo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
