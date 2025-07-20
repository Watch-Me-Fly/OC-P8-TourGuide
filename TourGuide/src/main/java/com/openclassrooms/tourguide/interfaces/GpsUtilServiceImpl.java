package com.openclassrooms.tourguide.interfaces;

import com.openclassrooms.tourguide.user.User;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.util.List;

public class GpsUtilServiceImpl implements IGpsUtilService {

    private final GpsUtil gpsUtil;

    public GpsUtilServiceImpl() {
        this.gpsUtil = new GpsUtil();
    }

    @Override
    public VisitedLocation getUserLocation(User user) {
        return gpsUtil.getUserLocation(user.getUserId());
    }

    @Override
    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

}