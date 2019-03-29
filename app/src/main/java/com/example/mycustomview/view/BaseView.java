package com.example.mycustomview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class BaseView extends View {
    private Thread thread;
    private boolean running=true;
    public BaseView(Context context) {
        super(context);
    }
    public BaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    final protected void onDraw(Canvas canvas) {
        //禁止子类覆盖
        if(thread==null){
            thread=new MyThread();
            thread.start();
        }else{
            logic();
            drawSub(canvas);
        }
    }
    class MyThread extends Thread{
        @Override
        public void run() {
            init();
            while(running){
                postInvalidate();
                try{
                    Thread.sleep(40);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public abstract void init();
    public abstract void logic();
    public abstract void drawSub(Canvas canvas);

    @Override
    protected void onDetachedFromWindow() {
        running=false;
        super.onDetachedFromWindow();
    }
}
