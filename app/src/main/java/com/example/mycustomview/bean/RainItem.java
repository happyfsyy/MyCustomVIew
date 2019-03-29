package com.example.mycustomview.bean;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.mycustomview.utils.DisplayUtil;

import java.util.Random;

public class RainItem {
    private int width;
    private int height;
    private float startX=0;
    private float startY=0;
    private float stopX=0;
    private float stopY=0;
    private float sizeX=0;
    private float sizeY=0;
    private Paint paint;
    private float opt;
    private Random random;

    public RainItem(int width,int height){
        this.width=width;
        this.height=height;
        init();
    }
    private void init(){
        random=new Random();
        //随机改变雨点的角度
        sizeX=100+random.nextInt(100);
        sizeY=50+random.nextInt(200);
        startX=random.nextInt(width);
        startY=random.nextInt(height);
        stopX=startX+sizeX;
        stopY=startY+sizeY;
        paint=new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(DisplayUtil.dp2px(2));
    }
    public void draw(Canvas canvas){
        canvas.drawLine(startX,startY,stopX,stopY,paint);
    }
    public void move(){
        opt=0.2f+random.nextFloat();
        startX+=sizeX*opt;
        stopX+=sizeX*opt;
        startY+=sizeY*opt;
        stopY+=sizeY*opt;
        if(startY>height&&startX>width){
            startX=random.nextInt(width);
            startY=random.nextInt(height);
            stopX=startX+sizeX;
            stopY=startY+sizeY;
        }
    }

}
