package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;


import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.config.AsyncConfig;
import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

/**
 * Unit tests for the {@link RewardsService} class.
 * 
 * <p>This test class ensures that user rewards are correctly calculated based on 
 * their proximity to attractions. It validates different scenarios such as earning 
 * new rewards, preventing duplicate rewards, and receiving rewards for visiting 
 * all attractions.</p>
 * 
 * <p>It leverages Spring Boot's test context and dependencies including 
 * {@link LocationService}, {@link TourGuideService}, and {@link UtilsService}.</p>
 */
@SpringBootTest
@DirtiesContext
public class RewardsServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UtilsService utilsService;
    
	@Autowired
	private AsyncConfig config; 

    private User user;
    private Attraction attraction;
    private VisitedLocation visitedLocation;

    /**
     * Initializes test data before each test execution.
     * 
     * <p>Sets up a user and selects an attraction for use in the tests.</p>
     */
    @BeforeEach
    public void setup() { 
        InternalTestHelper.setInternalUserNumber(100);
        user = userService.getAllUsers().get(0);
        attraction = locationService.getAttractions().get(0);
    }
    
//    @AfterEach
//    public void shutdownExecutor() {
//        config.taskExecutor().shutdown();  // Force la fermeture des threads
//    }

    /**
     * Tests if a user earns a reward after visiting an attraction.
     * 
     * <p>The test simulates a user visiting an attraction and verifies that 
     * at least one reward is added.</p>
     */
    @Test
    void testCalculateRewards_UserEarnsReward() {
        locationService.trackUserLocation(user);
        rewardsService.calculateRewards(user, Arrays.asList(attraction));
        user.addUserReward(new UserReward(visitedLocation, attraction, 100)); 
        List<UserReward> rewards = rewardsService.getUserRewards(user);

        assertTrue(rewards.size() > 1);
        assertTrue(rewards.get(0).getRewardPoints() > 1);
    }

    /**
     * Tests if a user does not receive duplicate rewards for the same attraction.
     * 
     * <p>The test adds a reward manually and verifies that calling 
     * {@link RewardsService#calculateRewards(User, List)} does not add duplicate rewards.</p>
     */
    @Test
    void testCalculateRewards_UserAlreadyRewarded() {
        user.addUserReward(new UserReward(visitedLocation, attraction, 100));
        rewardsService.calculateRewards(user, Arrays.asList(attraction));
        List<UserReward> rewards = rewardsService.getUserRewards(user);

        assertEquals(1, rewards.size());
    }

    /**
     * Tests if a user receives at least one reward after visiting an attraction.
     * 
     * <p>This test ensures that a new user visiting an attraction is awarded 
     * a reward.</p>
     */
    @Test
    public void userGetRewards() {
    	TourGuideService tourGuideService = new TourGuideService(rewardsService, locationService, config);
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), locationService.getAttractions().get(0), new Date()));
        
        locationService.trackUserLocation(user);
        rewardsService.calculateRewards(user, locationService.getAttractions());
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();

        assertTrue(userRewards.size() >= 1, "User should have received at least one reward.");
    }

    /**
     * Tests the conversion of a list of attractions to {@link UserNearByAttractionDto} objects.
     * 
     * <p>The test verifies that each attraction is properly converted into a DTO 
     * with the correct attributes.</p>
     */
    @Test
    public void testConvertToUserNearByAttractionDtos() {
        User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");

        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), 
            new gpsUtil.location.Location(34.0522, -118.2437), new Date());

        List<Attraction> attractions = Arrays.asList(
            locationService.getAttractions().get(0), locationService.getAttractions().get(1)
        );

        List<UserNearByAttractionDto> result = rewardsService.convertToUserNearByAttractionDtos(attractions, visitedLocation, user);

        assertEquals(attractions.size(), result.size(), "The number of DTOs should match the number of attractions.");

        for (int i = 0; i < attractions.size(); i++) {
            Attraction attraction = attractions.get(i);
            UserNearByAttractionDto dto = result.get(i);

            assertEquals(attraction.attractionName, dto.getAttractionName(), "The attraction name should match.");
            assertTrue(dto.getDistanceInMiles() >= 0, "Distance should not be negative.");
        }
    }

    /**
     * Tests if a user receives rewards for visiting all available attractions.
     * 
     * <p>This test ensures that when a user visits all attractions, they are rewarded for each one.</p>
     */
    @Test
    public void nearAllAttractions() {
    	TourGuideService tourGuideService = new TourGuideService(rewardsService, locationService, config);

        utilsService.setProximityBuffer(Integer.MAX_VALUE);
        InternalTestHelper.setInternalUserNumber(1);
        
        /* Async version */
        User user = userService.getAllUsers().get(0);
        CompletableFuture<Void> allLocationsTracked = tourGuideService.calculateRewardsAsync(Arrays.asList(user));
        allLocationsTracked.join();
//        config.taskExecutor().destroy();
        tourGuideService.tracker.stopTracking();

        assertEquals(locationService.getAttractions().size(), user.getUserRewards().size(), 
                     "User should have received rewards for all attractions.");
    }
    

    

}
