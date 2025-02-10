package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

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
@Slf4j
public class TestRewardsService {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RewardsService rewardsService;
	
	@Autowired
	private GpsUtil gpsUtil;

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
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        
        log.info("userRewards size{}", userRewards.size());

        /* == is not >= */
        assertTrue(userRewards.size() >= 1, "User should have received at least one reward.");
    }

    /**
     * Tests if an attraction is within the proximity of another attraction.
     * 
     * <p>This test verifies that the {@link RewardsService#isWithinAttractionProximity(Attraction, Attraction)} 
     * method correctly identifies if two attractions are within proximity.</p>
     * 
     * @see RewardsService#isWithinAttractionProximity(Attraction, Attraction)
     */
    @Test
    public void isWithinAttractionProximity() {
        Attraction attraction = gpsUtil.getAttractions().get(0);
        
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction), 
                   "The attraction should be within proximity of itself.");
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
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService, userService);

        rewardsService.calculateRewards(userService.getAllUsers().get(0));
        List<UserReward> userRewards = rewardsService.getUserRewards(userService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtil.getAttractions().size(), userRewards.size(), 
                     "User should have received rewards for all attractions.");
    }
}
