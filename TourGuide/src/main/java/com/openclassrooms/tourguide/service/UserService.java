package com.openclassrooms.tourguide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.model.User;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for managing user-related operations.
 * 
 * <p>This service interacts with the {@link TourGuideService} to retrieve user details and other user-related data.</p>
 */
@Service
@Slf4j
public class UserService {
	
	Boolean testMode = true;
	public final Map<String, User> userMap = new HashMap<>();
	
	public UserService() {
	    if (testMode) {
	        log.info("TestMode enabled");
	        log.debug("Initializing users");
	        initializeInternalUsers();
	        log.debug("Finished initializing users");
	    }
	}

	/**
	 * Retrieves a user by their username.
	 *
	 * <p>This method searches for the user in the internal user map by their username.</p>
	 *
	 * @param userName The username of the user to be retrieved.
	 * @return The user object associated with the provided username, or null if no user is found.
	 */
	public User getUser(String userName) {
	    return userMap.get(userName);
	}
	
	/**
	 * Retrieves a list of all users.
	 *
	 * <p>This method returns all users present in the internal user map.</p>
	 *
	 * @return A list containing all users in the system.
	 */
	public List<User> getAllUsers() {
	    return userMap.values().stream().collect(Collectors.toList());
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
	    if (!userMap.containsKey(user.getUserName())) {
	    	userMap.put(user.getUserName(), user);
	    }
	}
	
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/

	/**
	 * Initializes internal test users.
	 *
	 * <p>This method generates a set number of internal test users based on the value returned from
	 * {@link InternalTestHelper#getInternalUserNumber()}. For each user, a user object is created with
	 * a random UUID, a generated user name, and a phone number, along with a generated email address.
	 * It also assigns the user a set of visited locations, which are stored in an internal map.</p>
	 *
	 * <p>The created users are added to the {@link #internalUserMap}, allowing them to be used in testing.</p>
	 */
	public void initializeInternalUsers() {
	    IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
	        String userName = "internalUser" + i;
	        String phone = "000";
	        String email = userName + "@tourGuide.com";
	        User user = new User(UUID.randomUUID(), userName, phone, email);
	        generateUserLocationHistory(user);

	        userMap.put(userName, user);
	    });
	    log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	/**
	 * Generates a random location history for the specified user.
	 *
	 * <p>This method generates a set of 3 visited locations for the given user, where each location has
	 * a randomly generated latitude, longitude, and timestamp. These locations are added to the user's visited locations list.</p>
	 *
	 * @param user The user for whom location history will be generated.
	 */
	private void generateUserLocationHistory(User user) {
	    IntStream.range(0, 3).forEach(i -> {
	        user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
	                new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
	    });
	}

	/**
	 * Generates a random longitude value.
	 *
	 * <p>This method generates a random longitude between -180 and 180 degrees, ensuring that the value
	 * is within the valid range for geographical coordinates.</p>
	 *
	 * @return A randomly generated longitude value.
	 */
	private double generateRandomLongitude() {
	    double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generates a random latitude value.
	 *
	 * <p>This method generates a random latitude between -85.05112878 and 85.05112878 degrees, ensuring
	 * that the value is within the valid range for geographical coordinates.</p>
	 *
	 * @return A randomly generated latitude value.
	 */
	private double generateRandomLatitude() {
	    double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	/**
	 * Generates a random timestamp within the last 30 days.
	 *
	 * <p>This method generates a random time by subtracting a random number of days (up to 30) from the current date,
	 * and returns the corresponding {@link Date} object representing that time.</p>
	 *
	 * @return A random {@link Date} object representing a timestamp within the last 30 days.
	 */
	private Date getRandomTime() {
	    LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
	public void removeAllUsers() { 
		if (testMode)
			userMap.clear();
	}
    
}
