package com.mshando.userservice.model;

/**
 * Alias for Profile entity to maintain compatibility
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public class UserProfile extends Profile {
    
    // Default constructor
    public UserProfile() {
        super();
    }

    // Constructor
    public UserProfile(User user) {
        super(user);
    }
}
