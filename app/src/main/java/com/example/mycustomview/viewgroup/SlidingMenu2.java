package com.example.mycustomview.viewgroup;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.MenuPopupWindow;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.DisplayUtil;
import com.example.mycustomview.utils.LogUtil;

/**
 * 实现类似QQ侧滑滑动的效果，加入了内容scale动画，alpha动画，translationX动画
 * 参考链接：https://blog.csdn.net/lmj623565791/article/details/39257409
 */
public class SlidingMenu2 extends HorizontalScrollView {
    private int mScreenWidth;
    private int mRightPadding;
    private int mMenuWidth;
    private int mHalfMenuWidth;
    private ViewGroup menu;
    private ViewGroup content;
    private boolean isOpen;
    private boolean loadOnce;
    private boolean isSliding;
    private float xDown;
    private float xMove;
    private int xOffset;
    private int scrollX;
    private float touchSlop;
    private float progress;


    public SlidingMenu2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu2, defStyleAttr, 0);
        mRightPadding = typedArray.getDimensionPixelSize(R.styleable.SlidingMenu2_rightPadding,
                DisplayUtil.dp2px(50));
        typedArray.recycle();
        mScreenWidth=getResources().getDisplayMetrics().widthPixels;
        mMenuWidth=mScreenWidth-mRightPadding;
        mHalfMenuWidth=mMenuWidth/2;
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(!loadOnce){
            LinearLayout wrapper=(LinearLayout)getChildAt(0);
            menu=(ViewGroup)wrapper.getChildAt(0);
            content=(ViewGroup)wrapper.getChildAt(1);
            menu.getLayoutParams().width=mMenuWidth;
            content.getLayoutParams().width=mScreenWidth;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed&&!loadOnce){
            this.scrollTo(mMenuWidth,0);
            loadOnce=true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action=ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                xDown=ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                xMove=ev.getRawX();
                xOffset=(int)(xMove-xDown);
               if(Math.abs(xOffset)>touchSlop){
                   isSliding=true;
               }
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.e("ACTION_UP");
                if(isSliding){
                    scrollX=getScrollX();
                    if(scrollX>mHalfMenuWidth){
                        this.smoothScrollTo(mMenuWidth,0);
                        isOpen=false;
                    }else{
                        this.smoothScrollTo(0,0);
                        isOpen=true;
                    }
                    isSliding=false;
                }
                return true;

        }
        return super.onTouchEvent(ev);
    }

    /**
     *
     progress从1到0
     content：
        字体大小，从1到0.85
     menu:
        透明度，从0.7到1
        字体大小，从0.7到1
        translationX，根据拉出来的距离来算，distance到0
        translationX为正，是为了让menu向右偏移点。
     * @param l
     * @param t
     * @param oldl
     * @param oldt
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        LogUtil.e("translationX: "+menu.getTranslationX()+"\tl: "+l);
        progress=l*1.0f/mMenuWidth;
        float leftAlphaScale=1-0.3f*progress;
        float leftContentScale=1-0.3f*progress;
        float rightContentScale=0.85f+progress*0.15f;
        menu.animate().scaleX(leftContentScale).scaleY(leftContentScale)
              .alpha(leftAlphaScale).translationX(l*0.6f).setDuration(0);
        content.animate().scaleX(rightContentScale).scaleY(rightContentScale).setDuration(0);

        //content覆盖menu
//        menu.animate().translationX(l).setDuration(0);
    }

    public void openMenu(){
        if(!isOpen){
            smoothScrollTo(0,0);
            isOpen=true;
        }
    }
    public void closeMenu(){
        if(isOpen){
            smoothScrollTo(mMenuWidth,0);
            isOpen=false;
        }
    }
    public void toggle(){
        if(isOpen){
            closeMenu();
        }else{
            openMenu();
        }
    }
}
