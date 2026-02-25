package com.academic.config;

public class UserContext {
    private static final ThreadLocal<JwtPayload> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<Object> currentDomainId = new ThreadLocal<>();

    public static void setUser(JwtPayload user) {
        currentUser.set(user);
    }

    public static JwtPayload getUser() {
        return currentUser.get();
    }

    public static void setDomainId(Object domainId) {
        currentDomainId.set(domainId);
    }

    public static Long getStudentId() {
        Object id = currentDomainId.get();
        if (id instanceof Integer)
            return ((Integer) id).longValue();
        if (id instanceof Long)
            return (Long) id;
        return null;
    }

    public static Long getStaffId() {
        Object id = currentDomainId.get();
        if (id instanceof Integer)
            return ((Integer) id).longValue();
        if (id instanceof Long)
            return (Long) id;
        return null;
    }

    public static Long getUserId() {
        return (currentUser.get() != null) ? currentUser.get().getUserId() : null;
    }

    public static String getUserType() {
        return (currentUser.get() != null) ? currentUser.get().getUserType() : null;
    }

    public static void clear() {
        currentUser.remove();
        currentDomainId.remove();
    }
}
