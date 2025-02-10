package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

/**
 * Represents a user in the TourGuide application.
 * This class stores user details, visited locations, rewards, preferences, and trip deals.
 */
public class User {
    
    /** The unique identifier of the user. */
    private final UUID userId;
    
    /** The username of the user. */
    private final String userName;
    
    /** The phone number of the user. */
    private String phoneNumber;
    
    /** The email address of the user. */
    private String emailAddress;
    
    /** The timestamp of the user's latest known location. */
    private Date latestLocationTimestamp;
    
    /** The list of locations visited by the user. */
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    
    /** The list of rewards earned by the user. */
    private List<UserReward> userRewards = new ArrayList<>();
    
    /** The user's preferences for travel and rewards. */
    private UserPreferences userPreferences = new UserPreferences();
    
    /** The list of trip deals available to the user. */
    private List<Provider> tripDeals = new ArrayList<>();
    
    /**
     * Constructs a new User object with the given details.
     *
     * @param userId      The unique identifier of the user.
     * @param userName    The username of the user.
     * @param phoneNumber The phone number of the user.
     * @param emailAddress The email address of the user.
     */
    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
    
    /** 
     * @return The unique identifier of the user.
     */
    public UUID getUserId() {
        return userId;
    }
    
    /** 
     * @return The username of the user.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Sets the phone number of the user.
     *
     * @param phoneNumber The new phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /** 
     * @return The phone number of the user.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the email address of the user.
     *
     * @param emailAddress The new email address.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /** 
     * @return The email address of the user.
     */
    public String getEmailAddress() {
        return emailAddress;
    }
    
    /**
     * Sets the timestamp of the user's latest known location.
     *
     * @param latestLocationTimestamp The timestamp of the latest location.
     */
    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }
    
    /** 
     * @return The timestamp of the user's latest known location.
     */
    public Date getLatestLocationTimestamp() {
        return latestLocationTimestamp;
    }
    
    /**
     * Adds a visited location to the user's history.
     *
     * @param visitedLocation The visited location to add.
     */
    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }
    
    /** 
     * @return A list of all locations visited by the user.
     */
    public List<VisitedLocation> getVisitedLocations() {
        return visitedLocations;
    }
    
    /** Clears all visited locations for the user. */
    public void clearVisitedLocations() {
        visitedLocations.clear();
    }
    
    /**
     * Adds a user reward if the user has not already received it for the same attraction.
     *
     * @param userReward The reward to add.
     */
    public void addUserReward(UserReward userReward) {
        if (!userRewards.stream().anyMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName))) {
            userRewards.add(userReward);
        }
    }
    
    /** 
     * @return A list of all rewards earned by the user.
     */
    public List<UserReward> getUserRewards() {
        return userRewards;
    }
    
    /** 
     * @return The user's travel and reward preferences.
     */
    public UserPreferences getUserPreferences() {
        return userPreferences;
    }
    
    /**
     * Sets the user's travel and reward preferences.
     *
     * @param userPreferences The new user preferences.
     */
    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    /** 
     * @return The last visited location of the user, or null if no locations exist.
     */
    public VisitedLocation getLastVisitedLocation() {
        if (visitedLocations.isEmpty()) {
            return null;
        }
        return visitedLocations.get(visitedLocations.size() - 1);
    }
    
    /**
     * Sets the trip deals available to the user.
     *
     * @param tripDeals The list of trip deals.
     */
    public void setTripDeals(List<Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }
    
    /** 
     * @return A list of trip deals available to the user.
     */
    public List<Provider> getTripDeals() {
        return tripDeals;
    }
}
