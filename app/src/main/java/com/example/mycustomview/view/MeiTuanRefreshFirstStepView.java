package com.example.mycustomview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;

public class MeiTuanRefreshFirstStepView extends View {
    private Bitmap initBitmap;
    private Bitmap endBitmap;
    private int measuredWidth;
    private int measuredHeight;
    private Bitmap scaledBitmap;
    private float progress;


    public MeiTuanRefreshFirstStepView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams(context);
    }
    private void initParams(Context context){
        initBitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.pull_image);
        endBitmap=BitmapFactory.decodeResource(context.getResources(),R.drawable.pull_end_image_frame_05);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        LogUtil.e("onMeasure()");
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        if(widthMode==MeasureSpec.EXACTLY){
            measuredWidth=widthSize;
        }else{
            measuredWidth=endBitmap.getWidth();
            measuredWidth=Math.min(measuredWidth,widthSize);
        }
        if(heightMode==MeasureSpec.EXACTLY){
            measuredHeight=heightSize;
        }else{
            measuredHeight=measuredWidth*endBitmap.getHeight()/endBitmap.getWidth();
        }
        scaledBitmap=Bitmap.createScaledBitmap(initBitmap,measuredWidth,
                measuredWidth*initBitmap.getHeight()/initBitmap.getWidth(),true);
        setMeasuredDimension(measuredWidth,measuredHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        LogUtil.e("onDraw()");
        canvas.scale(progress,progress,measuredWidth/2,measuredHeight/2);
        canvas.drawBitmap(scaledBitmap,0,measuredHeight/4,null);
    }
    public void setProgress(float progress){
        this.progress=progress;
    }
}
