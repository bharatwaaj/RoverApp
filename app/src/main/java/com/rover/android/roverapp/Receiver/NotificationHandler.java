package com.rover.android.roverapp.Receiver;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.rover.android.roverapp.Activities.DashboardActivity;
import com.rover.android.roverapp.Fragments.DashboardFragment;
import com.rover.android.roverapp.Services.LocationUpdateService;

public class NotificationHandler extends BroadcastReceiver {

    private Context mContxt;
    private static final String TAG = NotificationHandler.class.getSimpleName();

    public NotificationHandler(){}

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        mContxt = context;

        String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            int notifId = intent.getIntExtra(DashboardFragment.EXTRA_NOTIFICATION_ID, 0);
            Intent mintent = new Intent(context, LocationUpdateService.class);
            if (action.equalsIgnoreCase(DashboardActivity.ACTION_STOP)) {
                if (LocationUpdateService.isEnded) {
                    context.stopService(mintent);
                    cancelNotification(context, notifId);
                }
            }
        }

    }

    private void cancelNotification(Context mContext, int mnotinotifId) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mnotinotifId);
    }

}


                    /*if(DashboardFragment.getCurrentInstance() != null)
                        DashboardFragment.getCurrentInstance().stopRecordInBackground();*/