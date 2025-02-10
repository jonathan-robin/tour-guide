package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.UserReward;

@Controller
public class RewardController {
	
	@Autowired
	RewardsService rewardService;
	
	@Autowired
	TourGuideService tourGuideService;
	
	@Autowired
	UserService userService;
    
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return rewardService.getUserRewards(userService.getUser(userName));
    }
	
}
