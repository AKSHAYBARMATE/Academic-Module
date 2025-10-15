package com.academic.mapper;

import com.academic.entity.ExamSetup;
import com.academic.entity.CommonMaster;
import com.academic.repository.CommonMasterRepository;
import com.academic.request.ExamSetupRequest;
import com.academic.response.ExamSetupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamSetupMapper {

    private final CommonMasterRepository commonMasterRepository;

    public ExamSetup toEntity(ExamSetupRequest request) {
        if (request == null) return null;
        return ExamSetup.builder()
                .examName(request.getExamName())
                .classId(request.getClassId())
                .subjectId(request.getSubjectId())
                .examDate(request.getExamDate())
                .maxMarks(request.getMaxMarks())
                .isDeleted(false)
                .build();
    }

    public ExamSetupResponse toResponse(ExamSetup entity) {
        if (entity == null) return null;

        String className = getCommonMasterName(entity.getClassId());
        String subjectName = getCommonMasterName(entity.getSubjectId());

        return ExamSetupResponse.builder()
                .id(entity.getId())
                .examName(entity.getExamName())
                .classId(entity.getClassId())
                .subjectId(entity.getSubjectId())
                .examDate(entity.getExamDate())
                .maxMarks(entity.getMaxMarks())
                .className(className)
                .subjectName(subjectName)
                .build();
    }

    private String getCommonMasterName(Integer id) {
        if (id == null) return null;
        return commonMasterRepository.findById(id)
                .map(CommonMaster::getData)
                .orElse("Unknown");
    }
}
