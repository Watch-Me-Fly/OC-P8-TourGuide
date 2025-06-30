package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.*;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
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
public class TestPerformance {

	private static GpsUtil gpsUtil;
	private static RewardsService rewardsService;
	private static TourGuideService tourGuideService;
	private StopWatch stopWatch;

	@BeforeAll
	static void setUpBeforeClass() {
		gpsUtil = new GpsUtil();
		rewardsService = new RewardsService(gpsUtil, new RewardCentral());
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
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		// start service and fetch users
		stopWatch.start();
		for (User user : allUsers) {
			tourGuideService.addUser(user);
		}

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
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		allUsers.forEach(user -> {
			// add the attraction to each user
			VisitedLocation visitedLocation = new VisitedLocation(
					user.getUserId(), attraction, new Date());
			user.addToVisitedLocations(visitedLocation);

			// calculate rewards
			rewardsService.calculateRewards(user);

			// check user was rewarded
			assertTrue(user.getUserRewards().size() > 0);
		});

		long limitTo20minutes = TimeUnit.MINUTES.toSeconds(20);
		long totalTime = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());

		// verify the total time is less than or equal to 20 minutes
		System.out.println("highVolumeGetRewards: Time Elapsed: " + totalTime + " seconds.");
		assertTrue(limitTo20minutes >= totalTime);
	}

}
