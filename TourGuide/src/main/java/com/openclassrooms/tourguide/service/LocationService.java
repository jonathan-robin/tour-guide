package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.model.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class LocationService {
	
	@Autowired
	private GpsUtil gpsUtil;
//	
//	@Autowired
//	private RewardsService rewardsService;
	
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

   
	private int attractionProximityRange = 200;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	public LocationService(GpsUtil gpsUtil) { 
		this.gpsUtil = gpsUtil;
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
	
	public List<Attraction> getAttractions(){ 
		return gpsUtil.getAttractions();
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
	public List<Attraction> getFiveNearestAttractions(VisitedLocation visitedLocation) {

	    List<Attraction> attractions = gpsUtil.getAttractions();

	    /* Calculer les distances aux attractions de manière asynchrone */
	    List<CompletableFuture<Pair<Attraction, Double>>> futureDistances = attractions.stream()
	        .map(attraction -> CompletableFuture.supplyAsync(() -> {
	            try {
	                double distance = getDistance(attraction, visitedLocation.location);
	                return Pair.of(attraction, distance);
	            } catch (Exception ex) {
	                log.error("Error processing attraction {}: {}", attraction.attractionName, ex.getMessage());
	                return Pair.of(attraction, Double.MAX_VALUE);
	            }
	        }, executorService))
	        .collect(Collectors.toList());

	    /* Trier et récupérer les 5 attractions les plus proches */
	    return futureDistances.stream()
	        .map(CompletableFuture::join)
	        .sorted(Comparator.comparing(Pair::getRight))
	        .limit(5)
	        .map(Pair::getLeft)
	        .collect(Collectors.toList());
	}
	/**
	 * Calculates the distance between two locations using the Haversine formula.
	 * 
	 * <p>This method calculates the great-circle distance between two locations, expressed 
	 * in statute miles, based on their latitude and longitude.</p>
	 * 
	 * @param loc1 The first location.
	 * @param loc2 The second location.
	 * @return The distance between the two locations in statute miles.
	 */
	public double getDistance(Location loc1, Location loc2) {
	    double lat1 = Math.toRadians(loc1.latitude);
	    double lon1 = Math.toRadians(loc1.longitude);
	    double lat2 = Math.toRadians(loc2.latitude);
	    double lon2 = Math.toRadians(loc2.longitude);

	    double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
	                            + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

	    double nauticalMiles = 60 * Math.toDegrees(angle);
	    double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	    return statuteMiles;
	}
	
	/**
	 * Checks whether the given location is within the proximity range of an attraction.
	 * 
	 * <p>This method calculates the distance between the specified attraction and the location 
	 * and returns {@code true} if the distance is within the proximity range of the attraction, 
	 * or {@code false} otherwise.</p>
	 * 
	 * @param attraction The attraction to check the proximity against.
	 * @param location The location to check for proximity to the attraction.
	 * @return {@code true} if the location is within the attraction's proximity range, 
	 *         {@code false} otherwise.
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
	    return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}



	
}
