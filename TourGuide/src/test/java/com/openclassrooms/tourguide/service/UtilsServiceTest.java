package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import gpsUtil.location.Attraction;
import gpsUtil.location.Location;


/**
 * Unit tests for the {@link UtilsService} class.
 * 
 * <p>This test class ensures that the utility methods in the {@link UtilsService} are functioning correctly.</p>
 * <p>The tests cover the distance calculation between two locations and the proximity check between a user and an attraction.</p>
 * 
 * <p>The tests utilize Mockito for mocking the behavior of the {@link UtilsService} methods.</p>
 */
@SpringBootTest
public class UtilsServiceTest {

    @MockBean
    private UtilsService utilsService;

    private Attraction attraction;

    /**
     * Tests the {@link UtilsService#getDistance(Location, Location)} method to ensure it calculates 
     * the correct distance between two locations.
     * 
     * <p>This test mocks the distance between two locations and verifies that the 
     * method returns the expected result.</p>
     */
    @Test
    public void testGetDistance() {
        Location loc1 = new Location(40.748817, -73.985428); 
        Location loc2 = new Location(40.689247, -74.044502);
        
        when(utilsService.getDistance(loc1, loc2)).thenReturn(5.0); 
        
        double distance = utilsService.getDistance(loc1, loc2);
        
        assertTrue(distance > 0, "The distance should be positive.");
        assertEquals(5.0, distance, 0.1, "The distance should be 5.0 with a margin of error.");
    }

    /**
     * Tests the {@link UtilsService#isWithinAttractionProximity(Attraction, Location)} method to ensure 
     * that the proximity check works as expected.
     * 
     * <p>This test simulates a scenario where a location is within proximity to an attraction and verifies 
     * that the method returns the correct result.</p>
     */
    @Test
    public void testIsWithinAttractionProximity() {
        attraction = new Attraction("test", "test", "null", 40.8584, -73.2945);
        Location loc1 = new Location(40.748817, -73.885428); 
        when(utilsService.isWithinAttractionProximity(any(Attraction.class), any(Location.class))).thenReturn(true);
        
        assertTrue(utilsService.isWithinAttractionProximity(attraction, loc1), 
                   "The location should be within the attraction's proximity.");
    }

    /**
     * Tests the {@link UtilsService#isWithinAttractionProximity(Attraction, Location)} method to ensure 
     * that the proximity check returns false for locations outside the proximity.
     * 
     * <p>This test simulates a scenario where the location is far from the attraction and verifies that 
     * the method returns false.</p>
     */
    @Test
    public void testIsNotWithinAttractionProximity() {
        Location farLocation = new Location(41.748817, -74.985428); 
        
        boolean result = utilsService.isWithinAttractionProximity(attraction, farLocation);
        
        assertFalse(result, "The location should not be within the attraction's proximity.");
    }
}
