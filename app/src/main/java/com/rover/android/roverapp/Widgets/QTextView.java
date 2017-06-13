package com.rover.android.roverapp.Widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by DEV on 28-08-2016.
 */
public class QTextView extends TextView {

    public QTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/linotte_semibold.otf"));
    }

}
