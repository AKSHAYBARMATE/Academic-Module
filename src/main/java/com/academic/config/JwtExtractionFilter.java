package com.academic.config;

import com.academic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
public class JwtExtractionFilter implements Filter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final LoginUser loginUser;

    public JwtExtractionFilter(UserRepository userRepository, LoginUser loginUser) {
        this.userRepository = userRepository;
        this.loginUser = loginUser;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payload = parts[1];
                    String json = new String(Base64.getUrlDecoder().decode(payload));
                    JwtPayload jwtPayload = mapper.readValue(json, JwtPayload.class);

                    loginUser.setUserId(jwtPayload.getUserId());
                    loginUser.setName(jwtPayload.getName());
                    loginUser.setEmail(jwtPayload.getEmail());
                    loginUser.setUserType(jwtPayload.getUserType());

                    // Resolve student_id or staff_code (employeeId)
                    if (jwtPayload.getUserId() != null) {
                        if ("student".equalsIgnoreCase(jwtPayload.getUserType())) {
                            userRepository.findStudentIdByUserId(jwtPayload.getUserId())
                                    .ifPresent(id -> loginUser.setStudentId(id));
                        } else {
                            userRepository.findStaffIdByUserId(jwtPayload.getUserId())
                                    .ifPresent(id -> loginUser.setStaffId(id));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        chain.doFilter(request, response);
    }
}
