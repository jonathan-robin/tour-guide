package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.config.AsyncConfig;
import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

@Service
@Slf4j
public class RewardsService {

	@Autowired
	private UtilsService utilsService;
	
    private final ThreadPoolTaskExecutor executorService;
	
	private final RewardCentral rewardsCentral;
	
	public RewardsService(RewardCentral rewardCentral, AsyncConfig config) {
		this.rewardsCentral = rewardCentral;
		this.executorService = config.taskExecutor();
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
	public void calculateRewards(User user, List<Attraction> attractions) {
		
		log.info("Rewards : " + user.getUserId() + " - Thread: " + Thread.currentThread().getName());
		/* maybe not needed userLocation COPY */
		   	List<VisitedLocation> location = new ArrayList<>(user.getVisitedLocations());
//		    CopyOnWriteArrayList<UserReward> rewards = new CopyOnWriteArrayList<>(user.getUserRewards());
//		    CopyOnWriteArrayList<UserReward> newRewards = new CopyOnWriteArrayList<>();
  
		    
		    for (VisitedLocation visitedLocation : location) {
		    	
		    	for (Attraction attraction : attractions) {
		    		
		    		if (!user.getUserRewards().stream().anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))
		    				&& utilsService.nearAttraction(visitedLocation, attraction)) {
		    			user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
		    		}
		    	}
		    }

//		    newRewards.forEach(user::addUserReward);
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
	    return user.getUserRewards();
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


	public List<UserNearByAttractionDto> convertToUserNearByAttractionDtos(List<Attraction> attractions, VisitedLocation visitedLocation, User user) {
	
	    List<CompletableFuture<UserNearByAttractionDto>> futureDtos = attractions.stream()
	        .map(attraction -> CompletableFuture.supplyAsync(() -> {
	            try {
	                double distance = utilsService.getDistance(attraction, visitedLocation.location);
	                int rewardPoints = getRewardPoints(attraction, user);
	
	                return new UserNearByAttractionDto(
	                        attraction.attractionName,
	                        attraction.latitude, attraction.longitude,
	                        visitedLocation.location.latitude, visitedLocation.location.longitude,
	                        distance, rewardPoints
	                );
	            } catch (Exception ex) {
	                log.error("Error processing attraction {}: {}", attraction.attractionName, ex.getMessage());
	                return new UserNearByAttractionDto(
	                        attraction.attractionName,
	                        attraction.latitude, attraction.longitude,
	                        visitedLocation.location.latitude, visitedLocation.location.longitude,
	                        Double.MAX_VALUE, 0
	                );
	            }
	        }, executorService))
	        .collect(Collectors.toList());
	
	    return futureDtos.stream().map(CompletableFuture::join).collect(Collectors.toList());
	}


}
