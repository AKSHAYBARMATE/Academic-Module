package com.academic.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaAuditingConfig implements AuditorAware<Long> {

    private final LoginUser loginUser;

    public JpaAuditingConfig(LoginUser loginUser) {
        this.loginUser = loginUser;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(loginUser.getUserId());
    }
}
