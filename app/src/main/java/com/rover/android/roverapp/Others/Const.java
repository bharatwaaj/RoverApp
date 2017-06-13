package com.rover.android.roverapp.Others;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class Const {

    public static final String DATABASE_NAME = "Location_master_rover.sqlite";
    public static final String DATABASE_PATH = "/data/data/com.rover.android.roverapp/databases/";
    public static final int DATABASE_VERSION = 2;
    public static final String DASHBOARD_FRAG_FAB_TOGGLE_TAG = "fabToggleValeIsPlayActive";

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public static String getCompleteAddressString(Context m_context, double LATITUDE, double LONGITUDE) {
        String street = null;
        Geocoder geocoder = new Geocoder(m_context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                // Thoroughfare seems to be the street name without numbers
                street = address.getThoroughfare();
                Log.v("My Street Addr is", "" + street);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(m_context, "Sorry, Your location cannot be retrieved !" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return street;
    }

    public static void ExportDatabase(Context mcont) {
        File f = new File(DATABASE_PATH + DATABASE_NAME);
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(f);
            fos = new FileOutputStream("/mnt/sdcard/" + DATABASE_NAME);
            while (true) {
                int i = fis.read();
                if (i != -1) {
                    fos.write(i);
                } else {
                    break;
                }
            }
            fos.flush();
            Toast.makeText(mcont, "Database Export Successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mcont, "DB dump ERROR", Toast.LENGTH_LONG).show();
        }
    }

    public static boolean checkTripStart(Context context, String isTripStart) {
        boolean isFirstRun = context.getSharedPreferences("ROVER_APP_CHECK_TRIP_START_PREF", MODE_PRIVATE).getBoolean(isTripStart, true);
        if (isFirstRun){
            context.getSharedPreferences("ROVER_APP_CHECK_TRIP_START_PREF", MODE_PRIVATE)
                    .edit()
                    .putBoolean(isTripStart, false)
                    .apply();
            return true;
        }
        return false;
    }

    public static boolean storeTripPointsLocally(Context context, String tripId, int points) {
        int scores = context.getSharedPreferences("ROVER_APP_STORE_TRIP_POINTS", MODE_PRIVATE).getInt(tripId, points);
        return true;
    }

    public static int calculateTripScore(int speedCounter, int brakeCounter, long tripDuration) {
        return (int) ((1/speedCounter)*(1/brakeCounter)*tripDuration);
    }
}