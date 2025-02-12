package com.openclassrooms.tourguide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openclassrooms.tourguide.service.RewardsService;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tripPricer.TripPricer;

@Configuration
public class TourGuideModule {
	
//	RewardsService rewardsService;

    @Bean
    public GpsUtil gpsUtil() {
        return new GpsUtil();
    }

    @Bean
    public RewardCentral rewardCentral() {
        return new RewardCentral();
    }
    
    @Bean
    public TripPricer tripPricer() {
        return new TripPricer();
    }

//    @Bean
//    public RewardsService rewardsService() {
//        return new RewardsService(rewardCentral());
//    }
//    
//    @Bean
//    public RewardsService rewardsService() {
//        return new RewardsService(rewardCentral());
//    }

//    @Bean
//    public TourGuideService tourGuideService() {
//        return new TourGuideService(gpsUtil(), rewardsService);
//    }
}