package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.service.UtilsService;

/**
 * Test suite for verifying the functionality of the {@link RewardsService} class.
 * The tests simulate various scenarios related to user rewards, proximity to attractions,
 * and the ability to calculate rewards for a user.
 * 
 * <p>These tests utilize the Spring Boot test context and the relevant service classes such as 
 * {@link RewardsService}, {@link TourGuideService}, and {@link GpsUtil}.</p>
 * 
 * <p>Each test ensures that the reward system correctly associates users with rewards based on
 * their visited locations and the proximity to attractions.</p>
 */

@SpringBootTest
public class TestRewardsService {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RewardsService rewardsService;

	@Autowired
	private LocationService locationService;

	@Autowired 
	private TourGuideService tourGuideService;
	
	@Autowired
	private UtilsService utilsService;
	
	@Autowired
	private GpsUtil gpsUtils;


    /**
     * Tests that a user receives rewards based on their visited location.
     * 
     * <p>This test simulates the scenario where a user visits an attraction and verifies 
     * that the user receives at least one reward.</p>
     * 
     * @see RewardsService#addReward(User, Attraction)
     * @see TourGuideService#trackUserLocation(User)
     */
    @Test
    public void userGetRewards() {
        InternalTestHelper.setInternalUserNumber(0);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), locationService.getAttractions().get(0), new Date()));
        
        locationService.trackUserLocation(user);
        rewardsService.calculateRewards(user, locationService.getAttractions());
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        userRewards.get(0).setRewardPoints(0);
        userRewards.get(0).getRewardPoints();

        assertTrue(userRewards.size() >= 1, "User should have received at least one reward.");
    }
    
    @Test
    public void testConvertToUserNearByAttractionDtos() {
        User user = new User(UUID.randomUUID(), "testUser", "000", "test@tourGuide.com");

        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), 
            new gpsUtil.location.Location(34.0522, -118.2437), new Date());

        List<Attraction> attractions = Arrays.asList(
        		locationService.getAttractions().get(0), locationService.getAttractions().get(1)
        );

        List<UserNearByAttractionDto> result = rewardsService.convertToUserNearByAttractionDtos(attractions, visitedLocation, user);

        assertEquals(attractions.size(), result.size(), "Le nombre de DTOs retournés doit être égal au nombre d'attractions.");

        for (int i = 0; i < attractions.size(); i++) {
            Attraction attraction = attractions.get(i);
            UserNearByAttractionDto dto = result.get(i);

            assertEquals(attraction.attractionName, dto.getAttractionName(), "Le nom de l'attraction doit correspondre.");
            assertTrue(dto.getDistanceInMiles() >= 0, "La distance ne doit pas être négative.");
        }
    }



    /**
     * Tests if the user is rewarded for visiting all attractions in the system.
     * 
     * <p>This test verifies that when a user visits all attractions, they receive a reward for each one.
     * The {@link RewardsService#calculateRewards(User)} method is invoked to calculate rewards for the user,
     * and the number of rewards is compared to the number of attractions in the system.</p>
     * 
     * @see RewardsService#calculateRewards(User)
     */
    @Test
    public void nearAllAttractions() {
    	utilsService.setProximityBuffer(Integer.MAX_VALUE);
        InternalTestHelper.setInternalUserNumber(1);
        
        /* first version of calculate rewards */
        tourGuideService.calculateRewards(userService.getAllUsers().get(0));
		/* async version */
        User user = userService.getAllUsers().get(0);
        CompletableFuture<Void> allLocationsTracked = tourGuideService.calculateRewardsAsync(Arrays.asList(user));
	    allLocationsTracked.join();
	    tourGuideService.tracker.stopTracking();

        assertEquals(locationService.getAttractions().size(), user.getUserRewards().size(), 
                     "User should have received rewards for all attractions.");
    }
}
