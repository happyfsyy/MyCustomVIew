package com.example.mycustomview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class MyTextView extends TextView {
    private static final String TAG = "MyTextView";
    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint=this.getPaint();
        Paint.FontMetrics fontMetrics=paint.getFontMetrics();
        float ascent=fontMetrics.ascent;
        float descent=fontMetrics.descent;
        float top=fontMetrics.top;
        float bottom=fontMetrics.bottom;
        float leading=fontMetrics.leading;
        Log.e(TAG,"text_ascent: "+ascent);
        Log.e(TAG,"text_descent: "+descent);
        Log.e(TAG,"text_top: "+top);
        Log.e(TAG,"text_bottom: "+bottom);
        Log.e(TAG,"text_leading: "+leading);
        Log.e(TAG,"textview_measured_height: "+getMeasuredHeight());
        Log.e(TAG,"textview_height: "+getHeight());

    }
}
