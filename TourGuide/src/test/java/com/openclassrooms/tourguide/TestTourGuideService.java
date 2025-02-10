package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.TripService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import tripPricer.Provider;

/**
 * Test suite for verifying the functionality of the {@link TourGuideService} class.
 * The tests simulate various scenarios related to user management, user tracking,
 * nearby attractions, and trip deals.
 * 
 * <p>These tests utilize the Spring Boot test context and the relevant service classes such as 
 * {@link TourGuideService}, {@link RewardsService}, {@link GpsUtil}, and {@link Provider}.</p>
 * 
 * <p>Each test ensures that the {@link TourGuideService} behaves as expected when interacting with 
 * users and their locations, and when retrieving nearby attractions and trip deals.</p>
 */
@SpringBootTest(classes = TourguideApplication.class)
public class TestTourGuideService {
	
	@Autowired
	UserService userService;
	
	@Autowired
	TripService tripService;

    /**
     * Tests the functionality of retrieving a user's location.
     * 
     * <p>This test simulates the scenario where a user's location is tracked and verifies that
     * the returned location matches the user's ID.</p>
     * 
     * @see TourGuideService#trackUserLocation(User)
     */
    @Test
    public void getUserLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();

        assertTrue(visitedLocation.userId.equals(user.getUserId()), "User's location should match the user ID.");
    }

    /**
     * Tests adding multiple users to the system and retrieving them.
     * 
     * <p>This test simulates the scenario where two users are added to the system, and then
     * verifies that both users can be retrieved by their username.</p>
     * 
     * @see TourGuideService#addUser(User)
     * @see TourGuideService#getUser(String)
     */
    @Test
    public void addUser() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrievedUser = userService.getUser(user.getUserName());
        User retrievedUser2 = userService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrievedUser, "The retrieved user should match the original user.");
        assertEquals(user2, retrievedUser2, "The retrieved second user should match the original second user.");
    }

    /**
     * Tests retrieving all users in the system.
     * 
     * <p>This test verifies that after adding two users, they are both present when retrieving 
     * all users from the system.</p>
     * 
     * @see TourGuideService#getAllUsers()
     */
    @Test
    public void getAllUsers() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = userService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user), "User should be in the list of all users.");
        assertTrue(allUsers.contains(user2), "Second user should also be in the list of all users.");
    }

    /**
     * Tests tracking a user's location.
     * 
     * <p>This test verifies that the tracking process works correctly and that the user's ID
     * matches the ID in the returned location data.</p>
     * 
     * @see TourGuideService#trackUserLocation(User)
     */
    @Test
    public void trackUser() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId, "Tracked user's ID should match the visited location's user ID.");
    }

    /**
     * Tests retrieving nearby attractions for a user.
     * 
     * <p>This test verifies that the {@link TourGuideService#getFiveNearestAttractions(VisitedLocation, User)} 
     * method correctly returns a list of nearby attractions, and checks that the list contains 5 attractions.</p>
     * 
     * @see TourGuideService#getFiveNearestAttractions(VisitedLocation, User)
     */
    @Test
    public void getNearbyAttractions() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        List<UserNearByAttractionDto> attractions = tourGuideService.getFiveNearestAttractions(visitedLocation, user);
        
        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size(), "The user should have 5 nearby attractions.");
    }

    /**
     * Tests retrieving trip deals for a user.
     * 
     * <p>This test verifies that the method {@link TourGuideService#getTripDeals(User)} correctly 
     * retrieves a list of trip providers for the user, with the expected number of providers.</p>
     * 
     * @see TourGuideService#getTripDeals(User)
     */
    public void getTripDeals() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tripService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(10, providers.size(), "The user should have 10 trip providers.");
    }
}
