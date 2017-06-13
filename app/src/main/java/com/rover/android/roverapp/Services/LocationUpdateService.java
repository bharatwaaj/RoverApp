package com.rover.android.roverapp.Services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rover.android.roverapp.Models.Trips;
import com.rover.android.roverapp.Others.Const;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private float lastX, lastY, lastZ;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private double oldLat = 0.0;
    private double oldLon = 0.0;

    protected static final String TAG = "LocationUpdateService";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    public static Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    public static final double HARD_BRAKING_THRESHOLD_RANGE_END = 8;
    public static final double HARD_BRAKING_THRESHOLD_RANGE_START = 6;
    public static final double SOFT_BRAKING_THRESHOLD_RANGE_END = 6;
    public static final double SOFT_BRAKING_THRESHOLD_RANGE_START = 4;

    public static final double HARD_SPEEDING_THRESHOLD_RANGE_END = -17;
    public static final double HARD_SPEEDING_THRESHOLD_RANGE_START = -12;
    public static final double SOFT_SPEEDING_THRESHOLD_RANGE_END = -12;
    public static final double SOFT_SPEEDING_THRESHOLD_RANGE_START = -7;

    protected Location mCurrentLocation;
    public static boolean isEnded = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Trips trips;

    private int speedCounter = 0;
    private int brakeCounter = 0;
    private float distanceCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        trips = new Trips();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOC", "Service init...");
        isEnded = false;
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        buildGoogleApiClient();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {

        }
        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended==");
        mGoogleApiClient.connect();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        if (Const.checkTripStart(this, trips.getTripId()) && location != null) {
            setTripSourceDetails(location);
            oldLat = location.getLatitude();
            oldLon = location.getLongitude();
        }
        if (location != null) {
            uploadTripRouteDetailsToFirebase(location);
            float tempDistance = getDistanceTravelled(location) + distanceCounter;
            distanceCounter = tempDistance;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient===");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, " startLocationUpdates===");
            isEnded = true;
        }
    }

    private void setTripSourceDetails(Location startLocation) {
        speedCounter = 0;
        brakeCounter = 0;
        trips.setTripSourceLatitude((float) startLocation.getLatitude());
        trips.setTripSourceLongitude((float) startLocation.getLongitude());
        trips.setTripStartTime(System.currentTimeMillis() / 1000);
    }

    protected void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            Log.d(TAG, "stopLocationUpdates();==");
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setTripDestDetails(mCurrentLocation);
        uploadTripsToFirebase();
        stopLocationUpdates();
        sensorManager.unregisterListener(this);
    }

    private void setTripDestDetails(Location location) {
        if (location != null) {
            trips.setTripDestinationLatitude((float) location.getLatitude());
            trips.setTripDestinationLongitude((float) location.getLongitude());
        }
        trips.setTripDistance(distanceCounter);
        trips.setTripEndTime(System.currentTimeMillis() / 1000);
        if(trips.getTripStartTime() != 0){
            trips.setTripDuration(trips.getTripEndTime() - trips.getTripStartTime());
        }else{
            trips.setTripDuration(0);
        }
        speedCounter = 0;
        brakeCounter = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        deltaX = lastX - sensorEvent.values[0];
        deltaY = lastY - sensorEvent.values[1];
        deltaZ = lastZ - sensorEvent.values[2];

        long speedx = 0;
        long speedy = 0;
        long speedz = 0;
        long speed = 0;

        speedx += Math.abs(deltaX);
        speedy += Math.abs(deltaY);
        speedz += Math.abs(deltaZ);

        speed = (long) Math.sqrt(Math.pow(speedx,2) + Math.pow(speedy,2) + Math.pow(speedz,2));

        if((speed * 3.6) >= 20 && (speed * 3.6) <= 120){
            uploadTripSpeedDetailsAlwaysToFirebase((long) (speed * 3.6));
        }

        if (deltaY <= SOFT_SPEEDING_THRESHOLD_RANGE_START && deltaY > SOFT_SPEEDING_THRESHOLD_RANGE_END) {
            speedCounter++;
            uploadTripSpeedingDetailsToFirebase("s");
        } else if (deltaY <= HARD_SPEEDING_THRESHOLD_RANGE_START && deltaY > HARD_SPEEDING_THRESHOLD_RANGE_END) {
            speedCounter += 2;
            uploadTripSpeedingDetailsToFirebase("hs");
        } else if (deltaY <= HARD_SPEEDING_THRESHOLD_RANGE_END) {
            speedCounter += 3;
            uploadTripSpeedingDetailsToFirebase("vhs");
        }

        if (deltaY >= SOFT_BRAKING_THRESHOLD_RANGE_START && deltaY < SOFT_BRAKING_THRESHOLD_RANGE_END) {
            brakeCounter++;
            uploadTripSpeedingDetailsToFirebase("b");
        } else if (deltaY >= HARD_BRAKING_THRESHOLD_RANGE_START && deltaY < HARD_BRAKING_THRESHOLD_RANGE_END) {
            brakeCounter += 2;
            uploadTripSpeedingDetailsToFirebase("hb");
        } else if (deltaY >= HARD_BRAKING_THRESHOLD_RANGE_END) {
            brakeCounter += 3;
            uploadTripSpeedingDetailsToFirebase("vhb");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void uploadTripsToFirebase() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        // .child(df.format(c.getTime()))
        databaseReference.child("trips").child(getUid()).child(trips.getTripId()).setValue(trips);
    }

    private void uploadTripRouteDetailsToFirebase(Location location) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        if (location != null) {
            databaseReference.child("tripRoute").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("latitude").setValue(location.getLatitude());
            databaseReference.child("tripRoute").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("longitude").setValue(location.getLongitude());
        }
    }

    private void uploadTripSpeedingDetailsToFirebase(String string) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        databaseReference.child("tripSpeeding").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("factor").setValue(string);
        if (mCurrentLocation != null) {
            databaseReference.child("tripSpeeding").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("location").child("latitude").setValue(mCurrentLocation.getLatitude());
            databaseReference.child("tripSpeeding").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("location").child("longitude").setValue(mCurrentLocation.getLongitude());
        }
        databaseReference.child("tripSpeeding").child(getUid()).child(trips.getTripId()).child("braking").setValue(brakeCounter);
        databaseReference.child("tripSpeeding").child(getUid()).child(trips.getTripId()).child("speeding").setValue(speedCounter);
    }

    private void uploadTripSpeedDetailsAlwaysToFirebase(long speed){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);
        databaseReference.child("tripSpeedDetails").child(getUid()).child(trips.getTripId()).child(Long.toString(System.currentTimeMillis() / 1000)).child("speed").setValue(speed);
    }

    public float getDistanceTravelled(Location location) {
        double newLat = location.getLatitude();
        double newLon = location.getLongitude();
        double distance = calculationBydistance(newLat, newLon, oldLat, oldLon);
        oldLat = newLat;
        oldLon = newLon;
        return (float) distance;
    }

    public double calculationBydistance(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6372.8;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return radius * c;
    }
}