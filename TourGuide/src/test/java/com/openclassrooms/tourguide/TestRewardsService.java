package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@ExtendWith(MockitoExtension.class)
public class TestRewardsService {

	@Mock
	private GpsUtil gpsUtil;
	@Mock
	private RewardCentral rewardCentral;
	@InjectMocks
	private RewardsService rewardsService;

	private User user;
	private List<Attraction> attractions;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		attractions = List.of(
				new Attraction("A", "City", "State", 10.0, 10.0),
				new Attraction("B", "City", "State", 20.0, 20.0),
				new Attraction("C", "City", "State", 30.0, 30.0),
				new Attraction("D", "City", "State", 40.0, 40.0),
				new Attraction("E", "City", "State", 50.0, 50.0));

		rewardsService = new RewardsService(gpsUtil, rewardCentral);
		when(gpsUtil.getAttractions()).thenReturn(attractions);
		when(rewardCentral.getAttractionRewardPoints(any(), any())).thenReturn(500);
	}

	@Test
	public void userGetRewards() {
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		// location
		Location mockedLocation = new Location(10.0, 10.0);
		VisitedLocation mockedVisit = new VisitedLocation(user.getUserId(), mockedLocation, new Date());
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(mockedVisit);

		// attraction
		Attraction attraction = attractions.getFirst();
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		// act
		tourGuideService.trackUserLocation(user);

		List<UserReward> userRewards = user.getUserRewards();

		tourGuideService.tracker.stopTracking();
        assertEquals(1, userRewards.size());
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
		List<Attraction> attractions = gpsUtil.getAttractions();

		// Sets proximity to MAX, so the user is near EVERY attraction
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		// Sets the test to simulate 1 user
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

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
