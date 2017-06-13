package com.rover.android.roverapp.Fragments;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rover.android.roverapp.Activities.HomeActivity;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Receiver.NotificationHandler;
import com.rover.android.roverapp.Services.LocationUpdateService;
import com.rover.android.roverapp.Widgets.QTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment{

    // UI Components
    public boolean mIsServiceStarted = false;

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String ACTION_STOP = "STOP_ACTION";
    public static final String ACTION_FROM_NOTIFICATION = "isFromNotification";
    private String action;
    private int notifID;

    private static DashboardFragment currentDashboardFragment;

    public static DashboardFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt("argsInstance", instance);
        DashboardFragment dashboardFragment = new DashboardFragment();
        return dashboardFragment;
    }

    public static DashboardFragment getCurrentInstance() {
        return currentDashboardFragment;
    }

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDashboardFragment = this;
//        startRecordInBackground();
    }

    // UI Components
    private QTextView dashFragDistTv;
    private QTextView dashFragTimeTv;
    private QTextView dashFragAvgSpeedTv;
    private QTextView dashFragMaxSpeedTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize UI Components
        dashFragDistTv = (QTextView) view.findViewById(R.id.dashFragDistTv);
        dashFragTimeTv = (QTextView) view.findViewById(R.id.dashFragTimeTv);
        dashFragAvgSpeedTv = (QTextView) view.findViewById(R.id.dashFragAvgSpeedTv);
        dashFragMaxSpeedTv = (QTextView) view.findViewById(R.id.dashFragMaxSpeedTv);

        // Download Data From Firebase and upload on respective Text Views
        downloadDataToPopulateViewFromFirebase();

        return view;
    }

    private float calculateAverageSpeed(List <Float> marks) {
        float sum = 0;
        if(!marks.isEmpty()) {
            for (Float mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private void downloadDataToPopulateViewFromFirebase() {
        DatabaseReference tripSpeedDetailsRef = FirebaseDatabase.getInstance().getReference("tripSpeedDetails/" + ((HomeActivity) getActivity()).getUid());
        tripSpeedDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Float> speedList = new ArrayList<Float>();
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot trips : dataSnapshot.getChildren()) {
                        for (DataSnapshot timestamps : trips.getChildren()) {
                            speedList.add(Float.parseFloat(timestamps.child("speed").getValue().toString()));
                        }
                    }
                    dashFragAvgSpeedTv.setText(Float.toString(round(calculateAverageSpeed(speedList),2)));
                    dashFragMaxSpeedTv.setText(Float.toString(round(Collections.max(speedList),2)));
                }else{
                    dashFragAvgSpeedTv.setText("0");
                    dashFragMaxSpeedTv.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        DatabaseReference tripsDetailsRef = FirebaseDatabase.getInstance().getReference("trips/" + ((HomeActivity) getActivity()).getUid());
        tripsDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float totalDistance = 0;
                int totalDuration = 0;
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot trips : dataSnapshot.getChildren()) {
                        totalDistance += Float.parseFloat(trips.child("tripDistance").getValue().toString());
                        totalDuration += Integer.parseInt(trips.child("tripDuration").getValue().toString());
                    }
                    dashFragTimeTv.setText(Integer.toString(totalDuration/3600));
                    dashFragDistTv.setText(Float.toString((float) Math.round(totalDistance * 100) / 100));
                }else{
                    dashFragTimeTv.setText("0");
                    dashFragDistTv.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void stopRecordInBackground() {
        if (mIsServiceStarted) {
            mIsServiceStarted = false;
            cancelNotification(getActivity(), notifID);
            getActivity().stopService(new Intent(getActivity(), LocationUpdateService.class));
        }
    }

    private void startRecordInBackground() {
        if (!mIsServiceStarted) {
            mIsServiceStarted = true;
            OnGoingLocationNotification(getActivity());
            getActivity().startService(new Intent(getActivity(), LocationUpdateService.class));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().getIntent().getAction() != null) {
            action = getActivity().getIntent().getAction();
            notifID = getActivity().getIntent().getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            if (action.equalsIgnoreCase(ACTION_FROM_NOTIFICATION)) {
                mIsServiceStarted = true;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopRecordInBackground();
    }

    public static void OnGoingLocationNotification(Context mcontext) {

        int mNotificationId;
        mNotificationId = 0;
        Intent mstopReceive = new Intent(mcontext, NotificationHandler.class);
        mstopReceive.putExtra(EXTRA_NOTIFICATION_ID, mNotificationId);
        mstopReceive.setAction(ACTION_STOP);
        PendingIntent pendingIntentStopService = PendingIntent.getBroadcast(mcontext, (int) System.currentTimeMillis(), mstopReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mcontext)
                        .setSound(alarmSound)
                        .setSmallIcon(R.drawable.ic_play_black_24dp)
                        .setContentTitle("Rover")
                        .addAction(R.drawable.ic_pause_black_24dp, "Pause Service", pendingIntentStopService)
                        .setOngoing(true).setContentText("Running...");

        mBuilder.setAutoCancel(false);

        Intent resultIntent = new Intent(mcontext, HomeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setAction(ACTION_FROM_NOTIFICATION);
        resultIntent.putExtra(EXTRA_NOTIFICATION_ID, mNotificationId);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mcontext, (int) System.currentTimeMillis(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(mNotificationId);

        Notification mNotification = mBuilder.build();
        mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(mNotificationId, mNotification);

    }

    private void cancelNotification(Context mContext, int mnotinotifId) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mnotinotifId);
    }
}
