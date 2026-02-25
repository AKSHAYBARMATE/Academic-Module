package com.academic.config;

import com.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final UserRepository userRepository;

    @Bean
    public FilterRegistrationBean<JwtExtractionFilter> jwtFilter() {
        FilterRegistrationBean<JwtExtractionFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtExtractionFilter(userRepository));
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
