package com.example.mycustomview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.mycustomview.R;

public class MeiTuanRefreshSecondStepView extends View {
    private Bitmap endBitmap;

    public MeiTuanRefreshSecondStepView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init(){
        endBitmap=Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.pull_end_image_frame_05));
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int width;
        int height;
        if(widthMode==MeasureSpec.EXACTLY){
            width=widthSize;
        }else{
            width=endBitmap.getWidth();
            if(widthMode==MeasureSpec.AT_MOST){
                width=Math.min(width,widthSize);
            }
        }
        if(heightMode==MeasureSpec.EXACTLY){
            height=heightSize;
        }else{
            height=width*endBitmap.getHeight()/endBitmap.getWidth();
        }
        setMeasuredDimension(width,height);
    }
}
