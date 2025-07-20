package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.interfaces.GpsUtilServiceImpl;
import com.openclassrooms.tourguide.interfaces.IGpsUtilService;
import com.openclassrooms.tourguide.interfaces.IRewardCentral;
import com.openclassrooms.tourguide.interfaces.RewardCentralImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openclassrooms.tourguide.service.RewardsService;

@Configuration
public class TourGuideModule {
	
	@Bean
	public IGpsUtilService getGpsUtil() {
		return new GpsUtilServiceImpl();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public IRewardCentral getRewardCentral() {
		return new RewardCentralImpl();
	}
	
}
