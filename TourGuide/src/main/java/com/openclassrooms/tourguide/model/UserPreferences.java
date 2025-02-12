package com.openclassrooms.tourguide.model;

/**
 * Represents the user preferences for a trip, including proximity to attractions,
 * trip duration, number of tickets, and number of travelers (adults and children).
 */
public class UserPreferences {
    
    /** Maximum distance to an attraction that the user considers acceptable. */
    private int attractionProximity = Integer.MAX_VALUE;
    
    /** Duration of the trip in days. */
    private int tripDuration = 1;
    
    /** Number of tickets required for the trip. */
    private int ticketQuantity = 1;
    
    /** Number of adults traveling. */
    private int numberOfAdults = 1;
    
    /** Number of children traveling. */
    private int numberOfChildren = 0;
    
    /**
     * Default constructor initializing user preferences with default values.
     */
    public UserPreferences() {
    }
    
    /**
     * Sets the maximum distance to an attraction that the user considers acceptable.
     * 
     * @param attractionProximity the proximity value in distance units
     */
    public void setAttractionProximity(int attractionProximity) {
        this.attractionProximity = attractionProximity;
    }
    
    /**
     * Gets the maximum distance to an attraction that the user considers acceptable.
     * 
     * @return the attraction proximity in distance units
     */
    public int getAttractionProximity() {
        return attractionProximity;
    }
    
    /**
     * Gets the duration of the trip in days.
     * 
     * @return the trip duration
     */
    public int getTripDuration() {
        return tripDuration;
    }

    /**
     * Sets the duration of the trip in days.
     * 
     * @param tripDuration the duration of the trip
     */
    public void setTripDuration(int tripDuration) {
        this.tripDuration = tripDuration;
    }

    /**
     * Gets the number of tickets required for the trip.
     * 
     * @return the ticket quantity
     */
    public int getTicketQuantity() {
        return ticketQuantity;
    }

    /**
     * Sets the number of tickets required for the trip.
     * 
     * @param ticketQuantity the number of tickets
     */
    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }
    
    /**
     * Gets the number of adults traveling.
     * 
     * @return the number of adults
     */
    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    /**
     * Sets the number of adults traveling.
     * 
     * @param numberOfAdults the number of adults
     */
    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    /**
     * Gets the number of children traveling.
     * 
     * @return the number of children
     */
    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    /**
     * Sets the number of children traveling.
     * 
     * @param numberOfChildren the number of children
     */
    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }
}
