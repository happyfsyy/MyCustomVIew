package com.example.mycustomview.utils;

import android.content.Context;
import android.util.TypedValue;

import com.example.mycustomview.activity.MyApplication;

public class DisplayUtil {
    private DisplayUtil(){
        throw new UnsupportedOperationException("can not be instantiated");
    }

    /**
     * dp值转化为px值
     */
    public static int dp2px(float dpValue){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpValue,
                MyApplication.getContext().getResources().getDisplayMetrics());
    }

    public static int sp2px(float spValue){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spValue,
                MyApplication.getContext().getResources().getDisplayMetrics());
    }


}
