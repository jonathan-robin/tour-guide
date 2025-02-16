package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.UserService;

import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/locations") 
public class LocationController {

	@Autowired
	private LocationService locationService;
	
	@Autowired
	private TourGuideService tourGuideService;
	
	@Autowired
	private UserService userService;
    
    @GetMapping("") 
    public ResponseEntity<VisitedLocation> getLocation(@RequestParam String userName) {
    	log.info("call API /locations with user: {}", userName);
    	return ResponseEntity.ok(locationService.getUserLocation(userService.getUser(userName)));
    }
    
    /**
     * Endpoint to retrieve the five nearest tourist attractions to a user.
     * This method fetches the user's current location using their username, 
     * and then returns a list of nearby attractions with detailed information for each attraction.
     *
     * @param userName The name of the user whose location will be used to calculate nearby attractions.
     *                 This parameter is retrieved from the HTTP request as a String.
     * @return A list of DTOs (Data Transfer Objects) representing the five nearest attractions to the user.
     *         Each DTO contains the following information:
     *         - Name of the attraction
     *         - Coordinates (latitude and longitude) of the attraction
     *         - Coordinates (latitude and longitude) of the user
     *         - Distance in miles between the user and the attraction
     *         - Reward points for visiting the attraction
     * 
     * @throws UserNotFoundException If the specified user does not exist in the system.
     * @throws LocationNotFoundException If the user's location cannot be determined.
     * @throws ExternalServiceException If an error occurs while fetching data from an external service.
     *
     * @see UserNearByAttractionDto
     * @see TourGuideService
     * @see UserService
     */
    @GetMapping("/getNearbyAttractions")
    public ResponseEntity<List<UserNearByAttractionDto>> getNearbyAttractions(@RequestParam String userName) {
        log.info("call API locations/getNearByAttractions with user: {}", userName);
        VisitedLocation visitedLocation = locationService.getUserLocation(userService.getUser(userName));
        return ResponseEntity.ok(tourGuideService.getUserNearByAttractions(visitedLocation, userService.getUser(userName)));
    }

	
}
