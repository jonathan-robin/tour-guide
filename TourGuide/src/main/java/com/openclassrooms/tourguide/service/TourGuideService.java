package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

@Service
@Slf4j
public class TourGuideService {
	
	
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	
	@Autowired
	private GpsUtil gpsUtil;
	
	@Autowired
	private RewardsService rewardsService;
	
//	ExecutorService executorService = Executors.newCachedThreadPool();
	ExecutorService executorService = Executors.newFixedThreadPool(10);
	public final Tracker tracker;
	boolean testMode = true;	
	
	@Autowired
	private UserService userService;

	/**
	 * Constructs a new TourGuideService instance.
	 *
	 * <p>This constructor initializes the TourGuideService with the provided GPS and Rewards services. 
	 * It also sets the default locale to US and initializes the internal users in test mode. 
	 * A tracker is created to monitor user locations, and a shutdown hook is added to clean up resources when the application stops.</p>
	 *
	 * @param gpsUtil The GPS utility used to get the location data.
	 * @param rewardsService The rewards service used to manage user rewards.
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
	    this.gpsUtil = gpsUtil;
	    this.rewardsService = rewardsService;
	    this.userService = userService;
	    
	    Locale.setDefault(Locale.US);

	    if (testMode) {
	        logger.info("TestMode enabled");
	        logger.debug("Initializing users");
	        userService.initializeInternalUsers();
	        logger.debug("Finished initializing users");
	    }
	    tracker = new Tracker(this);
	    addShutDownHook();
	}



	/**
	 * Retrieves the current location of the specified user.
	 *
	 * <p>If the user has visited locations, the last visited location is returned; otherwise, the method tracks the user's location and returns it.</p>
	 *
	 * @param user The user whose location is to be fetched.
	 * @return The current visited location of the user.
	 */
	public VisitedLocation getUserLocation(User user) {
	    VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
	            : trackUserLocation(user);
	    return visitedLocation;
	}


	/**
	 * Adds a user to the internal user map.
	 *
	 * <p>This method adds a user to the system only if they are not already present. 
	 * If the user is not already in the internal user map, they are added.</p>
	 *
	 * @param user The user to be added to the system.
	 */
	public void addUser(User user) {
	    if (!userService.userMap.containsKey(user.getUserName())) {
	    	userService.userMap.put(user.getUserName(), user);
	    }
	}



