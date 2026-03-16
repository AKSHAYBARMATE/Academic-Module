package com.academic.request;

import lombok.Data;

@Data
public class CoScholasticUpdateRequest {

    private Long id;

    private Long activityId;

    private Integer maxMarks;

}