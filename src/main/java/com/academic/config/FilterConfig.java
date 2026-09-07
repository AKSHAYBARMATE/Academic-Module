package com.academic.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtExtractionFilter> jwtFilter(JwtExtractionFilter jwtExtractionFilter) {
        FilterRegistrationBean<JwtExtractionFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(jwtExtractionFilter);
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
