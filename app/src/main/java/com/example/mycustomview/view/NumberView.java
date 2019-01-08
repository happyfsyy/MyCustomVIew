package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.Random;

public class NumberView extends View {
    private String numberText;
    private int numberColor;
    private int numberTextSize;
    private Paint mPaint;
    private float mWidth;
    private float mHeight;

    private static final String TAG = "NumberView";

    public NumberView(Context context) {
        this(context,null);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int defaultColor=Color.BLACK;
        numberColor=defaultColor;
        int defaultTextSize=DisplayUtil.sp2px(16);
        numberTextSize=defaultTextSize;


        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.NumberView,defStyleAttr,0);
        int indexCount=typedArray.getIndexCount();
        int length=typedArray.length();
        Log.e(TAG,"indexCount: "+indexCount);
        Log.e(TAG,"length: "+length);


        for(int i=0;i<indexCount;i++){
            int index=typedArray.getIndex(i);
            switch (index){
                case R.styleable.NumberView_numberText:
                    numberText=typedArray.getString(index);
                    break;
                case R.styleable.NumberView_numberColor:
                    numberColor=typedArray.getColor(index,defaultColor);
                    break;
                case R.styleable.NumberView_numberSize:
                    numberTextSize=typedArray.getDimensionPixelSize(index,defaultTextSize);
                    break;
                default:
                    break;
            }
        }
        typedArray.recycle();

        mPaint=new Paint();
        mPaint.setTextSize(numberTextSize);
        Rect mBound=new Rect();
        mPaint.getTextBounds(numberText,0,numberText.length(),mBound);

        mWidth=mPaint.measureText(numberText);
        Log.e(TAG,"mBound.width(): "+mBound.width());
        Log.e(TAG,"mPaint.measureText(): "+mWidth);

        FontMetrics fontMetrics=mPaint.getFontMetrics();
        //在Top，ascent，bottom，descent画线
        mHeight=fontMetrics.bottom-fontMetrics.top;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int width,height;
        if(widthMode==MeasureSpec.EXACTLY){
            width=widthSize;
        }else{
            width=getPaddingLeft()+(int)mWidth+getPaddingRight();
        }
        if(heightMode==MeasureSpec.EXACTLY){
            height=heightSize;
        }else{
            height=getPaddingTop()+(int)mHeight+getPaddingBottom();
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setColor(numberColor);


        FontMetrics fontMetrics=mPaint.getFontMetrics();
        float y=getHeight()/2-fontMetrics.descent+(fontMetrics.bottom-fontMetrics.top)/2;
        Log.e(TAG,"ascent: "+fontMetrics.ascent);
        Log.e(TAG,"descent: "+fontMetrics.descent);
        Log.e(TAG,"top: "+fontMetrics.top);
        Log.e(TAG,"bottom: "+fontMetrics.bottom);


        float y1=getHeight()/2-fontMetrics.descent+(fontMetrics.descent-fontMetrics.ascent)/2;
        Log.e(TAG,"baseline: "+y);
        Log.e(TAG,"y1: "+y1);

        canvas.drawText(numberText,getWidth()/2-mWidth/2,
                y,mPaint);
//        mPaint.setColor(Color.RED);
//        mPaint.setStrokeWidth(1);
//        float ascent=fontMetrics.ascent+y;
//        canvas.drawLine(0,ascent,getWidth(),ascent,mPaint);
//        mPaint.setColor(Color.YELLOW);
//        float top=fontMetrics.top+y;
//        canvas.drawLine(0,top,getWidth(),top,mPaint);
//
//        mPaint.setColor(Color.MAGENTA);
//        float descent=fontMetrics.descent+y;
//        canvas.drawLine(0,descent,getWidth(),descent,mPaint);
//
//        mPaint.setColor(Color.BLUE);
//        float bottom=fontMetrics.bottom+y;
//        canvas.drawLine(0,bottom,getWidth(),bottom,mPaint);
    }
}
