package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class DeepVDHLayout extends LinearLayout {
    private ViewDragHelper mDragger;

    /**
     * 可以自由拖动的view
     */
    private View mDragView;
    /**
     * 除了演示拖动，拖动后松手回到原本的位置。（拖动的越快，返回的越快）
     */
    private View mAutoBackView;
    /**
     * 边界移动时对view进行捕获，只能从边界拖动的view
     */
    private View mEdgeTrackerView;

    private Point mAutoBackOriginPos=new Point();
    public DeepVDHLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mDragger=ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NonNull View view, int i) {
                //mEdgeTrackerView禁止移动
                return view==mDragView||view==mAutoBackView;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                int leftBound=getPaddingLeft();
                int rightBound=getWidth()-child.getWidth()-getPaddingRight();
                int newLeft=Math.min(rightBound,Math.max(leftBound,left));
                return newLeft;
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
                int topBound=getPaddingTop();
                int bottomBound=getHeight()-child.getHeight()-getPaddingBottom();
                int newTop=Math.min(Math.max(topBound,top),bottomBound);
                return newTop;
            }

            /**
             * 手指释放的时候回调。
             * <p>判断如果是mAutoBackView，就调用settleCapturedViewAt()方法回到初始的位置。
             * 大家可以看到紧随其后的代码是invalidate();
             * <p>因为其内部使用的是mScroller.scroll()，所以别忘了invalidate()以及结合computeScroll()使用。
             *
             */
            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                if(releasedChild==mAutoBackView){
                    mDragger.settleCapturedViewAt(mAutoBackOriginPos.x,mAutoBackOriginPos.y);
                    invalidate();
                }
            }

            /**
             * 在边界拖动时回调。
             * <p>我们在onEdgeDragStarted()回调方法中，主要通过captureChildView()对其进行捕获，
             * 该方法可以绕过tryCaptureView，所以我们的tryCaptureView虽然并未返回true，但却不影响。
             * <p>注意，如果需要使用边界检测，需要加上setEdgeTrackingEnabled()。
             */
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                mDragger.captureChildView(mEdgeTrackerView,pointerId);
            }


            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return getMeasuredWidth()-child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return getMeasuredHeight()-child.getMeasuredHeight();
            }
        });
        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        mAutoBackOriginPos.x=mAutoBackView.getLeft();
        mAutoBackOriginPos.y=mAutoBackView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView=getChildAt(0);
        mAutoBackView=getChildAt(1);
        mEdgeTrackerView=getChildAt(2);
    }

    @Override
    public void computeScroll() {
        if(mDragger.continueSettling(true)){
            invalidate();
        }
    }
}
