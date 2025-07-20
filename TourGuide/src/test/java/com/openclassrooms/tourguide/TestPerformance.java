package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.interfaces.IGpsUtilService;
import com.openclassrooms.tourguide.interfaces.IRewardCentral;
import gpsUtil.location.Location;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.*;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

/**
 * Performance test class
 *
 * <p>Contains high-load tests to measure scalability and response time for critical operations (location tracking and reward calculation)</p>
 *
 * <p>Target performance metric</p>
 * <ul>
 *     <li><b>Track location :</b> 100,000 users within 15 minutes.</li>
 *     <li><b>Get rewards</b> 100,000 users within 20 minutes</li>
 * </ul>
 *
 * <p>To adjust user count, call: {@code InternalTestHelper.setInternalUserNumber(x)}</p>
 */
@Tag("performance")
public class TestPerformance {

	private static IGpsUtilService gpsUtil;
	private static RewardsService rewardsService;
	private static TourGuideService tourGuideService;
	private StopWatch stopWatch;

	@BeforeAll
	static void setUpBeforeClass() {
		gpsUtil = mock(IGpsUtilService.class);
		IRewardCentral rewardCentral = mock(IRewardCentral.class);
		
		when(gpsUtil.getAttractions()).thenReturn(generateMockAttractions());
		when(gpsUtil.getUserLocation(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			return new VisitedLocation(user.getUserId(), new Location(48.8584, 2.2945), new Date());
		});
		when(rewardCentral.getAttractionRewardPoints(any(), any())).thenReturn(100);

		rewardsService = new RewardsService(gpsUtil, rewardCentral);
	}

	private static List<Attraction> generateMockAttractions() {
		return List.of(
				new Attraction("Eiffel Tower", "Paris", "FR", 48.8584, 2.2945),
				new Attraction("Big Ben", "London", "UK", 51.5007, -0.1246),
				new Attraction("Colosseum", "Rome", "IT", 41.8902, 12.4922)
		);
	}

	@BeforeEach
	void setUp() {
		stopWatch = new StopWatch();
		tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		// simulate 100K app users for each test
		InternalTestHelper.setInternalUserNumber(100000);
	}

	@AfterEach
	void tearDown() {
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
	}

	@DisplayName("Measure how long it takes to track location for a large number of users")
	@Test
	public void highVolumeTrackLocation() {
		// simulate user's list
		List<User> allUsers = tourGuideService.getAllUsers();

		// start service and fetch users
		stopWatch.start();

		// track locations
		tourGuideService.trackAllUsersLocations(allUsers);

		long limitTo15minutes = TimeUnit.MINUTES.toSeconds(15);
		long totalTime = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + totalTime + " seconds.");

		// verify the total time is less than or equal to 15 minutes
		assertTrue(limitTo15minutes >= totalTime);
	}

	@DisplayName("Measure how long it takes to calculate rewards for a large number of users")
	@Test
	public void highVolumeGetRewards() {
		// start service
		stopWatch.start();

		// simulate an attraction
		Attraction attraction = gpsUtil.getAttractions().get(0);

		// generate a list of users again
		List<User> allUsers = tourGuideService.getAllUsers();

		allUsers.forEach(user -> {
			// add an attraction to each user
			VisitedLocation visitedLocation = new VisitedLocation(
					user.getUserId(), attraction, new Date());
			user.addToVisitedLocations(visitedLocation);
		});

		// act
		rewardsService.calculateRewardsForMultipleUsers(allUsers);

		// assert all users were rewarded
		allUsers.forEach(user -> {
			assertFalse(user.getUserRewards().isEmpty());
		});

		long limitTo20minutes = TimeUnit.MINUTES.toSeconds(20);
		long totalTime = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());

		// verify the total time is less than or equal to 20 minutes
		System.out.println("highVolumeGetRewards: Time Elapsed: " + totalTime + " seconds.");
		assertTrue(limitTo20minutes >= totalTime);
	}

}
