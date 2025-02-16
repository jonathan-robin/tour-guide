package com.openclassrooms.tourguide.service;


import static org.junit.jupiter.api.Assertions.*;


import java.util.Arrays;

import java.util.List;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.openclassrooms.tourguide.config.AsyncConfig;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;

import gpsUtil.location.VisitedLocation;


@SpringBootTest
public class LocationServiceTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private GpsUtil gpsUtils;
    
    @MockBean
    private LocationService locationService;

    @BeforeEach
    void setUp() {
 	   InternalTestHelper.setInternalUserNumber(10);
 	   userService.initializeInternalUsers();
       locationService = new LocationService(gpsUtils, new AsyncConfig());
    }

    @Test
    void testGetUserLocation_WithVisitedLocations() {
        User user = userService.getAllUsers().get(0);
        VisitedLocation result = locationService.getUserLocation(user);
        assertNotNull(result);
    }

    @Test
    void testTrackUsersLocationsAsync() throws ExecutionException, InterruptedException {
    	List<User> users = Arrays.asList(userService.getAllUsers().get(0), userService.getAllUsers().get(1));

        users.forEach(user -> locationService.getUserLocation(user));
        List<VisitedLocation> result = locationService.trackUsersLocationsAsync(users);

        assertEquals(users.size(), result.size());

    }
    
    @Test
    void testGetFiveNearestAttractions() {
    	
        User user = userService.getAllUsers().get(0);
        
        List<Attraction> result = locationService.getFiveNearestAttractions(user.getLastVisitedLocation());
        assertEquals(5, result.size());
    }


}