	/**
	 * Tracks the location of a user and calculates the associated rewards.
	 * <p>
	 * This method uses the GPS service to get the user's current location,
	 * then adds this location to the user's visited locations list. After that,
	 * it calculates the rewards for the user through the rewards service.
	 * 
	 * @param user The user whose location needs to be tracked.
	 * @return The visited location of the user as a {@link VisitedLocation}.
	 * @throws Exception If an issue occurs while retrieving the location or calculating the rewards.
	 */
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	/**
	 * Tracks the locations of a list of users asynchronously. For each user, it fetches their visited location
	 * and adds it to a thread-safe list of visited locations. If an error occurs during the tracking process, it is logged.
	 * 
	 * This method ensures that all user location tracking tasks are completed before returning the list of visited locations.
	 * 
	 * @param users The list of users whose locations are to be tracked.
	 * @return A thread-safe list of {@link VisitedLocation} objects containing the visited locations for each user.
	 *         The list is populated asynchronously as the tracking tasks are completed.
	 * @throws java.util.concurrent.ExecutionException If one of the asynchronous tasks throws an exception that is not handled.
	 * @throws java.lang.InterruptedException If the current thread is interrupted while waiting for the completion of tasks.
	 */
	public List<VisitedLocation> trackUsersLocationsAsync(List<User> users) {
		
		List<VisitedLocation> visitedLocations = Collections.synchronizedList(new ArrayList<>());
		 
		 List<CompletableFuture<Void>> futures = users.stream()
	        .map(user -> CompletableFuture.supplyAsync(() -> trackUserLocation(user), executorService)
	            .thenAccept(visitedLocation -> {
	            	visitedLocations.add(visitedLocation);
	            })
	            .exceptionally(ex -> {
                	log.warn("Error tracking location for user {}: {}", user.getUserName(), ex.getMessage());
                	return null;
	            }))
	        .collect(Collectors.toList());

	    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

	    return visitedLocations;
	}
	/**
	 * Retrieves the five nearest tourist attractions to the user's current location and calculates 
	 * the reward points for each attraction.
	 * 
	 * This method performs the following tasks in two main steps:
	 * 1. It calculates the distance from the user's location to each attraction in parallel.
	 * 2. It then calculates the reward points for each attraction in parallel and returns a list of DTOs 
	 *    (Data Transfer Objects) containing details of the nearest attractions.
	 * 
	 * @param visitedLocation The location of the user, including latitude and longitude.
	 * @param user The user whose nearest attractions and reward points are being calculated.
	 * @return A list of DTOs containing the details of the five nearest attractions, including:
	 *         - Name of the attraction
	 *         - Coordinates (latitude and longitude) of the attraction
	 *         - Coordinates (latitude and longitude) of the user's location
	 *         - Distance (in miles) between the user and each attraction
	 *         - Reward points for visiting each attraction
	 * 
	 * @throws Exception If there is any error in processing the attractions, calculating the distance, 
	 *                   or retrieving reward points.
	 * 
	 * @see UserNearByAttractionDto
	 * @see RewardsService
	 * @see Attraction
	 * @see User
	 */
	public List<UserNearByAttractionDto> getFiveNearestAttractions(VisitedLocation visitedLocation, User user) {

	    List<Attraction> attractions = gpsUtil.getAttractions();

	    /* step 1 : Calculate distances to all attractions asynchronously */
	    List<CompletableFuture<Pair<Attraction, Double>>> futureDistances = attractions.stream()
	        .map(attraction -> CompletableFuture.supplyAsync(() -> {
	            try {
	                /* Calculate the distance from the user to the attraction */
	                double distance = rewardsService.getDistance(attraction, visitedLocation.location);
	                return Pair.of(attraction, distance);
	            } catch (Exception ex) {
	                log.error("Error processing attraction {}: {}", attraction.attractionName, ex.getMessage());
	                return Pair.of(attraction, Double.MAX_VALUE); /* Return a max distance in case of error - filter out errors */
	            }
	        }, executorService))
	        .collect(Collectors.toList());

	    /* Collect and sort the attractions by distance, keeping only the 5 closest attractions */
	    List<Pair<Attraction, Double>> attractionDistances = futureDistances.stream()
	        .map(CompletableFuture::join)
	        .sorted(Comparator.comparing(Pair::getRight))
	        .limit(5)  /* Keep the 5 nearest attractions */
	        .collect(Collectors.toList());

	    /* step 2 : Calculate reward points for each attraction asynchronously */
	    List<CompletableFuture<UserNearByAttractionDto>> futureDtos = attractionDistances.stream()
	        .map(pair -> CompletableFuture.supplyAsync(() -> {
	            try {
	                Attraction attraction = pair.getLeft();
	                double distance = pair.getRight();
	                /* Calculate the reward points for the user visiting this attraction */
	                int rewardPoints = rewardsService.getRewardPoints(attraction, user);

	                return new UserNearByAttractionDto(
	                        attraction.attractionName, 
	                        attraction.latitude, attraction.longitude,
	                        visitedLocation.location.latitude, visitedLocation.location.longitude,
	                        distance, rewardPoints
	                );
	            } catch (Exception ex) {
	                log.error("Error processing attraction {}: {}", pair.getLeft().attractionName, ex.getMessage());
	                return new UserNearByAttractionDto(
	                        pair.getLeft().attractionName, 
	                        pair.getLeft().latitude, pair.getLeft().longitude,
	                        visitedLocation.location.latitude, visitedLocation.location.longitude,
	                        pair.getRight(), 0
	                );
	            }
	        }, executorService))
	        .collect(Collectors.toList());

	    /** Wait for all asynchronous tasks to complete and return the results */
	    return futureDtos.stream().map(CompletableFuture::join).collect(Collectors.toList());
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}




}
