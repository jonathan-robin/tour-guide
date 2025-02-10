package com.openclassrooms.tourguide.dto;

/**
 * Data Transfer Object (DTO) that holds information about a nearby attraction for a user.
 * This class stores the details of an attraction and the user's proximity to it, along with the reward points they can earn.
 * 
 * <p>The DTO includes the name and location of the attraction, the user's location, the distance to the attraction,
 * and the reward points associated with visiting the attraction.</p>
 */
public class UserNearByAttractionDto {

    private String attractionName;
    private double attractionLatitude;
    private double attractionLongitude;
    private double userLatitude;
    private double userLongitude;
    private double distanceInMiles;
    private int rewardPoints;

    /**
     * Constructs a new {@link UserNearByAttractionDto} with the specified attributes.
     *
     * @param attractionName The name of the attraction.
     * @param attractionLatitude The latitude of the attraction.
     * @param attractionLongitude The longitude of the attraction.
     * @param userLatitude The latitude of the user.
     * @param userLongitude The longitude of the user.
     * @param distanceInMiles The distance from the user to the attraction in miles.
     * @param rewardPoints The reward points the user will earn by visiting the attraction.
     */
    public UserNearByAttractionDto(String attractionName, double attractionLatitude, double attractionLongitude,
                                   double userLatitude, double userLongitude, double distanceInMiles, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitude = attractionLatitude;
        this.attractionLongitude = attractionLongitude;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.distanceInMiles = distanceInMiles;
        this.rewardPoints = rewardPoints;
    }

    /**
     * Gets the name of the attraction.
     *
     * @return The attraction's name.
     */
    public String getAttractionName() {
        return attractionName;
    }

    /**
     * Sets the name of the attraction.
     *
     * @param attractionName The name of the attraction to set.
     */
    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    /**
     * Gets the latitude of the attraction.
     *
     * @return The latitude of the attraction.
     */
    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    /**
     * Sets the latitude of the attraction.
     *
     * @param attractionLatitude The latitude of the attraction to set.
     */
    public void setAttractionLatitude(double attractionLatitude) {
        this.attractionLatitude = attractionLatitude;
    }

    /**
     * Gets the longitude of the attraction.
     *
     * @return The longitude of the attraction.
     */
    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    /**
     * Sets the longitude of the attraction.
     *
     * @param attractionLongitude The longitude of the attraction to set.
     */
    public void setAttractionLongitude(double attractionLongitude) {
        this.attractionLongitude = attractionLongitude;
    }

    /**
     * Gets the latitude of the user.
     *
     * @return The user's latitude.
     */
    public double getUserLatitude() {
        return userLatitude;
    }

    /**
     * Sets the latitude of the user.
     *
     * @param userLatitude The latitude of the user to set.
     */
    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    /**
     * Gets the longitude of the user.
     *
     * @return The user's longitude.
     */
    public double getUserLongitude() {
        return userLongitude;
    }

    /**
     * Sets the longitude of the user.
     *
     * @param userLongitude The longitude of the user to set.
     */
    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    /**
     * Gets the distance from the user to the attraction in miles.
     *
     * @return The distance in miles.
     */
    public double getDistanceInMiles() {
        return distanceInMiles;
    }

    /**
     * Sets the distance from the user to the attraction in miles.
     *
     * @param distanceInMiles The distance in miles to set.
     */
    public void setDistanceInMiles(double distanceInMiles) {
        this.distanceInMiles = distanceInMiles;
    }

    /**
     * Gets the reward points associated with the attraction for the user.
     *
     * @return The reward points.
     */
    public int getRewardPoints() {
        return rewardPoints;
    }

    /**
     * Sets the reward points associated with the attraction for the user.
     *
     * @param rewardPoints The reward points to set.
     */
    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    /**
     * Returns a string representation of the nearby attraction DTO.
     * 
     * @return A string representation of the nearby attraction DTO.
     */
    @Override
    public String toString() {
        return "NearbyAttractionDTO{" +
                "attractionName='" + attractionName + '\'' +
                ", attractionLatitude=" + attractionLatitude +
                ", attractionLongitude=" + attractionLongitude +
                ", userLatitude=" + userLatitude +
                ", userLongitude=" + userLongitude +
                ", distanceInMiles=" + distanceInMiles +
                ", rewardPoints=" + rewardPoints +
                '}';
    }
}
