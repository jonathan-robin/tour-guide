package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class UtilsServiceTest {

    
    @MockBean
    private UtilsService utilsService;

    private Attraction attraction;
	
	 @Test
	    public void testGetDistance() {
	        Location loc1 = new Location(40.748817, -73.985428); 
	        Location loc2 = new Location(40.689247, -74.044502);
	        
	        when(utilsService.getDistance(loc1, loc2)).thenReturn(5.0); 
	        
	        double distance = utilsService.getDistance(loc1, loc2);
	        
	        assertTrue(distance > 0);  
	        assertEquals(5.0, distance, 0.1); 
	    }

	    @Test
	    public void testIsWithinAttractionProximity() {
	    	
	        attraction = new Attraction("test", "test", "null", 40.8584, -73.2945);
	        Location loc1 = new Location(40.748817, -73.885428); 
	        when(utilsService.isWithinAttractionProximity(any(Attraction.class), any(Location.class))).thenReturn(true);
	        assertTrue(utilsService.isWithinAttractionProximity(attraction, loc1));

	    }
	    
	    @Test
	    public void testIsNotWithinAttractionProximity() {
	        Location farLocation = new Location(41.748817, -74.985428); 
	        
	        boolean result = utilsService.isWithinAttractionProximity(attraction, farLocation);
	        
	        assertFalse(result);
	    }
	    
	    
	
}
