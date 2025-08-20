package com.taskrabbit.userservice.repository;

import com.taskrabbit.userservice.model.Profile;
import com.taskrabbit.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Profile entity
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Find profile by user
     * 
     * @param user the user to search for
     * @return Optional containing profile if found
     */
    Optional<Profile> findByUser(User user);

    /**
     * Find profile by user ID
     * 
     * @param userId the user ID to search for
     * @return Optional containing profile if found
     */
    Optional<Profile> findByUserId(Long userId);

    /**
     * Find profiles by city
     * 
     * @param city the city to search for
     * @return List of profiles in the specified city
     */
    List<Profile> findByCity(String city);

    /**
     * Find profiles by state
     * 
     * @param state the state to search for
     * @return List of profiles in the specified state
     */
    List<Profile> findByState(String state);

    /**
     * Find profiles with minimum rating
     * 
     * @param minRating the minimum rating
     * @return List of profiles with rating >= minRating
     */
    @Query("SELECT p FROM Profile p WHERE p.averageRating >= :minRating ORDER BY p.averageRating DESC")
    List<Profile> findByMinimumRating(@Param("minRating") Double minRating);

    /**
     * Find top-rated profiles
     * 
     * @param limit the number of profiles to return
     * @return List of top-rated profiles
     */
    @Query("SELECT p FROM Profile p WHERE p.averageRating > 0 ORDER BY p.averageRating DESC")
    List<Profile> findTopRatedProfiles();

    /**
     * Find profiles with skills containing specific skill
     * 
     * @param skill the skill to search for
     * @return List of profiles with the specified skill
     */
    @Query("SELECT p FROM Profile p WHERE p.skills LIKE %:skill%")
    List<Profile> findBySkillsContaining(@Param("skill") String skill);

    /**
     * Find profiles within hourly rate range
     * 
     * @param minRate the minimum hourly rate
     * @param maxRate the maximum hourly rate
     * @return List of profiles within the rate range
     */
    @Query("SELECT p FROM Profile p WHERE p.hourlyRate BETWEEN :minRate AND :maxRate")
    List<Profile> findByHourlyRateRange(@Param("minRate") Double minRate, @Param("maxRate") Double maxRate);

    /**
     * Find verified profiles
     * 
     * @param isBackgroundChecked background check status
     * @return List of verified profiles
     */
    List<Profile> findByIsBackgroundChecked(Boolean isBackgroundChecked);

    /**
     * Count profiles with minimum tasks completed
     * 
     * @param minTasks minimum number of tasks
     * @return count of profiles
     */
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.totalTasksCompleted >= :minTasks")
    long countByMinimumTasksCompleted(@Param("minTasks") Integer minTasks);
}
