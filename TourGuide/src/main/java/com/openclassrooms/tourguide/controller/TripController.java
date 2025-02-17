package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.tourguide.service.TripService;
import com.openclassrooms.tourguide.service.UserService;

import lombok.extern.slf4j.Slf4j;
import tripPricer.Provider;

@RestController
@Slf4j
@RequestMapping("/tripdeals") 
public class TripController {

	private final TripService tripService;
	private final UserService userService;
	
	public TripController(UserService userService, TripService tripService) { 
		this.userService = userService; 
		this.tripService = tripService;
	}
	
	 @GetMapping("")
	 public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName) {
	    log.info("call API /tripdeals with user: {}", userName);
	 	return ResponseEntity.ok(tripService.getTripDeals(userService.getUser(userName)));
	 }
	
}
