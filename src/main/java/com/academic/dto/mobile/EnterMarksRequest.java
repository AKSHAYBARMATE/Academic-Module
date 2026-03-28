package com.academic.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterMarksRequest {
    private Integer classId;
    private Integer examTypeId;
    private Long subjectId;
    private List<StudentMarkDto> marks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentMarkDto {
        private Long studentId;
        private List<ComponentMarkDto> componentMarks;
        private Integer theoryMarks;
        private Integer practicalMarks;
        private Integer internalMarks;
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentMarkDto {
        private Integer componentId;
        private Integer marksObtained;
    }
}
