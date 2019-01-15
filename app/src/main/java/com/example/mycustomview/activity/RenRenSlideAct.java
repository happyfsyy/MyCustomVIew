package com.example.mycustomview.activity;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mycustomview.R;

public class RenRenSlideAct extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "RenRenSlideAct";
    private View menu;
    private View content;

    private LinearLayout.LayoutParams menuParams;
    private int menuWidth;
    private int screenWidth;
    private LinearLayout.LayoutParams contentParams;
    /**
     * leftMargin的最左边缘
     */
    private int leftEdge;
    /**
     * leftMargin的最右边缘
     */
    private int rightEdge;

    /**
     * 显示menu当前的状态是不是可见，完全显示为true，完全隐藏为false
     */
    private boolean isMenuVisible;

    private float xDown;
    private float xMove;
    private float xUp;
    /**
     * 局部变量
     */
    private int leftMargin;

    private VelocityTracker mVelocityTracker;
    private float xVelocity;

    private static final int SNAP_VELOCITY=1000;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renren_slide);

        init();
    }

    private void init(){
        menu=findViewById(R.id.renren_slide_menu);
        content=findViewById(R.id.renren_slide_content);
        //设置menu的宽度，以及隐藏menu
        menuParams=(LinearLayout.LayoutParams)menu.getLayoutParams();
        screenWidth=getResources().getDisplayMetrics().widthPixels;
        menuWidth=screenWidth*2/3;
        menuParams.width=menuWidth;
        menuParams.leftMargin=-menuWidth;
        //设置leftMargin的范围
        leftEdge=-menuWidth;
        rightEdge=0;
        //设置content的宽度，刚开始的适合显示content
        contentParams=(LinearLayout.LayoutParams)content.getLayoutParams();
        contentParams.width=screenWidth;
        //将menu进行隐藏
        isMenuVisible=false;
        //设置content的touchListener，也就是说只有触摸content才有用
        content.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        initVelocityTracker(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown=event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove=event.getRawX();
                //无论向左还是向右滑动，也就是说，无论滑动的距离是正还是负
                //我们都可以直接采用leftMargin+distance的办法
                //以为如果我们向左滑动，那么leftMargin也就更向左，如果我们向右，亦然。
                if(isMenuVisible){
                    leftMargin=(int)(xMove-xDown);
                }else{
                    leftMargin=-menuWidth+(int)(xMove-xDown);
                }
//                leftMargin=menuParams.leftMargin+(int)(xMove-xDown);
                if(leftMargin>rightEdge){
                    leftMargin=rightEdge;
                }
                if(leftMargin<leftEdge){
                    leftMargin=leftEdge;
                }
                menuParams.leftMargin=leftMargin;
                menu.setLayoutParams(menuParams);
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG,"ACTION_UP");
                xUp=event.getRawX();
                xVelocity=getXVelocity();
                if(wantToShowMenu()){
                    if(shouldScrollToMenu()){
                        scrollToMenu();
                    }else{
                        scrollToContent();
                    }
                }else if(wantToShowContent()){
                    if(shouldScrollToContent()){
                        scrollToContent();
                    }else{
                        scrollToMenu();
                    }
                }else{
                    if(!isMenuVisible){
                        scrollToMenu();
                    }else{
                        scrollToContent();
                    }
                    Log.e(TAG,">>>>>>>>>>Others>>>>>>>>");
                    if(xVelocity>0){
                        Log.e(TAG,"xVelocity>0");
                    }else{
                        Log.e(TAG,"xVelocity<0");
                    }
                    if(isMenuVisible){
                        Log.e(TAG,"isMenuVisible=true");
                    }else{
                        Log.e(TAG,"isMenuVisible=false");
                    }
                }
                recycleVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }


    class ScrollTask extends AsyncTask<Integer,Integer,Integer>{
        @Override
        protected Integer doInBackground(Integer... speed) {
            while(true){
                leftMargin=menuParams.leftMargin+speed[0];
                if(leftMargin<leftEdge){
                    leftMargin=leftEdge;
                    break;
                }
                if(leftMargin>rightEdge){
                    leftMargin=rightEdge;
                    break;
                }
                publishProgress(leftMargin);
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(speed[0]>0){
                isMenuVisible=true;
            }else{
                isMenuVisible=false;
            }
            return leftMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            menuParams.leftMargin=values[0];
            menu.setLayoutParams(menuParams);
        }

        @Override
        protected void onPostExecute(Integer result) {
            menuParams.leftMargin=result;
            menu.setLayoutParams(menuParams);
        }
    }

    private void initVelocityTracker(MotionEvent event){
        if(mVelocityTracker==null){
            mVelocityTracker=VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }
    private void recycleVelocityTracker(){
        mVelocityTracker.recycle();
        mVelocityTracker=null;
    }

    private float getXVelocity(){
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getXVelocity();
    }

    private boolean wantToShowMenu(){
        return !isMenuVisible&&xUp-xDown>0;
    }
    private boolean wantToShowContent(){
        return isMenuVisible&&xUp-xDown<0;
    }
    private boolean shouldScrollToMenu(){
        return xUp-xDown>menuWidth/2||Math.abs(xVelocity)>SNAP_VELOCITY;
    }
    private boolean shouldScrollToContent(){
        return xDown-xUp>menuWidth/2||Math.abs(xVelocity)>SNAP_VELOCITY;
    }
    private void scrollToMenu(){
        new ScrollTask().execute(30);
    }
    private void scrollToContent(){
        new ScrollTask().execute(-30);
    }
}
