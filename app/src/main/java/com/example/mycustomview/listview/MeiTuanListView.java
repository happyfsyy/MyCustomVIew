package com.example.mycustomview.listview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.view.MeiTuanRefreshFirstStepView;
import com.example.mycustomview.view.MeiTuanRefreshSecondStepView;

public class MeiTuanListView extends ListView implements AbsListView.OnScrollListener {
    private LinearLayout headerView;
    private MeiTuanRefreshFirstStepView firstStepView;
    private MeiTuanRefreshSecondStepView secondStepView;
    private MeiTuanRefreshSecondStepView thirdStepView;
    private AnimationDrawable secondAnim;
    private AnimationDrawable thirdAnim;
    private int headerViewHeight;
    private int screenHeight;
    private int statusHeight;
    private static final int STATUS_IDLE=0;
    private static final int STATUS_PULL_TO_REFRESH=1;
    private static final int STATUS_RELEASE_TO_REFRESH=2;
    private static final int STATUS_REFRESHING=3;
    private float yDown;
    private float yMove;
    private float yOffset;
    private int yScrollHeight;
    private int mCurStatus;
    private OnRefreshListener mListener;


    public MeiTuanListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenHeight=context.getResources().getDisplayMetrics().heightPixels;

        int resourceId=context.getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0) {
            statusHeight=context.getResources().getDimensionPixelSize(resourceId);
        }
        LogUtil.e("屏幕高度："+screenHeight+"\t状态栏高度："+statusHeight);
        init(context);
    }

    private void init(Context context){
        setOnScrollListener(this);
        headerView=(LinearLayout)LayoutInflater.from(context).inflate(R.layout.meituan_header,this,false);
        firstStepView=headerView.findViewById(R.id.meituan_first_view);
        secondStepView=headerView.findViewById(R.id.meituan_second_view);
        thirdStepView=headerView.findViewById(R.id.meituan_third_view);
        secondStepView.setBackgroundResource(R.drawable.meituan_second_step_anim);
        thirdStepView.setBackgroundResource(R.drawable.meituan_third_step_anim);
        secondAnim=(AnimationDrawable)secondStepView.getBackground();
        thirdAnim=(AnimationDrawable)thirdStepView.getBackground();
        addHeaderView(headerView);

        mCurStatus=STATUS_IDLE;
    }

    private void measureView(View child){
        ViewGroup.LayoutParams params=child.getLayoutParams();
        if(params==null){
            params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec=ViewGroup.getChildMeasureSpec(0,0,params.width);
        int height=params.height;
        int childHeightSpec;
        if(height>0){
            childHeightSpec=MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);
        }else{
            childHeightSpec=MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec,childHeightSpec);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogUtil.e("onLayout()");
        super.onLayout(changed, l, t, r, b);
        headerViewHeight=headerView.getMeasuredHeight();
        scrollTo(0,headerViewHeight);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        LogUtil.e("firstVisibleItem: "+firstVisibleItem);
    }

    private boolean isTop(){
        return getFirstVisiblePosition()==0&&getScrollY()<=headerViewHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isTop()){
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    LogUtil.e("ACTION_DOWN");
                    yDown=ev.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    LogUtil.e("ACTION_MOVE");
                    yMove=ev.getRawY();
                    yOffset=yMove-yDown;
                    //yScrollHeight代表headerView移动的距离
                    yScrollHeight=(int)yOffset/2;
                    //小于0就是上滑，不进行任何操作
                    if(mCurStatus!=STATUS_REFRESHING&&yMove>0){
                        if(yScrollHeight<headerViewHeight){
                            mCurStatus=STATUS_PULL_TO_REFRESH;
                        }else{
                            mCurStatus=STATUS_RELEASE_TO_REFRESH;
                        }
                        scrollTo(0,headerViewHeight-yScrollHeight);
                        updateHeaderView();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(mCurStatus==STATUS_PULL_TO_REFRESH){
                        smoothScrollBy(headerViewHeight-getScrollY(),250);
                        mCurStatus=STATUS_IDLE;
                    }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
                        smoothScrollBy(0-getScrollY(),250);
                        setSelection(0);
                        mCurStatus=STATUS_REFRESHING;
                        updateHeaderView();
                        mListener.onRefresh();
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }
    private void updateHeaderView(){
        if(mCurStatus==STATUS_PULL_TO_REFRESH){
            float progress=(float)yScrollHeight/headerViewHeight;
            firstStepView.setVisibility(View.VISIBLE);
            firstStepView.setProgress(progress);
            firstStepView.postInvalidate();

            secondAnim.stop();
            secondStepView.setVisibility(View.GONE);
            thirdAnim.stop();
            thirdStepView.setVisibility(View.GONE);
        }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
            firstStepView.setVisibility(View.GONE);
            secondStepView.setVisibility(View.VISIBLE);
            secondAnim.start();
            thirdAnim.stop();
            thirdStepView.setVisibility(View.GONE);
        }else if(mCurStatus==STATUS_REFRESHING){
            firstStepView.setVisibility(View.GONE);
            secondAnim.stop();
            secondStepView.setVisibility(View.GONE);
            thirdStepView.setVisibility(View.VISIBLE);
            thirdAnim.start();
        }
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
    public void setOnRefreshListener(OnRefreshListener listener){
        mListener=listener;
    }
    public void finishRefresh(){
        smoothScrollBy(headerViewHeight,250);
        mCurStatus=STATUS_IDLE;
    }
}
