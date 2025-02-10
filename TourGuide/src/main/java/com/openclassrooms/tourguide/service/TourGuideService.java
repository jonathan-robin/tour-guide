package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
@Slf4j
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
//	ExecutorService executorService = Executors.newCachedThreadPool();
	ExecutorService executorService = Executors.newFixedThreadPool(10);
	public final Tracker tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return new CopyOnWriteArrayList<>(user.getUserRewards());
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
	
		return internalUserMap.values().stream().collect(Collectors.toList());

	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
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

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
