package com.academic.response;


import lombok.Data;

@Data
public class CoScholasticResponse {

    private Long activityId;

    private String activityName;

    private Integer marksObtained;

    private Integer maxMarks;

    private String grade;
}