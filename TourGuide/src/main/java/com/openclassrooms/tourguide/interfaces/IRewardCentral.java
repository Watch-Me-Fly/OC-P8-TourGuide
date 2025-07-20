package com.openclassrooms.tourguide.interfaces;

import java.util.UUID;

public interface IRewardCentral {
    int getAttractionRewardPoints(UUID attractionId, UUID userId);
}
