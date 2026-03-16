package com.academic.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComponentConfigResponse {

    private Long componentId;

    private String componentName;

    private Integer maxMarks;
}