package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.Collections;
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

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	ExecutorService executorService = Executors.newCachedThreadPool();
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

	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
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
