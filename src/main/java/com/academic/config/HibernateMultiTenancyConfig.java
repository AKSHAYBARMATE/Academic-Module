package com.academic.config;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateMultiTenancyConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            MultiTenantConnectionProvider multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver tenantIdentifierResolver
    ) {

        return (Map<String, Object> properties) -> {

            // Enable schema-based multi tenancy
            properties.put("hibernate.multiTenancy", "SCHEMA");

            // connection provider
            properties.put(
                    AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                    multiTenantConnectionProvider
            );

            // tenant resolver
            properties.put(
                    AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                    tenantIdentifierResolver
            );
        };
    }
}