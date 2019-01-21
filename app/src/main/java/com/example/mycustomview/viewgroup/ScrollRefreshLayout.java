package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;

public class ScrollRefreshLayout extends LinearLayout {
    private View headerView;
    private ImageView arrow;
    private ProgressBar progressBar;
    private TextView statusText;
    private ListView listView;
    private boolean isLoadOnce;
    private int headerHeight;
    private float yDown;
    private float yMove;
    private int touchSlop;
    public static final int STATUS_PULL_TO_REFRESH=0;
    public static final int STATUS_RELEASE_TO_REFRESH=1;
    public static final int STATUS_REFRESHING=2;
    public static final int STATUS_IDLE=3;
    private int mCurStatus=STATUS_IDLE;
    private int mLastStatus=mCurStatus;
    private Scroller mScroller;
    private OnRefreshListener mListener;
    private View footerView;
    private ProgressBar loadingPb;
    private int heightSize;
    public ScrollRefreshLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        initParams(context);
        initHeaderView(context);
        initListView(context);
        initFooterView(context);

    }

    private void initParams(Context context){
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller=new Scroller(context);
        int heightSize=context.getResources().getDisplayMetrics().heightPixels;
        LogUtil.e("screenHeight: "+heightSize);

        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
       LogUtil.e("状态栏高度："+result);
    }

    private void initHeaderView(Context context){
        headerView=LayoutInflater.from(context).inflate(R.layout.scroll_refresh_header,this,false);
        arrow=headerView.findViewById(R.id.scroll_refresh_arrow);
        progressBar=headerView.findViewById(R.id.scroll_refresh_progressbar);
        statusText=headerView.findViewById(R.id.scroll_refresh_status_text);
        addView(headerView);
    }
    private void initListView(Context context){
        listView=new ListView(context);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(params);
        addView(listView);
    }

    private void initFooterView(Context context){
        footerView=LayoutInflater.from(context).inflate(R.layout.scroll_refresh_footer,this,false);
        loadingPb=footerView.findViewById(R.id.scroll_load_progressbar);
        addView(footerView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.e("onMeasure()");
//        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
//        LogUtil.e("headerView.Height()"+headerView.getMeasuredHeight());
//        LogUtil.e("listView.Height()"+listView.getMeasuredHeight());
//        LogUtil.e("footerView.Height()"+footerView.getMeasuredHeight());
//        LogUtil.e("measuredHeight: "+MeasureSpec.getSize(heightMeasureSpec));

        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=0;
        int childCount=getChildCount();
        View childView;
        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            measureChild(childView,widthMeasureSpec,heightMeasureSpec);
            height+=childView.getMeasuredHeight();
        }
        LogUtil.e("measuredHeight2: "+height);
        LogUtil.e("headerView.Height2: "+headerView.getMeasuredHeight());
        LogUtil.e("listView.Height2: "+listView.getMeasuredHeight());
        LogUtil.e("footerView.Height2: "+footerView.getMeasuredHeight());
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogUtil.e("onLayout()");
//        if(changed&&!isLoadOnce){
//            isLoadOnce=true;
//            listView=(ListView)getChildAt(1);
//            //todo listview.setOnScrollListener
//        }
//        headerHeight=headerView.getMeasuredHeight();
//        headerView.layout(0,-headerHeight,headerView.getMeasuredWidth(),0);
//        listView.layout(0,0,listView.getMeasuredWidth(),listView.getMeasuredHeight()+headerHeight);

//        super.onLayout(changed,l,t,r,b);
//        if(changed&&!isLoadOnce){
//            isLoadOnce=true;
//            listView=(ListView)getChildAt(1);
//            headerHeight=headerView.getMeasuredHeight();
//            scrollTo(0,headerHeight);
//        }


        int childCount=getChildCount();
        View childView;
        int top=0;
        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            childView.layout(0,top,childView.getMeasuredWidth(),top+childView.getMeasuredHeight());
            top+=childView.getMeasuredHeight();
        }
        headerHeight=headerView.getMeasuredHeight();
        scrollTo(0,headerHeight);
    }


    /**
     * 判断什么时候拦截事件。<p>
     * 只有“不能继续下拉&&依旧下拉”，证明了用户确实是想下拉“刷新”控件。
     * 这时候才会拦截listView的onTouchEvent，执行this.onTouchEvent。
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!canScrollDown()){
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown=ev.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yMove=ev.getRawY();
                    int offset=(int)(yMove-yDown);
                    if(offset>touchSlop)
                        return true;
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //这个时候其实已经可以肯定是listView.isTop了。
        switch (event.getAction()){
            //不需要action_down了，因为我已经在interceptTouchEvent记录了yDown
            case MotionEvent.ACTION_MOVE:
                yMove=event.getRawY();
                //根据下拉的距离，执行scrollBy
                int offset=(int)(yMove-yDown)/2;
                //offset>0代表向下滑动，看上面的内容，scrollBy的参数应该是负
                //当前不是刷新状态的时候，才可以下拉headerView。
                // 如果下拉的距离超过headerView的高度，就进入Release_to_refresh状态。
                if(mCurStatus!=STATUS_REFRESHING){
                    if(offset<headerHeight){
                        mCurStatus=STATUS_PULL_TO_REFRESH;
                    }else{
                        mCurStatus=STATUS_RELEASE_TO_REFRESH;
                    }
                    scrollTo(0,headerHeight-offset);
//                    scrollTo(0,-offset);
                    LogUtil.e("ScrollTo()");
                    updateHeaderView();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mCurStatus==STATUS_PULL_TO_REFRESH){
                    LogUtil.e("STATUS_PULL_TO_REFRESH");
                    LogUtil.e("ScrollY(): "+getScrollY()+" dy: "+(0-getScrollY()));
//                    mScroller.startScroll(0,getScrollY(),0,0-getScrollY());
                    mScroller.startScroll(0,getScrollY(),0,headerHeight-getScrollY());

                    LogUtil.e("StartScroll");
                    postInvalidate();
                    mCurStatus=STATUS_IDLE;
                    mLastStatus=mCurStatus;
                }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
                    LogUtil.e("STATUS_RELEASE_TO_REFRESH");
                    LogUtil.e("ScrollY(): "+getScrollY()+" dy: "+(-headerHeight-getScrollY()));
//                    mScroller.startScroll(0,getScrollY(),0,-headerHeight-getScrollY());
                    mScroller.startScroll(0,getScrollY(),0,0-getScrollY());

                    postInvalidate();
                    mCurStatus=STATUS_REFRESHING;
                    updateHeaderView();
                    mListener.onRefresh();
                }
                break;
        }
        return true;
    }

    /**
     * 更新headerView的状态，包括箭头，状态文字
     */
    private void updateHeaderView(){
        LogUtil.e("updateHeaderView()");
        if(mLastStatus==mCurStatus)
            return;
        changeArrow();
        changeStatusText();
        mLastStatus=mCurStatus;
    }
    private void changeArrow(){
        float pivotX=arrow.getWidth()/2;
        float pivotY=arrow.getHeight()/2;
        if(mCurStatus==STATUS_PULL_TO_REFRESH){
            RotateAnimation animation1=new RotateAnimation(180,360,pivotX,pivotY);
            animation1.setFillAfter(true);
            animation1.setDuration(100);
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.startAnimation(animation1);
        }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
            RotateAnimation animation1=new RotateAnimation(0,180,pivotX,pivotY);
            animation1.setFillAfter(true);
            animation1.setDuration(100);
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.startAnimation(animation1);
        }else if(mCurStatus==STATUS_REFRESHING){
            arrow.clearAnimation();
            arrow.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }//todo 这里之后可以加上Status_loading
    }
    private void changeStatusText(){
        if(mCurStatus==STATUS_PULL_TO_REFRESH){
            statusText.setText(getResources().getString(R.string.scroll_pull_to_refresh));
        }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
            statusText.setText(getResources().getString(R.string.scroll_release_to_refresh));
        }else if(mCurStatus==STATUS_REFRESHING){
            statusText.setText(getResources().getString(R.string.scroll_refreshing));
        }
    }

    /**
     * 检测是不是已经到达了listView的顶部，是否还可以继续下拉。
     * @return true代表未到达顶部，可以继续下拉。
     *          false代表已经到达顶部，不可以继续下拉
     */
    private boolean canScrollDown(){
        return listView.canScrollVertically(-1);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            LogUtil.e("CurX: "+mScroller.getCurrX()+" CurY: "+mScroller.getCurrY());
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
    public void setOnRefreshListener(OnRefreshListener listener){
        this.mListener=listener;
    }
    public void finishRefresh(){
//        mScroller.startScroll(0,getScrollY(),0,0-getScrollY());
        mScroller.startScroll(0,getScrollY(),0,headerHeight-getScrollY());
        invalidate();
        mCurStatus=STATUS_IDLE;
        mLastStatus=mCurStatus;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtil.e("onDraw()");
    }

    public void setAdapter(BaseAdapter adapter){
        listView.setAdapter(adapter);
    }
}
