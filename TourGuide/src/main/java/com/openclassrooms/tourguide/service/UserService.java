package com.openclassrooms.tourguide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.user.User;

@Service
public class UserService {
	
	@Autowired
	TourGuideService tourGuideService;

    public User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   
	
}
