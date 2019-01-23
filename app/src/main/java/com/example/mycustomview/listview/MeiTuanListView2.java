package com.example.mycustomview.listview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.view.MeiTuanRefreshFirstStepView;
import com.example.mycustomview.view.MeiTuanRefreshSecondStepView;

/**
 * getScrollY()一直是0，根本不能用smoothScrollY来做，只能用AsyncTask异步任务来更改padding。
 */
public class MeiTuanListView2 extends ListView implements AbsListView.OnScrollListener {
    private static final int DONE = 0;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;
    private static final int RATIO = 2;
    private LinearLayout headerView;
    private int headerViewHeight;
    private float startY;
    private float moveY;
    private float offsetY;
    private float actualHeight;
    private TextView tv_pull_to_refresh;
    private OnMeiTuanRefreshListener mOnRefreshListener;
    private int state;
    private int mFirstVisibleItem;
    private MeiTuanRefreshFirstStepView mFirstView;
    private MeiTuanRefreshSecondStepView mSecondView;
    private AnimationDrawable secondAnim;
    private MeiTuanRefreshSecondStepView mThirdView;
    private AnimationDrawable thirdAnim;
    private int lastState;
    private int touchSlop;
    private Scroller mScroller;
    private int paddingTop;

    public MeiTuanListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public interface OnMeiTuanRefreshListener{
        void onRefresh();
    }

    public void setOnMeiTuanRefreshListener(OnMeiTuanRefreshListener onRefreshListener){
        mOnRefreshListener = onRefreshListener;
    }


    public void setOnRefreshComplete(){
        LogUtil.e("setOnRefreshComplete()");

//        mScroller.startScroll(0,(int)actualHeight-headerViewHeight,0,headerViewHeight);
//        invalidate();
        state = DONE;
//        lastState=DONE;
        changeHeaderByState(state);
    }

    private void init(Context context) {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setOnScrollListener(this);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller=new Scroller(context);

        headerView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.meituan_header,null);
        mFirstView = headerView.findViewById(R.id.meituan_first_view);
        tv_pull_to_refresh = headerView.findViewById(R.id.meituan_status_text);
        mSecondView = headerView.findViewById(R.id.meituan_second_view);
        mSecondView.setBackgroundResource(R.drawable.meituan_second_step_anim);
        secondAnim = (AnimationDrawable) mSecondView.getBackground();
        mThirdView =  headerView.findViewById(R.id.meituan_third_view);
        mThirdView.setBackgroundResource(R.drawable.meituan_third_step_anim);
        thirdAnim = (AnimationDrawable) mThirdView.getBackground();

        addHeaderView(headerView);

        state = DONE;
        lastState=DONE;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed&&headerViewHeight<=0){
            headerViewHeight=headerView.getMeasuredHeight();
            LogUtil.e("headerViewHeight: "+headerViewHeight);
            headerView.setPadding(0, -headerViewHeight, 0, 0);
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }
    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
