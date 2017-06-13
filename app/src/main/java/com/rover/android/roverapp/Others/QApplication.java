package com.rover.android.roverapp.Others;

import android.app.Application;
import android.content.Context;

import com.rover.android.roverapp.Receiver.ConnectivityReceiver;


/**
 * Created by Bharatwaaj on 22-09-2016.
 */
public class QApplication extends Application {

    private static QApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized QApplication getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }


    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}
