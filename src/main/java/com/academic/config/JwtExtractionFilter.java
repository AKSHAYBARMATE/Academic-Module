package com.academic.config;

import com.academic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Base64;

public class JwtExtractionFilter implements Filter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRepository userRepository;

    public JwtExtractionFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String payload = token.split("\\.")[1];

                String json = new String(Base64.getUrlDecoder().decode(payload));
                JwtPayload jwtPayload = mapper.readValue(json, JwtPayload.class);
                UserContext.setUser(jwtPayload); // 🔥 Store user globally for request

                // Resolve student_id or staff_code (employeeId)
                if (jwtPayload.getUserId() != null) {
                    if ("student".equalsIgnoreCase(jwtPayload.getUserType())) {
                        userRepository.findStudentIdByUserId(jwtPayload.getUserId())
                                .ifPresent(id -> UserContext.setDomainId(id));
                    } else if ("staff".equalsIgnoreCase(jwtPayload.getUserType())
                            || "teacher".equalsIgnoreCase(jwtPayload.getUserType())) {
                        userRepository.findStaffIdByUserId(jwtPayload.getUserId())
                                .ifPresent(id -> UserContext.setDomainId(id));
                    }
                }
            }

            chain.doFilter(request, response);

        } finally {
            UserContext.clear(); // important to avoid thread leaks
        }
    }
}
