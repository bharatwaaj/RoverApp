package com.rover.android.roverapp.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Receiver.NotificationHandler;
import com.rover.android.roverapp.Services.LocationUpdateService;
import com.rover.android.roverapp.Widgets.QTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends QBaseActivity implements SensorEventListener {

    private GraphView graph;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LineGraphSeries<DataPoint> mSeries;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;

    private float lastX, lastY, lastZ;
    private double graph2LastXValue = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private int points = 0;

    private QTextView distanceTvNewDash;
    private QTextView timeTvNewDash;
    private QTextView avgSpeedTvNewDash;
    private QTextView maxSpeedTvNewDash;
    private QTextView tripPointsTvNewDash;

    public boolean mIsServiceStarted = false;

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String ACTION_STOP = "STOP_ACTION";
    public static final String ACTION_FROM_NOTIFICATION = "isFromNotification";
    private String action;
    private int notifID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        graph = (GraphView) findViewById(R.id.liveGraph);
        distanceTvNewDash = (QTextView) findViewById(R.id.distanceTvNewDash);
        timeTvNewDash = (QTextView) findViewById(R.id.timeTvNewDash);
        avgSpeedTvNewDash = (QTextView) findViewById(R.id.avgSpeedTvNewDash);
        maxSpeedTvNewDash = (QTextView) findViewById(R.id.maxSpeedTvNewDash);
        tripPointsTvNewDash = (QTextView) findViewById(R.id.tripPointsTvNewDash);

        mSeries = new LineGraphSeries<>();
        graph.addSeries(mSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Download Data From Firebase and upload on respective Text Views
        downloadDataToPopulateViewFromFirebase();

        // Start the service
        startRecordInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                mSeries.appendData(new DataPoint(graph2LastXValue, speed * 3.6), true, 40);
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer, 1000);

        if (getIntent().getAction() != null) {
            action = getIntent().getAction();
            notifID = getIntent().getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            if (action.equalsIgnoreCase(ACTION_FROM_NOTIFICATION)) {
                mIsServiceStarted = true;
            }
        }
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }

    long speed;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        deltaX = lastX - sensorEvent.values[0];
        deltaY = lastY - sensorEvent.values[1];
        deltaZ = lastZ - sensorEvent.values[2];

        long speedx = 0;
        long speedy = 0;
        long speedz = 0;
        speed = 0;

        speedx += Math.abs(deltaX);
        speedy += Math.abs(deltaY);
        speedz += Math.abs(deltaZ);

        speed = (long) Math.sqrt(Math.pow(speedx, 2) + Math.pow(speedy, 2) + Math.pow(speedz, 2));
        float speedInKmph = (float) (speed * 3.6);
        if(speedInKmph >= 18 && speedInKmph <= 25){
            points+=1;
        }else if(speedInKmph >25 && speedInKmph <=35){
            points+=2;
        }else if(speedInKmph >35 && speedInKmph <=50){
            points+=3;
        }
        tripPointsTvNewDash.setText(points + "");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void downloadDataToPopulateViewFromFirebase() {
        DatabaseReference tripSpeedDetailsRef = FirebaseDatabase.getInstance().getReference("tripSpeedDetails/" + this.getUid());
        tripSpeedDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Float> speedList = new ArrayList<Float>();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot trips : dataSnapshot.getChildren()) {
                        for (DataSnapshot timestamps : trips.getChildren()) {
                            speedList.add(Float.parseFloat(timestamps.child("speed").getValue().toString()));
                        }
                    }
                    avgSpeedTvNewDash.setText(Float.toString(round(calculateAverageSpeed(speedList), 2)));
                    maxSpeedTvNewDash.setText(Float.toString(round(Collections.max(speedList), 2)));
                } else {
                    avgSpeedTvNewDash.setText("0");
                    maxSpeedTvNewDash.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        DatabaseReference tripsDetailsRef = FirebaseDatabase.getInstance().getReference("trips/" + this.getUid());
        tripsDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float totalDistance = 0;
                int totalDuration = 0;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot trips : dataSnapshot.getChildren()) {
                        totalDistance += Float.parseFloat(trips.child("tripDistance").getValue().toString());
                        totalDuration += Integer.parseInt(trips.child("tripDuration").getValue().toString());
                    }
                    timeTvNewDash.setText(Integer.toString(totalDuration / 3600));
                    distanceTvNewDash.setText(Float.toString((float) Math.round(totalDistance * 100) / 100));
                } else {
                    timeTvNewDash.setText("0");
                    distanceTvNewDash.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private float calculateAverageSpeed(List<Float> marks) {
        float sum = 0;
        if (!marks.isEmpty()) {
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

    public void stopRecordInBackground() {
        if (mIsServiceStarted) {
            mIsServiceStarted = false;
            cancelNotification(this, notifID);
            stopService(new Intent(this, LocationUpdateService.class));
        }
    }

    private void startRecordInBackground() {
        if (!mIsServiceStarted) {
            mIsServiceStarted = true;
            OnGoingLocationNotification(this);
            startService(new Intent(this, LocationUpdateService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecordInBackground();
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
                        .addAction(R.drawable.ic_pause_black_24dp, "Pause Recording", pendingIntentStopService)
                        .setOngoing(true).setContentText("Recording...");

        mBuilder.setAutoCancel(false);

        Intent resultIntent = new Intent(mcontext, DashboardActivity.class);
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
