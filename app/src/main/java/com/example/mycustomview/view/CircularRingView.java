package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.Map;

public class CircularRingView extends View {
    private int firstColor;
    private int secondColor;
    private int speed;
    private int ringWidth;

    private int progress;
    /**
     * 标记当前正在画的是不是firstColor的圆。
     */
    private boolean isFirstCircle;
    /**
     * 标记当前是不是第一次画。
     */
    private boolean isInit;

    private Paint mPaint;
    private RectF rectF;

    public CircularRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircularRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray=context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircularRingView,defStyleAttr,0);

        firstColor=typedArray.getColor(R.styleable.CircularRingView_firstColor,Color.BLACK);
        secondColor=typedArray.getColor(R.styleable.CircularRingView_secondColor,Color.GRAY);
        speed=typedArray.getInt(R.styleable.CircularRingView_speed,20);
        ringWidth=typedArray.getDimensionPixelOffset(R.styleable.CircularRingView_ringWidth,DisplayUtil.dp2px(5));
        typedArray.recycle();

        mPaint=new Paint();
        rectF=new RectF();
        progress=0;
        isFirstCircle=true;
        isInit=true;


        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(progress==360){
                        progress=0;
                        isInit=false;
                        if(isFirstCircle){
                            isFirstCircle=false;
                        }else {
                            isFirstCircle = true;
                        }
                    }
                    progress++;
                    try {
                        Thread.sleep(speed);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    postInvalidate();
                }
            }
        }).start();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(ringWidth);

        float center=getWidth()*1.0f/2;
        float radius=center-ringWidth*1.0f/2;
        rectF.left=center-radius;
        rectF.top=center-radius;
        rectF.right=center+radius;
        rectF.bottom=center+radius;


        if(isInit){
            mPaint.setColor(firstColor);
            canvas.drawArc(rectF,-90,progress,false,mPaint);
        }else{
            if(isFirstCircle){
                mPaint.setColor(secondColor);
                canvas.drawCircle(center,center,radius,mPaint);
                mPaint.setColor(firstColor);
                canvas.drawArc(rectF,-90,progress,false,mPaint);
            }else{
                mPaint.setColor(firstColor);
                canvas.drawCircle(center,center,radius,mPaint);
                mPaint.setColor(secondColor);
                canvas.drawArc(rectF,-90,progress,false,mPaint);
            }

            mPaint.setStrokeWidth(3);
            canvas.drawRect(rectF,mPaint);

        }
    }
}
