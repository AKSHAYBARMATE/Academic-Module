package com.academic.dto;

import lombok.Data;

@Data
public class ComponentMarksResponse {

    private Integer componentId;

    private String componentName;

    private Integer marksObtained;

    private Integer maxMarks;
}
