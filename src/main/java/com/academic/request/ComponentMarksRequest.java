package com.academic.request;

import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
public class ComponentMarksRequest {

    private Integer componentId;

    private Integer marksObtained;

    private Integer maxMarks;
}