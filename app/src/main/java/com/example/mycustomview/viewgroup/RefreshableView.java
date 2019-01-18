package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;


public class RefreshableView extends LinearLayout{
    public RefreshableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}
