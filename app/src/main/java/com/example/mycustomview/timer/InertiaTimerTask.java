package com.example.mycustomview.timer;

import android.support.v7.view.menu.MenuWrapperFactory;

import com.example.mycustomview.view.WheelView;

import java.util.TimerTask;

public class InertiaTimerTask extends TimerTask {
    private float mCurrentVelocityY;//当前滑动速度
    private float mFirstVelocityY;//手指离开屏幕时的初始速度
    private WheelView wheelView;
    public InertiaTimerTask(WheelView wheelView,float velocityY){
        this.wheelView=wheelView;
        this.mFirstVelocityY=velocityY;
        mCurrentVelocityY=Integer.MAX_VALUE;
    }

    @Override
    public void run() {
        //防止闪动，对速度做一个限制
        if(mCurrentVelocityY==Integer.MAX_VALUE){
            if(Math.abs(mFirstVelocityY)>2000f){
                mCurrentVelocityY=mFirstVelocityY>0?2000f:-2000f;
            }else{
                mCurrentVelocityY=mFirstVelocityY;
            }
        }
        //发送handler消息，处理平顺停止滑动逻辑
        if(Math.abs(mCurrentVelocityY)>=0.0f&&Math.abs(mCurrentVelocityY)<=20f){
            wheelView.cancelFuture();
            wheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
            return;
        }
        int dy=(int)(mCurrentVelocityY/100f);
        wheelView.setTotalScrollY(wheelView.getTotalScrollY()-dy);
        if(!wheelView.isLoop()) {
            float itemHeight = wheelView.getItemHeight();
            float top = (-wheelView.getInitPosition()) * itemHeight;
            float bottom = (wheelView.getItemCount() - 1 - wheelView.getInitPosition()) * itemHeight;
            if (wheelView.getTotalScrollY() - itemHeight * 0.25 < top) {
                top = wheelView.getTotalScrollY() + dy;
            } else if (wheelView.getTotalScrollY() + itemHeight * 0.25 > bottom) {
                bottom = wheelView.getTotalScrollY() + dy;
            }
            if (wheelView.getTotalScrollY() <= top) {
                mCurrentVelocityY = 40f;
                wheelView.setTotalScrollY((int) top);
            } else if (wheelView.getTotalScrollY() >= bottom) {
                wheelView.setTotalScrollY((int) bottom);
                mCurrentVelocityY = -40f;
            }
        }
        if(mCurrentVelocityY<0.0f){
            mCurrentVelocityY=mCurrentVelocityY+20f;
        }else{
            mCurrentVelocityY=mCurrentVelocityY-20f;
        }
        wheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
    }
}
