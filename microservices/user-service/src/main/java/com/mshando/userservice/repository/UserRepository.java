package com.mshando.userservice.repository;

import com.mshando.userservice.model.User;
import com.mshando.userservice.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * 
     * @param username the username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * 
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     * 
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Search users by username or email containing query
     * 
     * @param username the username query
     * @param email the email query
     * @param pageable pagination parameters
     * @return Page of users matching the criteria
     */
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String email, Pageable pageable);

    /**
     * Check if username exists
     * 
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * 
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by verification token
     * 
     * @param token the verification token
     * @return Optional containing user if found
     */
    Optional<User> findByVerificationToken(String token);

    /**
     * Find user by reset password token
     * 
     * @param token the reset password token
     * @return Optional containing user if found
     */
    Optional<User> findByResetPasswordToken(String token);

    /**
     * Find users by role
     * 
     * @param role the role to search for
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find verified users by role
     * 
     * @param role the role to search for
     * @param isVerified verification status
     * @return List of verified users with the specified role
     */
    List<User> findByRoleAndIsVerified(Role role, Boolean isVerified);

    /**
     * Find active users by role
     * 
     * @param role the role to search for
     * @param isActive active status
     * @return List of active users with the specified role
     */
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    /**
     * Find users by city (through profile)
     * 
     * @param city the city to search for
     * @return List of users in the specified city
     */
    @Query("SELECT u FROM User u JOIN u.profile p WHERE p.city = :city")
    List<User> findByCity(@Param("city") String city);

    /**
     * Find users within a radius of given coordinates
     * 
     * @param latitude the latitude
     * @param longitude the longitude
     * @param radius the radius in kilometers
     * @return List of users within the radius
     */
    @Query("SELECT u FROM User u JOIN u.profile p WHERE " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(p.latitude)))) <= :radius")
    List<User> findUsersWithinRadius(@Param("latitude") Double latitude, 
                                   @Param("longitude") Double longitude, 
                                   @Param("radius") Double radius);

    /**
     * Count users by role
     * 
     * @param role the role to count
     * @return count of users with the specified role
     */
    long countByRole(Role role);

    /**
     * Count verified users
     * 
     * @param isVerified verification status
     * @return count of verified users
     */
    long countByIsVerified(Boolean isVerified);
}
