package com.example.mycustomview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.example.mycustomview.bean.RainItem;

import java.util.ArrayList;

public class RainView extends BaseView{
    private ArrayList<RainItem> rainList=new ArrayList<>();
    private int size=80;
    public RainView(Context context) {
        super(context);
    }

    public RainView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init() {
        for(int i=0;i<size;i++){
            RainItem item=new RainItem(getWidth(),getHeight());
            rainList.add(item);
        }
    }

    @Override
    public void logic() {
        for (RainItem item:rainList){
            item.move();
        }
    }

    @Override
    public void drawSub(Canvas canvas) {
        for(RainItem item:rainList){
            item.draw(canvas);
        }
    }
}
