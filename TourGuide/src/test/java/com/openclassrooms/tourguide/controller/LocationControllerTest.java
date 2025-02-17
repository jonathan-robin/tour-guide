package com.openclassrooms.tourguide.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.UserService;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
/**
 * Unit tests for the {@link LocationController} class.
 * 
 * <p>This test class verifies the functionality of retrieving a user's location
 * and fetching nearby attractions.</p>
 * 
 * <p>It uses Spring Boot's test context with {@link SpringBootTest} and extends
 * {@link MockitoExtension} for potential mocking needs.</p>
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LocationControllerTest {

    @Autowired
    private LocationController locationController;
    
    @Autowired
    private UserService userService;

    private User testUser;

    /**
     * Initializes test data before each test execution.
     * 
     * <p>Sets up internal test users and retrieves a test user for use in the tests.</p>
     */
    @BeforeEach
    void setUp() {
        InternalTestHelper.setInternalUserNumber(100);
        testUser = userService.getAllUsers().get(0);
    }

    /**
     * Tests the retrieval of a user's location.
     * 
     * <p>Ensures that the {@link LocationController#getLocation(String)} method
     * returns a non-null {@link ResponseEntity} containing a {@link VisitedLocation}.</p>
     */
    @Test
    void testGetLocation() {
        ResponseEntity<VisitedLocation> result = locationController.getLocation(testUser.getUserName());
        assertNotNull(result);
    }

    /**
     * Tests the retrieval of nearby attractions for a user.
     * 
     * <p>Ensures that the {@link LocationController#getNearbyAttractions(String)} method
     * returns exactly five nearby attractions.</p>
     */
    @Test
    void testGetNearbyAttractions() {
        ResponseEntity<List<UserNearByAttractionDto>> result = locationController.getNearbyAttractions(testUser.getUserName());

        assertNotNull(result);
        assertEquals(5, result.getBody().size());
    }
}
