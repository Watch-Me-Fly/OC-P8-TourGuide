package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}

	@Test
	public void isWithinAttractionProximity() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	/**
	 * Tests that a user receives a reward for every attraction when proximity is a very large value
	 * (effectively disabling distance checks)
	 */
	@DisplayName("Reward user for nearby attractions")
	@Test
	public void nearAllAttractions() {
		// Arrange ___
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		List<Attraction> attractions = gpsUtil.getAttractions();

		// Sets proximity to MAX, so the user is near EVERY attraction
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		// Sets the test to simulate 1 user
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = tourGuideService.getAllUsers().get(0);

		// Act ___
		// Add a visited location at each attraction location
		for (Attraction attraction : attractions) {
			VisitedLocation visited = new VisitedLocation(
					user.getUserId(),
					new Location(attraction.latitude, attraction.longitude),
					new Date());
			user.addToVisitedLocations(visited);
		}
		// Retrieve a list of user's rewards and stop tracker thread
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = tourGuideService.getUserRewards(user);

		// Stop tracking
		tourGuideService.tracker.stopTracking();

		// Assert ___
		// Were rewards obtained for ALL available attractions ?
		int nbOfAttractions = attractions.size();
		int nbOfUserRewards = userRewards.size();
		assertEquals(attractions.size(), userRewards.size());
	}

}
