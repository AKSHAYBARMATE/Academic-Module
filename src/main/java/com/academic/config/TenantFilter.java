package com.academic.config;

import com.academic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final LoginUser loginUser;
    private final UserRepository userRepository;

    public TenantFilter(LoginUser loginUser, UserRepository userRepository) {
        this.loginUser = loginUser;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String resolvedSchema = null;

            // ─── 1. Try to extract schema and user details from JWT Bearer token ───
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String[] parts = token.split("\\.");

                if (parts.length >= 2) {
                    try {
                        String payload = parts[1];
                        String json = new String(Base64.getUrlDecoder().decode(payload));
                        Map<String, Object> claims = mapper.readValue(json, Map.class);

                        log.info("🔍 Decoded JWT Claims: {}", claims);

                        // Populate request-scoped LoginUser bean
                        Object userIdVal = claims.get("userId");
                        if (userIdVal != null) {
                            loginUser.setUserId(Long.valueOf(userIdVal.toString()));
                        }
                        loginUser.setName((String) claims.get("name"));
                        loginUser.setEmail((String) claims.get("email"));
                        loginUser.setUserType((String) claims.get("userType"));

                        // Resolve studentId or staffId
                        if (loginUser.getUserId() != null) {
                            if ("student".equalsIgnoreCase(loginUser.getUserType())) {
                                userRepository.findStudentIdByUserId(loginUser.getUserId())
                                        .ifPresent(id -> loginUser.setStudentId(id));
                            } else {
                                userRepository.findStaffIdByUserId(loginUser.getUserId())
                                        .ifPresent(id -> loginUser.setStaffId(id));
                            }
                        }

                        // Extract schema – support both "schema" and "tenant" claim names
                        resolvedSchema = (String) claims.get("schema");
                        if (resolvedSchema == null) {
                            resolvedSchema = (String) claims.get("tenant");
                        }

                        if (resolvedSchema != null) {
                            log.info("🔥 Extracted schema from JWT: [{}]", resolvedSchema);
                        } else {
                            log.warn("⚠️ No 'schema' or 'tenant' claim found in JWT payload");
                        }

                    } catch (Exception e) {
                        log.error("❌ Error parsing JWT payload: {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ Invalid JWT format (missing payload part)");
                }
            } else {
                log.debug("ℹ️ No Bearer token found in request headers");
            }

            // ─── 2. Fallback: try X-Schema header ────────────────────────────────
            if (resolvedSchema == null || resolvedSchema.trim().isEmpty()) {
                String headerSchema = request.getHeader("X-Schema");
                if (headerSchema != null && !headerSchema.trim().isEmpty()) {
                    resolvedSchema = headerSchema.trim();
                    log.info("🏷️ Using schema from X-Schema header: [{}]", resolvedSchema);
                }
            }

            // ─── 3. Set tenant in TenantContext ──────────────────────────────────
            if (resolvedSchema != null && !resolvedSchema.trim().isEmpty()) {
                TenantContext.setTenantId(resolvedSchema.trim());
                log.info("✅ TenantContext set to schema: [{}]", resolvedSchema);
            } else {
                log.warn("⚠️ No schema resolved from JWT or X-Schema header; using default tenant");
            }

            filterChain.doFilter(request, response);

        } finally {
            // Always clean up thread-local state to prevent tenant leakage
            TenantContext.clear();
        }
    }
}