package com.taskrabbit.userservice.model;

/**
 * User role enumeration
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum Role {
    CUSTOMER("Customer"),
    TASKER("Tasker"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
