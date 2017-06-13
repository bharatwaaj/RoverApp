package com.rover.android.roverapp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rover.android.roverapp.R;

public class LeaderBoardFragment extends Fragment {

    public static LeaderBoardFragment newInstance(int instance) {
        // Required empty public constructor
        Bundle args = new Bundle();
        args.putInt("argsInstance", instance);
        LeaderBoardFragment leaderBoardFragment = new LeaderBoardFragment();
        return leaderBoardFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

}