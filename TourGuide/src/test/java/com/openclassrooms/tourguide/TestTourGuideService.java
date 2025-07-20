package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.interfaces.RewardCentralImpl;
import com.openclassrooms.tourguide.model.NearbyAttractionDTO;
import com.openclassrooms.tourguide.interfaces.GpsUtilServiceImpl;
import com.openclassrooms.tourguide.interfaces.IGpsUtilService;
import gpsUtil.location.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void addUser() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void trackUser() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	/**
	 * Verifies that getNearByAttractions() returns the 5 closest attractions to the user
	 */
	@DisplayName("Verify returning nearby attractions")
	@Test
	public void getNearbyAttractions() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());

		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(),
							"jon", "000", "jon@tourGuide.com");
		Location location = new Location(44.13243, 55.38535);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
		user.addToVisitedLocations(visitedLocation);

		rewardsService.setAttractionProximityRange(Integer.MAX_VALUE);
		List<NearbyAttractionDTO> attractions = tourGuideService.getNearByAttractions(visitedLocation);

		tourGuideService.tracker.stopTracking();

		assertTrue(attractions.size() >= 5);
	}

	@Test
	public void getTripDeals() {
		IGpsUtilService gpsUtil = new GpsUtilServiceImpl();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralImpl());
		InternalTestHelper.setInternalUserNumber(100);

		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(),
				"jon",
				"000",
				"jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(5, providers.size());
	}

}
