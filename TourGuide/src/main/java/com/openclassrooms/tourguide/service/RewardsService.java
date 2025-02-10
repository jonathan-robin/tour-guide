package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
@Slf4j
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
//	ExecutorService executorService = Executors.newCachedThreadPool();
	ExecutorService executorService = Executors.newFixedThreadPool(45);
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	/**
	 * Calculates the rewards for a given user based on their visited locations and the nearby attractions.
	 * 
	 * For each visited location of the user, the method checks if they are near any attraction from the
	 * list of attractions. If the user has not been rewarded for that attraction and they are close enough,
	 * a new reward is created and added to the user's rewards list.
	 * 
	 * The method uses a thread-safe list (`CopyOnWriteArrayList`) to store and modify the rewards, ensuring
	 * that the operation is safe even if accessed by multiple threads concurrently.
	 * 
	 * @param user The user for whom the rewards are being calculated. This user should have a list of visited
	 *             locations and existing user rewards, which will be updated with new rewards if applicable.
	 * 
	 * @see User
	 * @see VisitedLocation
	 * @see Attraction
	 * @see UserReward
	 */
	public void calculateRewards(User user) {
		   CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
		   List<Attraction> attractions = gpsUtil.getAttractions();

		    CopyOnWriteArrayList<UserReward> rewards = new CopyOnWriteArrayList<>(user.getUserRewards());
		    CopyOnWriteArrayList<UserReward> newRewards = new CopyOnWriteArrayList<>();

		    for (VisitedLocation visitedLocation : userLocations) {
		        for (Attraction attraction : attractions) {
		            boolean alreadyRewarded = rewards.stream()
		                .anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

		            if (!alreadyRewarded && nearAttraction(visitedLocation, attraction)) {
		                newRewards.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
		            }
		        }
		    }

		    newRewards.forEach(user::addUserReward);
	}
	
	/**
	 * Asynchronously calculates rewards for a list of users using multiple threads.
	 * For each user in the provided list, this method will calculate their rewards asynchronously 
	 * in parallel. It will execute the reward calculation in separate threads managed by the provided
	 * executor service.
	 * 
	 * The method waits for all the reward calculations to complete by using a {@link CompletableFuture}.
	 * Once all tasks are finished, the returned {@link CompletableFuture<Void>} is completed. If any 
	 * exception occurs during the calculation of rewards for any user, an error message will be logged 
	 * and the calculation for that user will be skipped.
	 * 
	 * @param users The list of users for whom rewards need to be calculated.
	 * @return A {@link CompletableFuture<Void>} that will be completed when all the reward calculations
	 *         for all users are finished.
	 * @throws IllegalArgumentException if the provided list of users is null or empty.
	 * 
	 * @see CompletableFuture
	 * @see RewardsService#calculateRewards(User)
	 */
	public CompletableFuture<Void> calculateRewardsAsync(List<User> users) {
		 
		 List<CompletableFuture<Void>> futures = users.stream()
	        .map((User user) -> CompletableFuture.runAsync(() -> {
	        	try { 
	        		calculateRewards(user);
	        	} catch (Exception ex) {
	        		log.error("Error calculating rewards for user: {}", user.getUserName());
	        	}
	        }, executorService))
	        .collect(Collectors.toList());

	    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

	}

	/**
	 * Gets the list of rewards for the specified user.
	 *
	 * <p>This method returns a copy of the user's rewards list to ensure that the original list is not modified externally.</p>
	 *
	 * @param user The user whose rewards are to be fetched.
	 * @return A list of the user's rewards.
	 */
	public List<UserReward> getUserRewards(User user) {
	    return new CopyOnWriteArrayList<>(user.getUserRewards());
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

	/**
	 * Checks whether the visited location is within the proximity buffer of the attraction.
	 * 
	 * <p>This method calculates the distance between the given visited location and the specified 
	 * attraction, and returns {@code true} if the distance is within the proximity buffer, 
	 * or {@code false} otherwise.</p>
	 * 
	 * @param visitedLocation The visited location to check for proximity to the attraction.
	 * @param attraction The attraction to check the proximity against.
	 * @return {@code true} if the visited location is within the proximity buffer of the attraction, 
	 *         {@code false} otherwise.
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
	    return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	/**
	 * Retrieves the reward points associated with visiting an attraction.
	 * 
	 * <p>This method calls the external {@code rewardsCentral} service to get the reward points 
	 * for the specified user when they visit the specified attraction.</p>
	 * 
	 * @param attraction The attraction for which the reward points are to be retrieved.
	 * @param user The user who visited the attraction and for whom the reward points are to be fetched.
	 * @return The reward points associated with the attraction for the specified user.
	 */
	public int getRewardPoints(Attraction attraction, User user) {
	    return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
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


}
