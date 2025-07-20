package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.interfaces.*;
import gpsUtil.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@ExtendWith(MockitoExtension.class)
public class TestRewardsService {

	@Mock
	private IGpsUtilService gpsUtil;
	@Mock
	private IRewardCentral rewardCentral;
	@InjectMocks
	private RewardsService rewardsService;

	private User user;
	private List<Attraction> attractions;

	@BeforeEach
	public void setup() {
		user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		attractions = List.of(
				new Attraction("Eiffel Tower", "Paris", "FR", 48.8584, 2.2945),
				new Attraction("Louvre Museum", "Paris", "FR", 48.8606, 2.3376),
				new Attraction("Notre-Dame", "Paris", "FR", 48.8530, 2.3499),
				new Attraction("Arc de Triomphe", "Paris", "FR", 48.8738, 2.2950),
				new Attraction("Montmartre", "Paris", "FR", 48.8867, 2.3431));

		rewardsService = new RewardsService(gpsUtil, rewardCentral);
		lenient().when(rewardCentral.getAttractionRewardPoints(any(), any())).thenReturn(500);
	}

	@Test
	public void userGetRewards() {
		// arrange __
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = attractions.getFirst();
		Location mockedLocation = new Location(attraction.latitude, attraction.longitude);
		VisitedLocation mockedVisit = new VisitedLocation(user.getUserId(), mockedLocation, new Date());

		when(gpsUtil.getUserLocation(user)).thenReturn(mockedVisit);
		when(gpsUtil.getAttractions()).thenReturn(List.of(attraction));

		// act __
		tourGuideService.trackUserLocation(user);

		List<UserReward> userRewards = user.getUserRewards();

		// assert __
		tourGuideService.tracker.stopTracking();
        assertEquals(1, userRewards.size());
	}

	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = attractions.getFirst();
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
		// Sets proximity to MAX, so the user is near EVERY attraction
		when(gpsUtil.getAttractions()).thenReturn(attractions);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		// Sets the test to simulate 1 user
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.addUser(user);

		// Act ___
		// Add a visited location at each attraction location
		for (Attraction attraction : attractions) {
			if (attraction != null) {
				VisitedLocation visited = new VisitedLocation(
						user.getUserId(),
						new Location(attraction.latitude, attraction.longitude),
						new Date());
				user.addToVisitedLocations(visited);
			}
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
