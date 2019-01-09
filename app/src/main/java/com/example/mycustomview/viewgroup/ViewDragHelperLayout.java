package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ViewDragHelperLayout extends LinearLayout {
    private ViewDragHelper mDragger;
    public ViewDragHelperLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //创建实例需要三个参数，第一个参数就是当前的ViewGroup，第二个是Sensitivity,主要用于设置touchSlop，值越大越灵敏。
        mDragger=ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            /**
             * 返回true表示可以捕获该view，你可以根据传入的第一个参数决定哪些可以捕获。
             */
            @Override
            public boolean tryCaptureView(@NonNull View view, int i) {
                return true;
            }

            /**
             * 可以在该方法中对child的移动边界进行控制，left指示即将移动到的位置。
             * <p>比如横向的情况下，我只希望在ViewGroup的内部移动，即最下>=getPaddingLeft，
             * 最大<=viewGroup.getWidth-getPaddingRight-childView.getWidth
             */
            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                return top;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }
}
