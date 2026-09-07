package com.academic.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Data
public class LoginUser {
    private Long userId;
    private String name;
    private String email;
    private String userType;
    private Long studentId;
    private Long staffId;
}
