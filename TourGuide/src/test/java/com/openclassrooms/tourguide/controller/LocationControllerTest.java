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

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class LocationControllerTest {

    @Autowired
    private LocationController locationController;
    
    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
    	InternalTestHelper.setInternalUserNumber(100);    	
    	testUser = userService.getAllUsers().get(0);
    }

    @Test
    void testGetLocation() {
        ResponseEntity<VisitedLocation> result = locationController.getLocation(testUser.getUserName());
        assertNotNull(result);
    }

    @Test
    void testGetNearbyAttractions() {
        ResponseEntity<List<UserNearByAttractionDto>> result = locationController.getNearbyAttractions(testUser.getUserName());

        assertNotNull(result);
        assertEquals(5, result.getBody().size());
    }
}
