package com.rover.android.roverapp.Widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rover.android.roverapp.R;


/**
 * Created by Bharatwaaj on 11-09-2016.
 */
public class QToast extends Toast {

    View mRootView;
    TextView mMessageTextView;
    Context mContext;

    public QToast(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mRootView = inflater.inflate(R.layout.view_q_toast, null);
        mMessageTextView = (TextView) mRootView.findViewById(R.id.text);
        setView(mRootView);
    }

    public void showMessage(String message, int duration) {
        mMessageTextView.setText(message);
        setDuration(duration);
        show();
    }

    public void showMessage(int messageResId, int duration) {
        mMessageTextView.setText(mContext.getResources().getString(messageResId));
        setDuration(duration);
        show();
    }

    public void setBackgroundColor(Drawable drawable){
        mRootView.setBackgroundDrawable(drawable);
    }

}
