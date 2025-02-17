package com.openclassrooms.tourguide.application;

import com.openclassrooms.tourguide.config.AsyncConfig;
import com.openclassrooms.tourguide.dto.UserNearByAttractionDto;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.UtilsService;
import com.openclassrooms.tourguide.tracker.Tracker;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import jakarta.annotation.PreDestroy;

@Service
@Slf4j
public class TourGuideService {

	private final RewardsService rewardsService;
    private final LocationService locationService; 
//	private final UtilsService utilsService;
    private final ThreadPoolTaskExecutor executorService;
	public final Tracker tracker;	


	/**
	 * Constructs a new TourGuideService instance.
	 *
	 * <p>This constructor initializes the TourGuideService with the provided GPS and Rewards services. 
	 * It also sets the default locale to US and initializes the internal users in test mode. 
	 * A tracker is created to monitor user locations, and a shutdown hook is added to clean up resources when the application stops.</p>
	 *
	 * @param gpsUtil The GPS utility used to get the location data.
	 * @param rewardsService The rewards service used to manage user rewards.
	 * @param asyncConfig The multi-threading config used to manage threads pool.
	 */
	public TourGuideService(RewardsService rewardsService, LocationService locationService, AsyncConfig config) {
	    this.rewardsService = rewardsService;
	    this.locationService = locationService;
	    this.executorService = config.taskExecutor();
	    
        log.info("TourGuideService initialized with LocationService: {}", locationService);
	    
	    System.out.println(locationService);
	    
	    Locale.setDefault(Locale.US);
	    tracker = new Tracker(this);
	    addShutDownHook();
	}

	public List<UserNearByAttractionDto> getUserNearByAttractions(VisitedLocation visitedLocation, User user) {

	    CompletableFuture<List<Attraction>> nearestAttractionsFuture = CompletableFuture.supplyAsync(
	        () -> locationService.getFiveNearestAttractions(visitedLocation), executorService);

	    List<Attraction> nearestAttractions = nearestAttractionsFuture.join();

	    CompletableFuture<List<UserNearByAttractionDto>> dtosFuture = CompletableFuture.supplyAsync(
	        () -> rewardsService.convertToUserNearByAttractionDtos(nearestAttractions, visitedLocation, user), executorService);

	    return dtosFuture.join();
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
	@Async
	public CompletableFuture<Void> trackUsersLocationsAsync(List<User> users) {
		 
		List<CompletableFuture<Void>> futuresLocation = users.parallelStream()
			 .map(user -> CompletableFuture.supplyAsync(() -> {
		            VisitedLocation visitedLocation = locationService.trackUserLocation(user);
		            return visitedLocation;
		        }, executorService)

	        .thenAccept(visitedLocation -> {
	            rewardsService.calculateRewards(user, locationService.getAttractions());
	        })
            .exceptionally(ex -> {
            	log.warn("Error tracking location for user {}: {}", user.getUserName(), ex.getMessage());
            	return null;
            }))
        .collect(Collectors.toList());

	    CompletableFuture<Void> allLocationsTracked = CompletableFuture.allOf(futuresLocation.toArray(new CompletableFuture[0]));
	    return allLocationsTracked;

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
	@Async
	public CompletableFuture<Void> calculateRewardsAsync(List<User> users) {
		
		List<Attraction> attractions = locationService.getAttractions();
		log.info("Starting calculate Rewards for {} user(s).", users.size());
		 List<CompletableFuture<Void>> futures = users.stream()
	        .map((User user) -> CompletableFuture.runAsync(() -> {
	        	try { 
	        		rewardsService.calculateRewards(user, attractions);
	        	} catch (Exception ex) {
	        		log.error("Error calculating rewards for user: {}", user.getUserName());
	        	}
	        }, executorService))
	        .collect(Collectors.toList());

	    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

	}

    @PreDestroy
    public void cleanup() {
        System.out.println("Cleaning up and shutting down the executor...");

        if (executorService != null) {
        	executorService.shutdown();
            try {
                if (!executorService.getThreadPoolExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
                	executorService.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
            	executorService.getThreadPoolExecutor().shutdownNow();
            }
        }
    }
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

}
