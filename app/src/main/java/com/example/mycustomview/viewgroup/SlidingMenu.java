package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;
import com.example.mycustomview.utils.LogUtil;

/**
 * 侧滑菜单，参考：<href>https://blog.csdn.net/lmj623565791/article/details/39185641</href>
 *
 */
public class SlidingMenu extends HorizontalScrollView {
    private int mScreenWidth;
    private int mMenuRightPadding;
    private int mMenuWidth;
    private int mHalfMenuWidth;
    private boolean once;
    private boolean isOpen;
    private boolean isSliding;
    private int touchSlop;
    private float xDown;
    private float xMove;

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
        mScreenWidth=getResources().getDisplayMetrics().widthPixels;
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.SlidingMenu,defStyleAttr,0);
        mMenuRightPadding=typedArray.getDimensionPixelSize(R.styleable.SlidingMenu_rightPadding,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50,getResources().getDisplayMetrics()));
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.e("onMeasure");
        if(!once){
            LinearLayout wrapper=(LinearLayout)getChildAt(0);
            ViewGroup menu=(ViewGroup)wrapper.getChildAt(0);
            ViewGroup content=(ViewGroup)wrapper.getChildAt(1);
            mMenuWidth=mScreenWidth-mMenuRightPadding;
            mHalfMenuWidth=mMenuWidth/2;
            menu.getLayoutParams().width=mMenuWidth;
            content.getLayoutParams().width=mScreenWidth;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogUtil.e("onLayout");
        super.onLayout(changed, l, t, r, b);
        if(changed){
            this.scrollTo(mMenuWidth,0);
            once=true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action=ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                LogUtil.e("ACTION_DOWN");
                xDown=ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtil.e("ACTION_MOVE");
                xMove=ev.getRawX();
                int offset=(int)(xMove-xDown);
                if(Math.abs(offset)>touchSlop){
                    isSliding=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.e("ACTION_UP");
                int scrollX=getScrollX();
                if(isSliding){
                    LogUtil.e("正在滑动中");
                    if(scrollX>mHalfMenuWidth){
                        this.smoothScrollTo(mMenuWidth,0);
                        isOpen=false;
                    }else{
                        this.smoothScrollTo(0,0);
                        isOpen=true;
                    }
                    isSliding=false;
                }else{
                    LogUtil.e("不是滑动中");
                }
                return true;
        }
        return super.onTouchEvent(ev);
    }
    public void openMenu(){
        if(isOpen)
            return;
        this.smoothScrollTo(0,0);
        isOpen=true;
    }
    public void closeMenu(){
        if(isOpen){
            this.smoothScrollTo(mMenuWidth,0);
            isOpen=false;
        }
    }
    public void toggle(){
        if(isOpen)
            closeMenu();
        else
            openMenu();
    }
}
