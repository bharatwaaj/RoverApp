package com.rover.android.roverapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rover.android.roverapp.Activities.CalendarActivity;
import com.rover.android.roverapp.Activities.DashboardActivity;
import com.rover.android.roverapp.Activities.RedeemActivity;
import com.rover.android.roverapp.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    public static HomeFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt("argsInstance", instance);
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initUI(view);
        return view;
    }

    // UI Components
    private ImageView goToDashboardAct;
    private ImageView goToCalendarAct;
    private ImageView goToRedeemAct;

    private void initUI(View view) {

        // Initialize UI Components
        goToDashboardAct = (ImageView) view.findViewById(R.id.goToDashboardAct);
        goToCalendarAct = (ImageView) view.findViewById(R.id.goToCalendarAct);
        goToRedeemAct = (ImageView) view.findViewById(R.id.goToRedeemAct);

        // Handle On Click Events
        goToDashboardAct.setOnClickListener(this);
        goToCalendarAct.setOnClickListener(this);
        goToRedeemAct.setOnClickListener(this);
    }

    // Handle On CLick Events here
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.goToDashboardAct:
                startActivity(new Intent(getActivity(), DashboardActivity.class));
                break;

            case R.id.goToCalendarAct:
                startActivity(new Intent(getActivity(), CalendarActivity.class));
                break;

            case R.id.goToRedeemAct:
                startActivity(new Intent(getActivity(), RedeemActivity.class));
                break;
        }
    }
}
