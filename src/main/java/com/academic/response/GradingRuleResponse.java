package com.academic.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradingRuleResponse {

    private Long id;
    private String gradeName;
    private Double gradePoint;
    private Integer minPercentage;
    private Integer maxPercentage;
    private String description;
}
