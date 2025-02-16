package com.openclassrooms.tourguide.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.config.AsyncConfig;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;

@SpringBootTest
@Slf4j
public class RewardsServiceTest {

	@Autowired
    private RewardCentral rewardCentral;

    @Autowired
    private UtilsService utilsService;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private UserService userService;
    @Autowired 
    private LocationService locationService;
    
    private User user;
    private Attraction attraction;
    private VisitedLocation visitedLocation;
    
    @BeforeEach()
    public void setup() { 
    	user = userService.getAllUsers().get(0);
    	attraction = locationService.getAttractions().get(0);
    }
	
    @Test
    void testCalculateRewards_UserEarnsReward() {
    	
        locationService.trackUserLocation(user);
        rewardsService.calculateRewards(user, Arrays.asList(attraction));
        user.addUserReward(new UserReward(visitedLocation, attraction, 100)); 
        List<UserReward> rewards = rewardsService.getUserRewards(user);
        assertEquals(1, rewards.size());
        assertEquals(100, rewards.get(0).getRewardPoints());
    }
    
    @Test
    void testCalculateRewards_UserAlreadyRewarded() {
        user.addUserReward(new UserReward(visitedLocation, attraction, 100));
        rewardsService.calculateRewards(user, Arrays.asList(attraction));
        List<UserReward> rewards = rewardsService.getUserRewards(user);
        assertEquals(1, rewards.size());
    }
    
    
    
	
}
