package com.openclassrooms.tourguide.interfaces;

import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;

public interface IGpsUtilService {

    VisitedLocation getUserLocation(User user);
    List<Attraction> getAttractions();

}