package com.academic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtPayload {
    private String name;
    private String userType;
    private Long userId;
    private String email;
    private String sub;
    private long iat;
    private long exp;
}
