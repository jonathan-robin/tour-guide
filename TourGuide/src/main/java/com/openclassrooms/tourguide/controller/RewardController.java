package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/rewards") 
public class RewardController {
	
	private final RewardsService rewardService;
	private final UserService userService;
	
	public RewardController(RewardsService rewardService, UserService userService) {
		this.rewardService = rewardService; 
		this.userService = userService;
	}
    
    @GetMapping("") 
    public ResponseEntity<List<UserReward>> getRewards(@RequestParam String userName) {
    	log.info("call GET API /rewards with user: {}", userName);
    	return ResponseEntity.ok(rewardService.getUserRewards(userService.getUser(userName)));
    }
	
}
