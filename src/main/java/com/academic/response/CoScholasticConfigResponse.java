package com.academic.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoScholasticConfigResponse {

    private Long id;

    private Long activityId;

    private String activityName;

    private Integer maxMarks;

}