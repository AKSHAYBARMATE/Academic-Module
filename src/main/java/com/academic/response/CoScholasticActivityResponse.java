package com.academic.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoScholasticActivityResponse {

    private Long id;
    private String activityName;
    private Integer displayOrder;

}