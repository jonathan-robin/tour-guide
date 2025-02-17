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

/**
 * Unit tests for the {@link LocationService} class.
 * 
 * <p>This test class verifies the functionality of user location tracking, 
 * retrieving user locations, and finding the five nearest attractions.</p>
 * 
 * <p>It uses Spring Boot's test context with {@link SpringBootTest} and 
 * mocks dependencies where necessary.</p>
 */
@SpringBootTest
public class LocationServiceTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private GpsUtil gpsUtils;
    
    @MockBean
    private LocationService locationService;

    /**
     * Initializes test data before each test execution.
     * 
     * <p>Sets up internal test users and initializes the {@link LocationService}.</p>
     */
    @BeforeEach
    void setUp() {
        InternalTestHelper.setInternalUserNumber(10);
        userService.initializeInternalUsers();
        locationService = new LocationService(gpsUtils, new AsyncConfig(), new UtilsService());
    }

    /**
     * Tests retrieving a user's location when they have visited locations.
     * 
     * <p>Ensures that a non-null {@link VisitedLocation} is returned for a test user.</p>
     */
    @Test
    void testGetUserLocation_WithVisitedLocations() {
        User user = userService.getAllUsers().get(0);
        VisitedLocation result = locationService.getUserLocation(user);
        assertNotNull(result);
    }

    /**
     * Tests asynchronous tracking of multiple users' locations.
     * 
     * <p>Ensures that the number of tracked locations matches the number of users.</p>
     * 
     * @throws ExecutionException   If an exception occurs during asynchronous execution.
     * @throws InterruptedException If the asynchronous process is interrupted.
     */
    @Test
    void testTrackUsersLocationsAsync() throws ExecutionException, InterruptedException {
        List<User> users = Arrays.asList(userService.getAllUsers().get(0), userService.getAllUsers().get(1));

        users.forEach(user -> locationService.getUserLocation(user));
        List<VisitedLocation> result = locationService.trackUsersLocationsAsync(users);

        assertEquals(users.size(), result.size());
    }

    /**
     * Tests retrieving the five nearest attractions for a given location.
     * 
     * <p>Ensures that exactly five attractions are returned for a test user's last visited location.</p>
     */
    @Test
    void testGetFiveNearestAttractions() {
        User user = userService.getAllUsers().get(0);
        List<Attraction> result = locationService.getFiveNearestAttractions(user.getLastVisitedLocation());
        assertEquals(5, result.size());
    }
}

