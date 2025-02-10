package com.openclassrooms.tourguide;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

@Controller
@Slf4j
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
	@Autowired
	UserService userService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(userService.getUser(userName));
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
    @RequestMapping("/getNearbyAttractions")
    public List<UserNearByAttractionDto> getNearbyAttractions(@RequestParam String userName) {
        log.info("call API getNearByAttractions with user: {}", userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
        return tourGuideService.getFiveNearestAttractions(visitedLocation, userService.getUser(userName));
    }

    
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(userService.getUser(userName));
    }
       
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(userService.getUser(userName));
    }

}