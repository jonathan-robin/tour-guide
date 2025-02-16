package com.openclassrooms.tourguide.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.openclassrooms.tourguide.model.User;
import tripPricer.Provider;
import tripPricer.TripPricer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class TripServiceTest {

    @Autowired
    private TripPricer tripPricer;
    
    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService; 

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = userService.getAllUsers().get(0);
//        testUser.getUserPreferences().setNumberOfAdults(1);
//        testUser.getUserPreferences().setTripDuration(7);
    }

    @Test
    public void testGetTripDeals() {
        Provider provider1 = new Provider(new UUID(1L, 2L), "Provider1", 1D);
        Provider provider2 = new Provider(new UUID(1L, 2L), "Provider2", 2D);
        
        List<Provider> providers = Arrays.asList(provider1, provider2);
        testUser.setTripDeals(providers);

        List<Provider> tripDeals = tripService.getTripDeals(testUser);

        assertNotNull(tripDeals, "La liste des trip deals ne doit pas être nulle.");
        assertTrue(testUser.getTripDeals().size() > 0);
    }

    @Test
    public void testGetTripDealsWithNoRewardPoints() {
        User userNoRewards = userService.getAllUsers().get(2);
        userNoRewards.clearUserRewards();
        userNoRewards.clearVisitedLocations();;
        userNoRewards.clearTripDeals();

        Provider provider1 = new Provider(new UUID(1L, 2L), "Provider1", 1D);
        userNoRewards.setTripDeals(Arrays.asList(provider1));

        List<Provider> tripDeals = tripService.getTripDeals(userNoRewards);

        assertNotNull(tripDeals, "La liste des trip deals ne doit pas être nulle.");
        assertNotNull(userNoRewards.getTripDeals(), "La liste du user des trip deals ne doit pas être nulle.");
    }
}
