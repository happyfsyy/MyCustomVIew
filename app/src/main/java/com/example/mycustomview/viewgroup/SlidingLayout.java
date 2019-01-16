package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
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
    private boolean isLeftLayoutVisible=false;
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

    public boolean isLeftLayoutVisible(){
        return isLeftLayoutVisible;
    }

    /**
     * 这个方法是用来使用childView.layout(l,t,r,b)的。
     * <p>没Move的时候，rightLayout的margin改变的时候，都会调用这个方法。
     * <p>如果将获取leftLayout和rightLayout放在super.onLayout()下面，那么就多执行一次onLayout
     * <p>这里的leftLayout和rightLayout感觉更适合放在onMeasure()之中进行执行
     * @param changed
     *          而changed，代表的是这个View的大小或者位置改变的时候才为true，子view的大小位置改变不影响changed
     *
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            Log.e(TAG,"Changed为true，执行onLayout()");
        }else{
            Log.e(TAG,"Changed为false，执行onLayout()");
        }
        if(changed){
            leftLayout=getChildAt(0);
            leftLayoutParams=(MarginLayoutParams)leftLayout.getLayoutParams();

            rightEdge=-leftLayoutParams.width;
            rightLayout=getChildAt(1);
            rightLayoutParams=(MarginLayoutParams)rightLayout.getLayoutParams();
            rightLayoutParams.width=screenWidth;
            rightLayout.setLayoutParams(rightLayoutParams);
            Log.e(TAG,"Changed为true，setLayoutParams()");
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        createVelocityTracker(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"MotionEvent: ACTION_DOWN");
                xDown=event.getRawX();
                yDown=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG,"MotionEvent: ACTION_MOVE");
                xMove=event.getRawX();
                yMove=event.getRawY();
                int moveDistanceX=(int)(xMove-xDown);
                int distanceY=(int)(yMove-yDown);
                if(!isLeftLayoutVisible&&moveDistanceX>=touchSlop
                        &&(isSliding||Math.abs(distanceY)<=touchSlop)){
                    isSliding=true;
                    rightLayoutParams.rightMargin=-moveDistanceX;
                    //与下一段的位置颠倒过来
                    if(rightLayoutParams.rightMargin<rightEdge){
                        rightLayoutParams.rightMargin=rightEdge;
                    }
                    rightLayout.setLayoutParams(rightLayoutParams);
                }
                if(isLeftLayoutVisible&&-moveDistanceX>=touchSlop
                        &&(isSliding||Math.abs(distanceY)<=touchSlop)){
                    isSliding=true;
                    rightLayoutParams.rightMargin=rightEdge-moveDistanceX;
                    //与上一段的代码颠倒过来
                    if(rightLayoutParams.rightMargin>leftEdge){
                        rightLayoutParams.rightMargin=leftEdge;
                    }
                    rightLayout.setLayoutParams(rightLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG,"MotionEvent: ACTION_UP");
                xUp=event.getRawX();
                int upDistanceX=(int)(xUp-xDown);
                if(isSliding){
                    if(wantToShowLeftLayout()){
                        if(shouldScrollToLeftLayout()){
                            scrollToLeftLayout();
                        }else{
                            scrollToRightLayout();
                        }
                    }else if(wantToShowRightLayout()){
                        if(shouldScrollToRightLayout()){
                            scrollToRightLayout();
                        }else{
                            scrollToLeftLayout();
                        }
                    }
                }else if(upDistanceX<touchSlop&&isLeftLayoutVisible){
                    //左侧布局显示，点击一下右侧布局，显示右侧布局
                    scrollToRightLayout();
                }
                recycleVelocityTracker();
                break;
        }
        if(v.isEnabled()){
            if(isSliding){
                unFocusBindView();
                return true;
            }
            if(isLeftLayoutVisible){
                return true;
            }
            return false;
        }
        return true;
    }

    class ScrollTask extends AsyncTask<Integer,Integer,Integer>{
        @Override
        protected Integer doInBackground(Integer... integers) {
            int speed=integers[0];
            int rightMargin=rightLayoutParams.rightMargin;
            while(true){
                rightMargin+=speed;
                if(rightMargin>leftEdge){
                    rightMargin=leftEdge;
                    break;
                }
                if(rightMargin<rightEdge){
                    rightMargin=rightEdge;
                    break;
                }
                publishProgress(rightMargin);
                try{
                    Thread.sleep(15);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(speed>0){
                isLeftLayoutVisible=false;
            }else{
                isLeftLayoutVisible=true;
            }
            isSliding=false;
            return rightMargin;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            rightLayoutParams.rightMargin=values[0];
            rightLayout.setLayoutParams(rightLayoutParams);
            unFocusBindView();
        }
        @Override
        protected void onPostExecute(Integer result) {
            rightLayoutParams.rightMargin=result;
            rightLayout.setLayoutParams(rightLayoutParams);
        }
    }

    private void unFocusBindView(){
        if(mBindView!=null){
            mBindView.setPressed(false);
            mBindView.setFocusable(false);
            mBindView.setFocusableInTouchMode(false);
        }
    }

    private boolean wantToShowLeftLayout(){
        return xUp-xDown>0&&!isLeftLayoutVisible;
    }
    private boolean wantToShowRightLayout(){
        return xUp-xDown<0&&isLeftLayoutVisible;
    }
    private boolean shouldScrollToLeftLayout(){
        return xUp-xDown>leftLayoutParams.width/2&&getScrollVelocity()>SNAP_VELOCITY;
    }
    private boolean shouldScrollToRightLayout(){
        return xDown-xUp>leftLayoutParams.width/2&&getScrollVelocity()>SNAP_VELOCITY;
    }
    public void scrollToLeftLayout(){
        new ScrollTask().execute(-30);
    }
    public void scrollToRightLayout(){
        new ScrollTask().execute(30);
    }

    private int getScrollVelocity(){
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity=(int)mVelocityTracker.getXVelocity();
        return Math.abs(velocity);
    }
    private void recycleVelocityTracker(){
        mVelocityTracker.recycle();
        mVelocityTracker=null;
    }

    private void createVelocityTracker(MotionEvent event){
        if(mVelocityTracker==null){
            mVelocityTracker=VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }
}
