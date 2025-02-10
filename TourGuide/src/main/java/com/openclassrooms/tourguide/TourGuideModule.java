package com.openclassrooms.tourguide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;

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

//    @Bean
//    public RewardsService rewardsService() {
//        return new RewardsService(gpsUtil(), rewardCentral());
//    }

//    @Bean
//    public TourGuideService tourGuideService() {
//        return new TourGuideService(gpsUtil(), rewardsService);
//    }
}