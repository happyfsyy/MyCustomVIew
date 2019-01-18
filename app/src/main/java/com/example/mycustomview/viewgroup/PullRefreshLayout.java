package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;

public class PullRefreshLayout extends ViewGroup {
    private View mHeaderView;
    private final int VIEW_HEIGHT = 128;//刷新状态高度
    private final int REFRESH_HEIGHT = 32; //触发松开刷新高度
    private final int STATUS_HIDE = 0;//隐藏状态
    private final int STATUS_PULL_DOWN = 1;//下拉状态
    private final int STATUS_SLOW_REFRESH = 2;//松开后刷新状态
    private final int STATUS_REFRESH = 3;//刷新状态
    private int mStatus;
    private TextView mStatusView;
    private OnPullDownRefreshListener mOnPullDownRefreshListener;

    private int mLastY;
    private int mLastX;

    public PullRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mStatus = -1;

        mHeaderView = LayoutInflater.from(context).inflate(R.layout.pullable_layout_header_view, null, false);
        LayoutParams lp = generateDefaultLayoutParams();
        lp.height = VIEW_HEIGHT;
        lp.width = LayoutParams.MATCH_PARENT;
        mHeaderView.setLayoutParams(lp);
        addView(mHeaderView, 0);
        mStatusView = (TextView) mHeaderView.findViewById(R.id.tv_status);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalLength = 0;
        int maxWidth = 0;
        //将所有子View的高度加起来
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            totalLength += childView.getMeasuredHeight();
            maxWidth = Math.max(maxWidth, childView.getMeasuredWidth());
        }
        //和最小尺寸比较，选大的那个

        //设置测量大小
        setMeasuredDimension(maxWidth,
                totalLength);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = l;
        int childTop = t;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.layout(
                    childLeft,
                    childTop,
                    childLeft + childView.getMeasuredWidth(),
                    childTop + childView.getMeasuredHeight());
            childTop += childView.getMeasuredHeight();
        }
        setStatus(STATUS_HIDE);
    }

    /**
     * 什么时候拦截事件呢？
     * <li>listView不能继续向下滑动，而手指又继续向下滑动的时候拦截事件。 </li>
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (canScrollDown()) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (ev.getX() - mLastX);
                int dy = (int) (ev.getY() - mLastY);
                //不拦截水平滑动和上拉事件
                if (Math.abs(dy) > Math.abs(dx) && dy > 0) {
                    return true;
                }
                break;
        }
        mLastY = (int) ev.getY();
        mLastX = (int) ev.getX();
        return false;
    }

    /**
     * 什么时候会执行到onTouchEvent()呢？
     * <li>事件被拦截之后</li>
     * 那么，listview当前肯定不能继续向下滑了。
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //刷新状态的时候，不必再去scroll。
        if (mStatus == STATUS_REFRESH) {
            LogUtil.e("刷新过程中，不能再动了");
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                LogUtil.e("ACTION_MOVE");
                int dx = (int) (event.getX() - mLastX);
                int dy = (int) (event.getY() - mLastY);
                //不处理水平滑动和上拉事件
                if (Math.abs(dy) <= Math.abs(dx) || dy < 0) {
                    return false;
                }
                //当HeaderView全部显示后，不允许再往下滚动
                int targetScrollY = getScrollY() - dy;
                if (targetScrollY >= 0) {
                    scrollBy(0, -dy);
                }
                if (getScrollY() < REFRESH_HEIGHT) {
                    setStatus(STATUS_SLOW_REFRESH);//HeaderView显示一定高度时进入松开刷新状态
                } else if (getScrollY() < VIEW_HEIGHT) {
                    setStatus(STATUS_PULL_DOWN);//HeaderView开始显示时，进入继续下拉状态
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.e("ACTION_UP");
                if (mStatus == STATUS_SLOW_REFRESH) {
                    setStatus(STATUS_REFRESH);//松开刷新状态下松开时，进入刷新状态
                } else {
                    setStatus(STATUS_HIDE); //否则恢复隐藏状态
                }
                break;
        }
        mLastY = (int) event.getY();
        return true;
    }

    private void setStatus(int newStatus) {
        if (mStatus != newStatus) {
            mStatus = newStatus;
            switch (mStatus) {
                case STATUS_HIDE:
                    scrollTo(0, VIEW_HEIGHT);
                    break;
                case STATUS_PULL_DOWN:
                    mStatusView.setText("继续下拉");
                    break;
                case STATUS_SLOW_REFRESH:
                    mStatusView.setText("松开刷新");
                    break;
                case STATUS_REFRESH:
                    mStatusView.setText("正在刷新...");
                    scrollTo(0, mHeaderView.getHeight() - mStatusView.getHeight());
                    if (mOnPullDownRefreshListener != null) {
                        mOnPullDownRefreshListener.onPullDownRefresh();
                    }
                    break;
            }
        }
    }

    /**
     * 是否可以继续下滑，可以就返回true，不可以就返回false
     * @return
     */
    protected boolean canScrollDown() {
        View targetView = getChildAt(1);
        if (Build.VERSION.SDK_INT < 14) {
            if (targetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) targetView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return targetView.getScrollY() > 0;
            }
        } else {
            return targetView.canScrollVertically(-1);
        }

    }


    public void setOnPullDownRefreshListener(OnPullDownRefreshListener listener) {
        mOnPullDownRefreshListener = listener;
    }

    public void setRefreshFinished() {
        setStatus(STATUS_HIDE);
    }

    public interface OnPullDownRefreshListener {
        void onPullDownRefresh();
    }
}
