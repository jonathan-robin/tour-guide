package com.openclassrooms.tourguide.service;

import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

@Service
public class UtilsService {
	
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	   
	private int attractionProximityRange = 200;
	
	public int defaultProximityBuffer = 10;
	public int proximityBuffer = defaultProximityBuffer;

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
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
	public boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
	    return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	

	
}
