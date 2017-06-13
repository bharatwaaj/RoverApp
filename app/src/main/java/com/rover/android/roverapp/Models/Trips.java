package com.rover.android.roverapp.Models;

import android.content.Context;

import com.google.firebase.database.IgnoreExtraProperties;
import com.rover.android.roverapp.Others.Const;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Bharatwaaj on 17-05-2017.
 */

@IgnoreExtraProperties
public class Trips implements Serializable{

    private float tripSourceLatitude;
    private float tripSourceLongitude;
    private float tripDestinationLatitude;
    private float tripDestinationLongitude;
    private float tripDistance;
    private float tripMaxSpeed;
    private float tripAvgSpeed;
    private long tripDuration;
    private long tripStartTime;
    private long tripEndTime;
    private String tripId;
    private int tripScore;
    private float tripRate;
    private String tripSourceAddr;
    private String tripDestAddr;

    public Trips (float tripAvgSpeed, float tripDestinationLatitude, float tripDestinationLongitude, float tripDistance, long tripDuration, long tripEndTime, String tripId, float tripMaxSpeed, float tripSourceLatitude, float tripSourceLongitude, long tripStartTime) {
        this.tripAvgSpeed = tripAvgSpeed;
        this.tripDestinationLatitude = tripDestinationLatitude;
        this.tripDestinationLongitude = tripDestinationLongitude;
        this.tripDistance = tripDistance;
        this.tripDuration = tripDuration;
        this.tripEndTime = tripEndTime;
        this.tripId = tripId;
        this.tripMaxSpeed = tripMaxSpeed;
        this.tripSourceLatitude = tripSourceLatitude;
        this.tripSourceLongitude = tripSourceLongitude;
        this.tripStartTime = tripStartTime;
    }

    public String getTripDestAddr(Context context) {
        return Const.getCompleteAddressString(context, this.tripDestinationLatitude, this.tripDestinationLongitude);
    }

    public String getTripSourceAddr(Context context) {
        return Const.getCompleteAddressString(context, this.tripSourceLatitude, this.tripSourceLongitude);
    }

    public void setTripRate(float tripRate) {
        this.tripRate = tripRate;
    }

    public float getTripRate() {
        return tripRate;
    }

    public Trips () {
        tripId = UUID.randomUUID().toString();
    }

    public int getTripScore() {
        return tripScore;
    }

    public void setTripScore(int tripScore) {
        this.tripScore = tripScore;
    }

    public float getTripAvgSpeed() {
        return tripAvgSpeed;
    }

    public void setTripAvgSpeed(float tripAvgSpeed) {
        this.tripAvgSpeed = tripAvgSpeed;
    }

    public float getTripDestinationLatitude() {
        return tripDestinationLatitude;
    }

    public void setTripDestinationLatitude(float tripDestinationLatitude) {
        this.tripDestinationLatitude = tripDestinationLatitude;
    }

    public float getTripDestinationLongitude() {
        return tripDestinationLongitude;
    }

    public void setTripDestinationLongitude(float tripDestinationLongitude) {
        this.tripDestinationLongitude = tripDestinationLongitude;
    }

    public float getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(float tripDistance) {
        this.tripDistance = tripDistance;
    }

    public long getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(long tripDuration) {
        this.tripDuration = tripDuration;
    }

    public long getTripEndTime() {
        return tripEndTime;
    }

    public void setTripEndTime(long tripEndTime) {
        this.tripEndTime = tripEndTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public float getTripMaxSpeed() {
        return tripMaxSpeed;
    }

    public void setTripMaxSpeed(float tripMaxSpeed) {
        this.tripMaxSpeed = tripMaxSpeed;
    }

    public float getTripSourceLatitude() {
        return tripSourceLatitude;
    }

    public void setTripSourceLatitude(float tripSourceLatitude) {
        this.tripSourceLatitude = tripSourceLatitude;
    }

    public float getTripSourceLongitude() {
        return tripSourceLongitude;
    }

    public void setTripSourceLongitude(float tripSourceLongitude) {
        this.tripSourceLongitude = tripSourceLongitude;
    }

    public long getTripStartTime() {
        return tripStartTime;
    }

    public void setTripStartTime(long tripStartTime) {
        this.tripStartTime = tripStartTime;
    }
}
