package com.rover.android.roverapp.ViewHolders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rover.android.roverapp.Models.Trips;
import com.rover.android.roverapp.Others.Const;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Widgets.QTextView;

import java.text.DateFormat;
import java.util.Date;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Created by Bharatwaaj on 01-06-2017.
 */

public class TripsViewHolder extends RecyclerView.ViewHolder {

    private QTextView timeTV;
    private QTextView fromToTV;
    private QTextView kmsTimeTV;
    private MaterialRatingBar tripsRatingBar;

    public TripsViewHolder(View itemView) {
        super(itemView);
        timeTV = (QTextView) itemView.findViewById(R.id.timeTV);
        fromToTV = (QTextView) itemView.findViewById(R.id.fromToTV);
        kmsTimeTV = (QTextView) itemView.findViewById(R.id.kmsTimeTV);
        tripsRatingBar = (MaterialRatingBar) itemView.findViewById(R.id.tripsRatingBar);
    }

    public void bindToTrips(Context context, Trips trips) {
        fromToTV.setText(trips.getTripSourceAddr(context) + "-" + trips.getTripDestAddr(context));
        timeTV.setText(parseTimeStamp(trips.getTripStartTime()));
        kmsTimeTV.setText(Const.round(trips.getTripDistance(), 2).toString());
        tripsRatingBar.setRating(trips.getTripRate());
    }

    private String parseTimeStamp(long milliseconds) {
        return DateFormat.getDateTimeInstance().format(new Date(milliseconds * 1000));
    }

}
