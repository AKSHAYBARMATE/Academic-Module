package com.academic.config;

import lombok.Data;

@Data
public class JwtPayload {
    private String name;
    private String userType;
    private Long userId;
    private String email;
    private String sub;
    private long iat;
    private long exp;
}
