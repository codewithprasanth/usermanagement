package com.sprintap.usermanagement.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Constants related to Keycloak operations and role/privilege management.
 * Values are loaded from application.properties for easy configuration.
 */
@Component
public class KeycloakConstants {

    private static String rolePrefix;
    private static String privilegePrefix;
    private static Integer defaultPageSize;
    private static Integer maxPageSize;

    /**
     * Prefix for role names in Keycloak.
     * Configured via app.role-prefix property.
     */
    public static String ROLE_PREFIX;

    /**
     * Prefix for privilege names in Keycloak.
     * Configured via app.privilege-prefix property.
     */
    public static String PRIVILEGE_PREFIX;

    /**
     * Length of the role prefix for string manipulation.
     */
    public static int ROLE_PREFIX_LENGTH;

    /**
     * Length of the privilege prefix for string manipulation.
     */
    public static int PRIVILEGE_PREFIX_LENGTH;

    /**
     * HTTP header name for Location header used in resource creation.
     */
    public static final String LOCATION_HEADER = "Location";

    /**
     * Default page size for paginated requests.
     * Configured via app.pagination.default-page-size property.
     */
    public static int DEFAULT_PAGE_SIZE;

    /**
     * Maximum page size for paginated requests.
     * Configured via app.pagination.max-page-size property.
     */
    public static int MAX_PAGE_SIZE;

    @Value("${app.role-prefix}")
    public void setRolePrefix(String value) {
        rolePrefix = value;
        ROLE_PREFIX = value;
        ROLE_PREFIX_LENGTH = value.length();
    }

    @Value("${app.privilege-prefix}")
    public void setPrivilegePrefix(String value) {
        privilegePrefix = value;
        PRIVILEGE_PREFIX = value;
        PRIVILEGE_PREFIX_LENGTH = value.length();
    }

    @Value("${app.pagination.default-page-size}")
    public void setDefaultPageSize(Integer value) {
        defaultPageSize = value;
        DEFAULT_PAGE_SIZE = value;
    }

    @Value("${app.pagination.max-page-size}")
    public void setMaxPageSize(Integer value) {
        maxPageSize = value;
        MAX_PAGE_SIZE = value;
    }
}