//        LogUtil.e("getScrollY(): "+getScrollY());
    }

    /**
     * 只需要检测headerView是否有重用现象就行了，就是在Adapter的getView中是否调用了convertView。
     * 如果使用了重用机制，那么在headerView不可见的时候，高度就是其他item的高度，而不是headerViewHeight
     */
    private void  isTop(){
        View child=getChildAt(0);
        int child0Top=child.getTop();
        int height0=child.getHeight();
        View child1=getChildAt(1);
        int child1Top=child1.getTop();
        int height1=child1.getHeight();
        LogUtil.e("child0Top: "+child0Top+"\tchild1Top: "+child1Top+
                "\theight0: "+height0+"\theight1: "+height1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mFirstVisibleItem==0){
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    startY=ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    LogUtil.e("ACTION_MOVE");
                    moveY=ev.getY();
                    offsetY=moveY-startY;
                    actualHeight=offsetY/RATIO;
                    if(offsetY>touchSlop&&state!=REFRESHING){
                        paddingTop=(int)(-headerViewHeight+actualHeight);
                        float progress=actualHeight/headerViewHeight;
                        if(progress>1) progress = 1;
                        if(paddingTop>=0){
                            LogUtil.e("RELEASE_TO_REFRESH");
                            state=RELEASE_TO_REFRESH;
                        }else{
                            LogUtil.e("PULL_TO_REFRESH");
                            state=PULL_TO_REFRESH;
                        }
                        changeHeaderByState(state);
                        mFirstView.setProgress(progress);
                        mFirstView.postInvalidate();
                        headerView.setPadding(0,paddingTop,0,0);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    LogUtil.e("ACTION_UP");
                    LogUtil.e("getScrollY()的值是："+getScrollY());
                    if(state==PULL_TO_REFRESH){
                        LogUtil.e("PULL_TO_REFRESH");
                        smoothScrollBy((int)(actualHeight),250);
//                        smoothScrollToPosition(1);
//                        scrollListBy((int)actualHeight);
//                        mScroller.startScroll(0,headerViewHeight-(int)actualHeight,0,(int)actualHeight);
//                        mScroller.startScroll(0,0,0,(int)actualHeight);
                        invalidate();
                        LogUtil.e("ScrollBy的终点应该是："+headerViewHeight);
                        LogUtil.e("ScrollBy的终点实际是："+(getScrollY()+actualHeight));
                        state=DONE;
                        changeHeaderByState(state);
//                        lastState=DONE;
                    }else if(state==RELEASE_TO_REFRESH){
                        LogUtil.e("RELEASE_TO_REFRESH");
                        LogUtil.e("ScrollBy的终点应该是：0");
                        LogUtil.e("ScrollBy的终点实际是："+(getScrollY()+actualHeight-headerViewHeight));
//                        mScroller.startScroll(0,0,0,(int)(actualHeight-headerViewHeight));
//                        invalidate();
                        smoothScrollBy((int)(actualHeight-headerViewHeight),250);
//                        smoothScrollToPosition(0,0);
//                        scrollListBy((int)(actualHeight-headerViewHeight));
                        mOnRefreshListener.onRefresh();
                        state=REFRESHING;
                        changeHeaderByState(state);
                    }else if(state==REFRESHING){
                        LogUtil.e("REFRESHING");
                    }else if(state==DONE){
                        LogUtil.e("DONE");
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void changeHeaderByState(int state){
        if(lastState==state) return;
        LogUtil.e("changeHeaderByState()");
        switch (state) {
            case DONE:
                LogUtil.e("state= Done");
                headerView.setPadding(0, -headerViewHeight, 0, 0);
                mFirstView.setVisibility(View.VISIBLE);
                mSecondView.setVisibility(View.GONE);
                secondAnim.stop();
                mThirdView.setVisibility(View.GONE);
                thirdAnim.stop();
                break;
            case RELEASE_TO_REFRESH:
                tv_pull_to_refresh.setText("放开刷新");
                mFirstView.setVisibility(View.GONE);
                mSecondView.setVisibility(View.VISIBLE);
                secondAnim.start();
                mThirdView.setVisibility(View.GONE);
                thirdAnim.stop();
                break;
            case PULL_TO_REFRESH:
                tv_pull_to_refresh.setText("下拉刷新");
                mFirstView.setVisibility(View.VISIBLE);
                mSecondView.setVisibility(View.GONE);
                secondAnim.stop();
                mThirdView.setVisibility(View.GONE);
                thirdAnim.stop();
                break;
            case REFRESHING:
                tv_pull_to_refresh.setText("正在刷新");
                mFirstView.setVisibility(View.GONE);
                mThirdView.setVisibility(View.VISIBLE);
                mSecondView.setVisibility(View.GONE);
                secondAnim.stop();
                thirdAnim.start();
                break;
            default:
                break;
        }
        lastState=state;
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            LogUtil.e("CurX: "+mScroller.getCurrX()+" CurY: "+mScroller.getCurrY());
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }
}
