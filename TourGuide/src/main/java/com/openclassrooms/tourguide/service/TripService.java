package com.openclassrooms.tourguide.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.model.User;

import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TripService {
	
	private TripPricer tripPricer = new TripPricer();
	private static final String tripPricerApiKey = "test-server-api-key";
	
	public TripService(TripPricer tripPricer) {
		this.tripPricer = tripPricer;
	}
	
	/**
	 * Retrieves a list of trip deals for the specified user.
	 *
	 * <p>This method calculates the cumulative reward points for the user and fetches available trip deals 
	 * from the Trip Pricer service based on the user's preferences and reward points.</p>
	 *
	 * @param user The user for whom trip deals are to be fetched.
	 * @return A list of trip providers with their available trip deals for the user.
	 */
	public List<Provider> getTripDeals(User user) {
	    int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
	    List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
	            user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
	            user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
	    user.setTripDeals(providers);
	    return providers;
	}
	
	

	
}
