package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 这只是一个简单的ImageView，可以存放Bitmap和Path等信息
 */
public class DataImageView extends AppCompatImageView {
    private String absolutePath;
    private Bitmap bitmap;

    public DataImageView(Context context) {
        this(context,null);
    }
    public DataImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public DataImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
