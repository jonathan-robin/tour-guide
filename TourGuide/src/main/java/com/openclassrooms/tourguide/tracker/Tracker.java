package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.openclassrooms.tourguide.application.TourGuideService;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.service.LocationService;
import com.openclassrooms.tourguide.service.UserService;

/**
 * The {@code Tracker} class is responsible for periodically tracking users' locations.
 * It runs as a separate thread and continuously updates the location of all users at a fixed interval.
 * 
 * <p>The tracking process involves retrieving all users from the {@link TourGuideService} and 
 * invoking the {@code trackUserLocation} method on each user.</p>
 * 
 * <p>The tracking interval is set to 5 minutes by default.</p>
 * 
 * <p>To ensure proper shutdown, the {@code stopTracking()} method should be called.</p>
 */
public class Tracker extends Thread {
    private Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private UserService userService;
    @Autowired
    private LocationService locationService;
    private boolean stop = false;

    /**
     * Creates a new {@code Tracker} instance and starts tracking users' locations.
     * 
     * @param tourGuideService The {@link TourGuideService} instance used to track users' locations.
     */
    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
        executorService.submit(this);
    }

    /**
     * Stops the tracking process and shuts down the tracker thread.
     * 
     * <p>This method ensures that the tracking thread is properly terminated.</p>
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    /**
     * Runs the tracking process in a loop until the tracker is stopped.
     * 
     * <p>This method retrieves all users from the {@link TourGuideService} and updates their locations.
     * It also logs the time taken to track all users and ensures the process runs at a fixed interval.</p>
     */
    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            List<User> users = userService.getAllUsers();

            logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            users.forEach(locationService::trackUserLocation);
            stopWatch.stop();
            logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();

            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
