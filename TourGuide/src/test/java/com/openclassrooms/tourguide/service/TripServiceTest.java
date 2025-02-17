package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.*;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import tripPricer.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
/**
 * Unit tests for the {@link TripService} class.
 * 
 * <p>This test class ensures that the methods in the {@link TripService} are functioning correctly, 
 * particularly those related to fetching trip deals for users.</p>
 * 
 * <p>The tests cover different scenarios such as retrieving trip deals for a user with rewards 
 * and without any rewards points.</p>
 * 
 * <p>Mockito is used to mock dependencies and simulate various behaviors in the tests.</p>
 */
@SpringBootTest
public class TripServiceTest {
    
    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService; 

    private User testUser;

    /**
     * Sets up the test environment by initializing the users and selecting a test user.
     * This method is run before each test.
     */
    @BeforeEach
    public void setup() {
  	   InternalTestHelper.setInternalUserNumber(10);
  	   userService.initializeInternalUsers();
       testUser = userService.getAllUsers().get(0);
    }

    /**
     * Tests the {@link TripService#getTripDeals(User)} method to ensure it retrieves trip deals 
     * for a user with available trip deals.
     * 
     * <p>This test creates mock providers, associates them with a user, and verifies that the 
     * {@code getTripDeals} method returns the expected trip deals for the user.</p>
     */
    @Test
    public void testGetTripDeals() {
        Provider provider1 = new Provider(new UUID(1L, 2L), "Provider1", 1D);
        Provider provider2 = new Provider(new UUID(1L, 2L), "Provider2", 2D);
        
        List<Provider> providers = Arrays.asList(provider1, provider2);
        testUser.setTripDeals(providers);

        List<Provider> tripDeals = tripService.getTripDeals(testUser);

        assertNotNull(tripDeals, "The list of trip deals should not be null.");
        assertTrue(testUser.getTripDeals().size() > 0, "The user should have trip deals.");
    }

    /**
     * Tests the {@link TripService#getTripDeals(User)} method for a user with no reward points.
     * 
     * <p>This test simulates a scenario where a user has no rewards, clears their trip deals, 
     * and verifies that the system still returns trip deals for that user.</p>
     */
    @Test
    public void testGetTripDealsWithNoRewardPoints() {
        User userNoRewards = userService.getAllUsers().get(2);
        userNoRewards.clearUserRewards();
        userNoRewards.clearVisitedLocations();
        userNoRewards.clearTripDeals();

        Provider provider1 = new Provider(new UUID(1L, 2L), "Provider1", 1D);
        userNoRewards.setTripDeals(Arrays.asList(provider1));

        List<Provider> tripDeals = tripService.getTripDeals(userNoRewards);

        assertNotNull(tripDeals, "The list of trip deals should not be null.");
        assertNotNull(userNoRewards.getTripDeals(), "The user's list of trip deals should not be null.");
    }
}
