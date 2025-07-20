package com.openclassrooms.tourguide.interfaces;

import rewardCentral.RewardCentral;

import java.util.UUID;

public class RewardCentralImpl implements IRewardCentral{

    private final RewardCentral rewardCentral;

    public RewardCentralImpl() {
        this.rewardCentral = new RewardCentral();
    }

    @Override
    public int getAttractionRewardPoints(UUID attractionId, UUID userId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}
