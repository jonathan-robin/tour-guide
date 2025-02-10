package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

@Slf4j
public class TestPerformance {



	/**
	 * 	
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * <p>Tests the high volume tracking of user locations within a specified time frame.</p>
	 * 
	 * This test simulates tracking the locations of a large number of users (up to 100,000 users) asynchronously using the 
	 * {@link TourGuideService}. The time taken to track all user locations is measured using a {@link StopWatch}. 
	 * The test ensures that the tracking process completes within 15 minutes.
	 * 
	 * @throws Exception If any error occurs during the test execution.
	 */
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(50000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
	    
		tourGuideService.trackUsersLocationsAsync(allUsers);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	
	/**
	 * This test method simulates the process of calculating rewards for a large volume of users (up to 100,000) 
	 * in a high-load scenario. It measures the time taken for calculating rewards and verifies that each user 
	 * has received at least one reward after the calculation process.
	 * 
	 * The test does the following:
	 * <ol>
	 *   <li>Initializes the required services (GPS, Rewards, and Tour Guide).</li>
	 *   <li>Creates a list of users and associates a visited location (attraction) with each user.</li>
	 *   <li>Asynchronously calculates the rewards for all users by calling the 
	 *       {@link RewardsService#calculateRewardsAsync(List<User>)} method.</li>
	 *   <li>Waits for all reward calculations to complete using {@link CompletableFuture#join()}.</li>
	 *   <li>Validates that each user has received at least one reward by checking the size of their user rewards list.</li>
	 *   <li>Measures and prints the elapsed time for the entire process.</li>
	 *   <li>Asserts that the total elapsed time is within an acceptable limit (20 minutes).</li>
	 * </ol>
	 * 
	 * @see RewardsService#calculateRewardsAsync(List<User>)
	 * @see User
	 * @see VisitedLocation
	 * @see Attraction
	 * @see TourGuideService
	 */
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

	    CompletableFuture<Void> allRewardsCalculated = rewardsService.calculateRewardsAsync(allUsers);

	    allRewardsCalculated.join(); 

		for (User user : allUsers)
			assertTrue(user.getUserRewards().size() > 0);
		
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
