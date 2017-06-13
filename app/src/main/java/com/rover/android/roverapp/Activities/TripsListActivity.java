package com.rover.android.roverapp.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.rover.android.roverapp.Models.Trips;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.ViewHolders.TripsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TripsListActivity extends QBaseActivity {

    // UI Components
    private RecyclerView mRecycler;
    public ProgressDialog mProgressDialog;

    // Adapter Components
    private FirebaseRecyclerAdapter<Trips, TripsViewHolder> mAdapter;
    private LinearLayoutManager mManager;

    // Firebase Components
    private DatabaseReference mDatabase;

    // Array List Components
    private List<Trips> tripsList = new ArrayList<>();

    // Other Components
    private int selectedDate;
    private int selectedMonth;
    private int selectedYear;
    private String dateString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);
        CalendarDay calendarDay = (CalendarDay) getIntent().getExtras().get("selectedDate");
        selectedDate = calendarDay.getDate().getDate();
        selectedMonth = calendarDay.getMonth();
        selectedYear = calendarDay.getYear();
        dateString = selectedDate + "-" + selectedMonth + "-" + selectedYear;
        initUI();
    }

    private void initUI() {

        // Create the UI references here
        mRecycler = (RecyclerView) findViewById(R.id.tripsRecyclerView);

        // Loading Progress Dialog
        setUpProgressDialog();

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mManager);

        // Set up Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set up the Adapter
        mAdapter = new FirebaseRecyclerAdapter<Trips, TripsViewHolder>(Trips.class, R.layout.layout_content_trips,
                TripsViewHolder.class, mDatabase.child("trips/" + this.getUid())) {
            @Override
            protected void populateViewHolder(TripsViewHolder viewHolder, Trips model, int position) {
                viewHolder.bindToTrips(TripsListActivity.this, model);
                hideProgressDialog();
            }
        };

        mRecycler.setAdapter(mAdapter);

    }

    private void setUpProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }
}
