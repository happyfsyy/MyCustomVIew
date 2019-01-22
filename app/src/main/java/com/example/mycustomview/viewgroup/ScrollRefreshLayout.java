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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;

public class ScrollRefreshLayout extends ViewGroup implements AbsListView.OnScrollListener {
    private View headerView;
    private ImageView arrow;
    private ProgressBar progressBar;
    private TextView statusText;
    private ListView listView;
    private int headerHeight;
    private float yDown;
    private float yMove;
    private int offset;
    private int touchSlop;
    public static final int STATUS_PULL_TO_REFRESH=0;
    public static final int STATUS_RELEASE_TO_REFRESH=1;
    public static final int STATUS_REFRESHING=2;
    public static final int STATUS_IDLE=3;
    public static final int STATUS_LOADING=4;
    private int mCurStatus=STATUS_IDLE;
    private int mLastStatus=mCurStatus;
    private Scroller mScroller;
    private OnRefreshListener mListener;
    private View footerView;
    private int footerHeight;
    private OnLoadListener mOnLoadListener;
    private int visibleItemCount;
    public ScrollRefreshLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams(context);
        initHeaderView(context);
        initListView(context);
        initFooterView(context);
    }

    private void initParams(Context context){
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller=new Scroller(context);
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
        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(params);
        listView.setOnScrollListener(this);
        addView(listView);
    }
    private void initFooterView(Context context){
        footerView=LayoutInflater.from(context).inflate(R.layout.scroll_refresh_footer,this,false);
        addView(footerView);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.e("onMeasure()");
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=0;
        int childCount=getChildCount();
        View childView;
        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            measureChild(childView,widthMeasureSpec,heightMeasureSpec);
            height+=childView.getMeasuredHeight();
        }
        setMeasuredDimension(width,height);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogUtil.e("onLayout()");
        int childCount=getChildCount();
        View childView;
        int top=0;
        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            childView.layout(0,top,childView.getMeasuredWidth(),top+childView.getMeasuredHeight());
            top+=childView.getMeasuredHeight();
        }
        headerHeight=headerView.getMeasuredHeight();
        footerHeight=footerView.getMeasuredHeight();
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
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                yDown=ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                yMove=ev.getRawY();
                offset=(int)(yMove-yDown);
                if(!canScrollDown()&&offset>touchSlop){
                    return true;
                }
                break;
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
                offset=(int)(yMove-yDown)/2;
                //offset>0代表手指向下滑动，内容向下滑动，scrollBy的参数应该是负
                //当前不是刷新状态的时候，才可以下拉headerView。
                // 如果下拉的距离超过headerView的高度，就进入Release_to_refresh状态。
                if(mCurStatus!=STATUS_REFRESHING&&mCurStatus!=STATUS_LOADING){
                    if(offset<headerHeight){
                        mCurStatus=STATUS_PULL_TO_REFRESH;
                    }else{
                        mCurStatus=STATUS_RELEASE_TO_REFRESH;
                    }
                    scrollTo(0,headerHeight-offset);
                    LogUtil.e("ScrollTo()");
                    updateHeaderView();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mCurStatus==STATUS_PULL_TO_REFRESH){
                    LogUtil.e("STATUS_PULL_TO_REFRESH");
                    mScroller.startScroll(0,getScrollY(),0,headerHeight-getScrollY());
                    LogUtil.e("StartScroll()");
                    postInvalidate();
                    mCurStatus=STATUS_IDLE;
                    mLastStatus=mCurStatus;
                }else if(mCurStatus==STATUS_RELEASE_TO_REFRESH){
                    LogUtil.e("STATUS_RELEASE_TO_REFRESH");
                    mScroller.startScroll(0,getScrollY(),0,0-getScrollY());
                    LogUtil.e("StartScroll()");
                    postInvalidate();
                    mCurStatus=STATUS_REFRESHING;
                    updateHeaderView();
                    mListener.onRefresh();
                }
                break;
        }
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState){
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                LogUtil.e("SCROLL_STATE_IDLE");
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                LogUtil.e("SCROLL_STATE_FLING");
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                LogUtil.e("SCROLL_STATE_TOUCH_SCROLL");
                LogUtil.e("mCurStatus: "+mCurStatus+" !canScrollUp: "+!canScrollUp()+" offset: "+offset);
                break;
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        LogUtil.e("onScroll()");
        LogUtil.e("mCurStatus: "+mCurStatus+" !canScrollUp: "+!canScrollUp()+" offset: "+offset);
        this.visibleItemCount=visibleItemCount;
        if(mCurStatus==STATUS_IDLE&&!canScrollUp()&&offset<0){
            mCurStatus=STATUS_LOADING;
            LogUtil.e("mCurStatus= "+mCurStatus);
            mScroller.startScroll(0,getScrollY(),0,footerHeight);
            invalidate();
            mLastStatus=mCurStatus;
            mOnLoadListener.onLoad();
        }
    }
    /**
     * 更新headerView的状态，包括箭头，状态文字
     */
    private void updateHeaderView(){
        if(mLastStatus==mCurStatus)
            return;
        changeArrow();
        changeStatusText();
        mLastStatus=mCurStatus;
    }
    private void changeArrow(){
        LogUtil.e("changeArrow()");
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
        }
    }
    private void changeStatusText(){
        LogUtil.e("changeStatusText()");
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
    private boolean canScrollUp() {
        return listView.canScrollVertically(1);
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

    public interface OnLoadListener{
        void onLoad();
    }
    public void setOnLoadListener(OnLoadListener onLoadListener){
        mOnLoadListener=onLoadListener;
    }
    public void finishLoading(){
        LogUtil.e("finishLoading()");
        mScroller.startScroll(0,getScrollY(),0,-footerHeight);
        invalidate();
        mCurStatus=STATUS_IDLE;
        mLastStatus=mCurStatus;
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        listView.setOnItemClickListener(listener);
    }
    public void setSelection(int pos){
        LogUtil.e("setSelection()");
        listView.setSelection(pos);
    }
    public int getVisibleItemCount(){
        return visibleItemCount;
    }
}
