package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.openclassrooms.tourguide.service.TripService;
import com.openclassrooms.tourguide.service.UserService;

import tripPricer.Provider;

@Controller
public class TripController {

	@Autowired
	TripService tripService;
	
	@Autowired
	UserService userService;
	
	 @RequestMapping("/getTripDeals")
	 public List<Provider> getTripDeals(@RequestParam String userName) {
	 	return tripService.getTripDeals(userService.getUser(userName));
	 }
	
}
