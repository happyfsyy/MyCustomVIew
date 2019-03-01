package com.example.mycustomview.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.example.mycustomview.view.WheelView;

public class LoopViewGestureListener extends GestureDetector.SimpleOnGestureListener {
    private final WheelView wheelView;

    public LoopViewGestureListener(WheelView wheelView) {
        this.wheelView=wheelView;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //todo wheelview.scrollby(velocityY)
        return super.onFling(e1, e2, velocityX, velocityY);
    }
}
