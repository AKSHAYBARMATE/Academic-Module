package com.academic.config;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    public static final String DEFAULT_TENANT = "schoolerp";

    private TenantContext() {
    }

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        String tenantId = CURRENT_TENANT.get();
        return (tenantId == null || tenantId.trim().isEmpty()) ? DEFAULT_TENANT : tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
