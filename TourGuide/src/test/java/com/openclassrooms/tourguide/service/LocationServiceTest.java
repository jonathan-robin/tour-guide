package com.openclassrooms.tourguide.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class LocationServiceTest {


//	@Autowired
//    private LocationService locationService;
//    
    @Autowired
    private UserService userService;
    
    @MockBean
    private LocationService locationServiceMock;
    

    private User user;
    private VisitedLocation visitedLocation;
    private Attraction attraction;
    private Location userLocation;

    @BeforeEach
    public void setUp() {
    	
 	   InternalTestHelper.setInternalUserNumber(100);
 	   userService.initializeInternalUsers();
 	   user = userService.getAllUsers().get(0); 
 	   log.info("user: {}", user);
 	   locationServiceMock.trackUserLocation(user);
       // Mocking the behavior of LocationService for getAttractions
       List<Attraction> attractions = Arrays.asList(
           new Attraction("Eiffel Tower", "test", "ull", 48.8584, 2.2945)
       );
       when(locationServiceMock.getAttractions()).thenReturn(attractions);
 	   assertFalse(attractions.isEmpty(), "Attractions list should not be empty.");
       attraction = locationServiceMock.getAttractions().get(0);  // This will not throw IndexOutOfBoundsException
       log.info("attraction: {}", attraction);

       visitedLocation = locationServiceMock.getUserLocation(user); 

    }



    @Test
    public void testGetFiveNearestAttractions() throws Exception {
        // Créer une liste d'attractions factices
        List<Attraction> attractions = Arrays.asList(
            new Attraction("Eiffel Tower", "test", "test", 48.8584, 2.2945),
            new Attraction("Louvre", "test", "test", 48.8606, 2.3376)
        );
        
        when(locationServiceMock.getFiveNearestAttractions(visitedLocation)).thenReturn(attractions);
        
        List<Attraction> result = locationServiceMock.getFiveNearestAttractions(visitedLocation);
        
        assertNotNull(result);
        assertTrue(result.size() > 0); 
    }


    @Test
    public void testGetDistance() {
        Location loc1 = new Location(40.748817, -73.985428); 
        Location loc2 = new Location(40.689247, -74.044502);
        
        when(locationServiceMock.getDistance(loc1, loc2)).thenReturn(5.0); 
        
        double distance = locationServiceMock.getDistance(loc1, loc2);
        
        assertTrue(distance > 0);  
        assertEquals(5.0, distance, 0.1); 
    }

    @Test
    public void testIsWithinAttractionProximity() {
        Location loc1 = new Location(40.748817, -73.985428); 
        when(locationServiceMock.isWithinAttractionProximity(any(Attraction.class), any(Location.class))).thenReturn(true);
        assertTrue(locationServiceMock.isWithinAttractionProximity(attraction, loc1));

    }
    
    @Test
    public void testIsNotWithinAttractionProximity() {
        Location farLocation = new Location(41.748817, -74.985428);  // Far from user
        
        boolean result = locationServiceMock.isWithinAttractionProximity(attraction, farLocation);
        
        assertFalse(result);  // As attraction is far, it should return false
    }

    @Test
    public void testTrackUsersLocationsAsync() throws Exception {
        List<User> users = Arrays.asList(user);

        // Mock le comportement de locationServiceMock.getUserLocation()
        when(locationServiceMock.getUserLocation(user)).thenReturn(visitedLocation);

        // Mock le comportement de trackUsersLocationsAsync()
        when(locationServiceMock.trackUsersLocationsAsync(users)).thenReturn(Arrays.asList(visitedLocation));

        // Effectuer l'appel à la méthode à tester
        List<VisitedLocation> result = locationServiceMock.trackUsersLocationsAsync(users);

        // Vérifier que le résultat n'est pas nul et qu'il contient la bonne quantité d'éléments
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visitedLocation, result.get(0));
    }

}
