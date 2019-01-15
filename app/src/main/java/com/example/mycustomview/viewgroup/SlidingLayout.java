package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class SlidingLayout extends RelativeLayout implements View.OnTouchListener {
    /**
     * 滚动显示和隐藏左侧布局时，手指滑动需要达到的速度。
     */
    public static final int SNAP_VELOCITY=200;
    private int screenWidth;
    /**
     * 右侧布局最多可以滑动到的左边缘
     */
    private int leftEdge=0;
    /**
     * 右侧布局最多可以滑动到的有边缘
     */
    private int rightEdge=0;
    /**
     * 在判定为滚动之前，用户手指可以移动的最大值。
     */
    private int touchSlop;
    private float xDown;
    private float yDown;
    private float xMove;
    private float yMove;
    private float xUp;
    /**
     * 判断左侧布局当前是显示还是隐藏。只有完全显示或隐藏时才会更改此值，
     * 滑动过程中此值无效。
     */
    private boolean isLeftLayoutVisible;
    /**
     * 判断是否正在滑动
     */
    private boolean isSliding;
    private View leftLayout;
    private View rightLayout;
    /**
     * 用于监听侧滑事件的view
     */
    private View mBindView;
    /**
     * 左侧布局的参数，通过此参数来重新确定左侧布局的宽度，
     * 以及更改leftMargin的值。
     */
    private MarginLayoutParams leftLayoutParams;
    private MarginLayoutParams rightLayoutParams;
    private VelocityTracker mVelocityTracker;

    private static final String TAG = "SlidingLayout";

    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG,"执行构造方法SlidingLayout()");
        screenWidth=context.getResources().getDisplayMetrics().widthPixels;
        //在系统认为是滑动之前，“触摸”能够走出的最远的距离，单位是像素
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 绑定监听侧滑事件的view，即在绑定的view进行滑动才可以显示和隐藏左侧布局
     * @param bindView
     *              需要绑定的view对象
     */
    public void setScrollEvent(View bindView){
        mBindView=bindView;
        mBindView.setOnTouchListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            Log.e(TAG,"super()之前，Changed为true");
        }else{
            Log.e(TAG,"super()之前，Changed为false");
        }
        super.onLayout(changed, l, t, r, b);
        if(changed){
            Log.e(TAG,"super()之后，Changed为true");
            leftLayout=getChildAt(0);
            leftLayoutParams=(MarginLayoutParams)leftLayout.getLayoutParams();
            //这里是改的，leftEdge，原本为rightEdge？？？？？？？？？？？？？？？？
            rightEdge=-leftLayoutParams.width;
            rightLayout=getChildAt(1);
            rightLayoutParams=(MarginLayoutParams)leftLayout.getLayoutParams();
            rightLayoutParams.width=screenWidth;
            rightLayout.setLayoutParams(rightLayoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);
        //这里有什么用？？？？？？？？？？？？？？？？？？？？？？？
        if(leftLayout.getVisibility()!=View.VISIBLE){
            leftLayout.setVisibility(View.VISIBLE);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown=event.getRawX();
                yDown=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove=event.getRawX();
                yMove=event.getRawY();
                int moveDistanceX=(int)(xMove-xDown);
                int distanceY=(int)(yMove-yDown);
                if(!isLeftLayoutVisible&&moveDistanceX>=touchSlop
                        &&(isSliding||Math.abs(distanceY)<=touchSlop)){
                    isSliding=true;
                    rightLayoutParams.rightMargin=-moveDistanceX;
                    //这里为什么？？？？？？？？？？？？？？？？？？？？？？？
                    //这里我改了，改rightLayout的右侧布局，rightMargin的最右边界
                    if(rightLayoutParams.rightMargin<leftEdge){
                        rightLayoutParams.rightMargin=leftEdge;
                    }
                    rightLayout.setLayoutParams(rightLayoutParams);
                }
                if(isLeftLayoutVisible&&-moveDistanceX>=touchSlop
                        &&(isSliding||Math.abs(distanceY)<=touchSlop)){
                    isSliding=true;
                    rightLayoutParams.rightMargin=rightEdge-moveDistanceX;







                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return false;
    }

    private void createVelocityTracker(MotionEvent event){
        if(mVelocityTracker==null){
            mVelocityTracker=VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }
}
