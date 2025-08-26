package com.mshando.userservice.model.enums;

/**
 * User role enumeration
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum UserRole {
    CUSTOMER("Customer"),
    TASKER("Tasker"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
