package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;

public class VolumeControlView extends View {
    private int firstColor;
    private int secondColor;
    private int dotCount;
    private int dotWidth;
    private int splitSize;
    private Bitmap bgImage;

    private Paint mPaint;
    private RectF oval;
    private int currentDotCount=3;
    private Rect innerRect;
    private float xDown;
    private float xUP;


    public VolumeControlView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VolumeControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.VolumeControlView,defStyleAttr,0);
        firstColor=typedArray.getColor(R.styleable.VolumeControlView_firstColor,Color.RED);
        secondColor=typedArray.getColor(R.styleable.VolumeControlView_secondColor,Color.GREEN);
        dotCount=typedArray.getInt(R.styleable.VolumeControlView_dotCount,10);
        dotWidth=typedArray.getDimensionPixelSize(R.styleable.VolumeControlView_dotWidth,DisplayUtil.dp2px(5));
        splitSize=typedArray.getInt(R.styleable.VolumeControlView_splitSize,5);
        int bgImgId=typedArray.getResourceId(R.styleable.VolumeControlView_bgImage,R.drawable.nav_icon);
        bgImage=BitmapFactory.decodeResource(context.getResources(),bgImgId);
        typedArray.recycle();

        mPaint=new Paint();
        oval=new RectF();
        innerRect=new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStrokeWidth(dotWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        int mWidth=getWidth();
        float center=mWidth*1.0f/2;
        float radius=center-dotWidth/2;
        drawDots(canvas,center,radius);

        float realRadius=radius-dotWidth/2;
        int left=(int)(realRadius-(float)Math.sqrt(2)/2*realRadius)+dotWidth;
        int top=left;
        int bottom=(int)(realRadius+(float)Math.sqrt(2)/2*realRadius)+dotWidth;
        int right=bottom;
        innerRect.set(left,top,right,bottom);

        if(bgImage.getWidth()<innerRect.width()){
            innerRect.left=innerRect.left+(innerRect.width()-bgImage.getWidth())/2;
            innerRect.right=innerRect.left+bgImage.getWidth();
            innerRect.top=innerRect.top+(innerRect.height()-bgImage.getHeight())/2;
            innerRect.bottom=innerRect.top+bgImage.getHeight();
        }
        canvas.drawBitmap(bgImage,null,innerRect,mPaint);
    }

    private void drawDots(Canvas canvas,float center,float radius){
        float itemSize=(360*1.0f-dotCount*splitSize)/dotCount;
        oval.set(center-radius,center-radius,center+radius,center+radius);
        mPaint.setColor(firstColor);
        for(int i=0;i<dotCount;i++){
            canvas.drawArc(oval,i*(itemSize+splitSize),itemSize,false,mPaint);
        }
        mPaint.setColor(secondColor);
        for(int i=0;i<currentDotCount;i++){
            canvas.drawArc(oval,i*(itemSize+splitSize),itemSize,false,mPaint);
        }
    }

    private void down(){
        currentDotCount--;
        postInvalidate();
    }
    private void up(){
        currentDotCount++;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown=event.getX();
                break;
            case MotionEvent.ACTION_UP:
                xUP=event.getX();
                if(xDown<xUP){
                    down();
                }else{
                    up();
                }
                break;
        }
        return true;
    }
}

