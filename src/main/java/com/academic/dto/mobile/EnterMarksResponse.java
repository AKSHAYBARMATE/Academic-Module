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
public class EnterMarksResponse {
    private String className;
    private String examType;
    private String subjectName;
    private List<ComponentDto> components;
    private List<StudentMarkListDto> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentDto {
        private Integer componentId;
        private String componentName;
        private Integer maxMarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentMarkListDto {
        private Long studentId;
        private String studentName;
        private String rollNo;
        private List<StudentComponentMarkDto> componentMarks;
        private Integer marksObtained;
        private String profilePicUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentComponentMarkDto {
        private Integer componentId;
        private Integer marksObtained;
    }
}
