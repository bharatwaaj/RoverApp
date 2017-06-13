package com.rover.android.roverapp.Widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Boopalan on 11-Sep-2016.
 */
public class QEditText extends EditText{

    public QEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/linotte_semibold.otf"));
    }
}
