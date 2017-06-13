package com.rover.android.roverapp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rover.android.roverapp.R;

public class TripsFragment extends Fragment {



    public static TripsFragment newInstance(int instance) {

        // Required empty public constructor
        Bundle args = new Bundle();
        args.putInt("argsInstance", instance);
        TripsFragment tripsFragment = new TripsFragment();
        return tripsFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trips, container, false);
        return view;

    }

}