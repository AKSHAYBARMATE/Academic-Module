package com.academic.request;

import lombok.Data;

@Data
public class GradingRuleUpdateItem {

    private Long id;
    private Double gradePoint;
    private Integer minPercentage;
    private Integer maxPercentage;
    private String description;
}
