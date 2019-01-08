package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Paint.FontMetricsInt;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.Map;

public class ImageIntroView extends View {
    private String introText;
    private int introTextSize;
    private int introTextColor;
    private int imageResourceId;
    private int imageScaleType;
    private Bitmap mImage;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private Rect rect;
    private TextPaint textPaint;

    private int imageWidth;
    private int imageHeight;
    private int introTextWidth;
    private int introTextHeight;

    public static int FIT_XY=0;



    public ImageIntroView(Context context) {
        this(context,null);
    }

    public ImageIntroView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ImageIntroView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.ImageIntroView,defStyleAttr,0);
        introText=typedArray.getString(R.styleable.ImageIntroView_android_text);
        introTextSize=typedArray.getDimensionPixelSize(R.styleable.ImageIntroView_introTextSize,DisplayUtil.sp2px(16));
        introTextColor=typedArray.getColor(R.styleable.ImageIntroView_introTextColor,Color.BLACK);
        imageResourceId=typedArray.getResourceId(R.styleable.ImageIntroView_imageSrc,0);
        imageScaleType=typedArray.getInt(R.styleable.ImageIntroView_imageScaleType,-1);
        typedArray.recycle();

        mImage=BitmapFactory.decodeResource(context.getResources(),imageResourceId);
        mPaint=new Paint();
        rect=new Rect();
        textPaint=new TextPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);

        mPaint.setTextSize(introTextSize);
        introTextWidth=(int)mPaint.measureText(introText);
        imageWidth=mImage.getWidth();
        imageHeight=mImage.getHeight();
        FontMetricsInt fontMetricsInt=mPaint.getFontMetricsInt();
        introTextHeight=fontMetricsInt.bottom-fontMetricsInt.top;

        if(widthMode==MeasureSpec.EXACTLY){
            mWidth=widthSize;
        }else{
            int desireWidth=getPaddingLeft()+Math.max(imageWidth,introTextWidth)+getPaddingRight();
            mWidth=Math.min(widthSize,desireWidth);
        }

        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        if(heightMode==MeasureSpec.EXACTLY){
            mHeight=heightSize;
        }else{
            int desireHeight=getPaddingTop()+Math.max(imageHeight,introTextHeight)+getPaddingBottom();
            mHeight=Math.min(heightSize,desireHeight);
        }
        setMeasuredDimension(mWidth,mHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.CYAN);
        canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);

        rect.left=getPaddingLeft();
        rect.right=getWidth()-getPaddingRight();
        rect.top=getPaddingTop();
        rect.bottom=getHeight()-getPaddingBottom();
        int spareWidth=rect.width();

        mPaint.setColor(introTextColor);
        mPaint.setStyle(Paint.Style.FILL);

        FontMetricsInt fontMetricsInt=mPaint.getFontMetricsInt();
        float startY=mHeight-getPaddingBottom()-fontMetricsInt.descent;

        if(introTextWidth>spareWidth){
            textPaint.set(mPaint);
            String ellipText=TextUtils.ellipsize(introText,textPaint,spareWidth,TextUtils.TruncateAt.END).toString();
            int startX=getPaddingLeft();
            canvas.drawText(ellipText,startX,startY,mPaint);
        }else{
            float startX=spareWidth*1.0f/2-introTextWidth*1.0f/2+getPaddingLeft();
            canvas.drawText(introText,startX,startY,mPaint);
        }
        rect.bottom=rect.bottom-introTextHeight;

        if(imageScaleType==FIT_XY){
            canvas.drawBitmap(mImage,null,rect,mPaint);
        }else{
            rect.left=spareWidth/2-imageWidth/2+getPaddingLeft();
            rect.right=spareWidth/2+imageWidth/2+getPaddingLeft();
            int spareHeight=rect.height();
            rect.top=spareHeight/2-introTextHeight/2+getPaddingTop();
            rect.bottom=spareHeight/2+introTextHeight/2+getPaddingTop();
            canvas.drawBitmap(mImage,null,rect,mPaint);
        }
    }
}
