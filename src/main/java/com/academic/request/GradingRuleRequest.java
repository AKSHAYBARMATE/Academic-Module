package com.academic.request;

import lombok.Data;

@Data
public class GradingRuleRequest {

    private String gradeName;
    private Double gradePoint;
    private Integer minPercentage;
    private Integer maxPercentage;
    private String description;
}
