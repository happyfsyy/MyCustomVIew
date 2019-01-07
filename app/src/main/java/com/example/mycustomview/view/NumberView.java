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

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.Random;

public class NumberView extends View {
    private String numberText;
    private int numberColor;
    private int numberTextSize;
    private Paint mPaint;
    private Rect mBound;

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
        mBound=new Rect();
        mPaint.getTextBounds(numberText,0,numberText.length(),mBound);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                numberText=randomText();
                postInvalidate();
            }
        });
    }

    private String randomText(){
        Random random=new Random();
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<4;i++){
            int a=random.nextInt(10);
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
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
            int textWidth=(int)mPaint.measureText(numberText);
            width=getPaddingLeft()+textWidth+getPaddingRight();
        }
        if(heightMode==MeasureSpec.EXACTLY){
            height=heightSize;
        }else{
            int textHeight=mBound.height();
            height=getPaddingTop()+textHeight+getPaddingBottom();
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.YELLOW);
        canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);
        int distance=mBound.top+mBound.bottom;
        float offset=(float)(distance*1.0/2);
        float y=getHeight()/2-offset;
        mPaint.setColor(numberColor);
        Log.e(TAG,"strokeWidth: "+mPaint.getStrokeWidth());
        canvas.drawText(numberText,getWidth()/2-mPaint.measureText(numberText)/2,
                y,mPaint);
    }
}
