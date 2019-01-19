package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;


public class RefreshableView extends LinearLayout implements View.OnTouchListener {
    private View headerView;
    private ImageView arrow;
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView updatedDateText;
    private ListView listView;
    private MarginLayoutParams headerParams;
    private int headerHeight;

    private boolean isAbleToPull;
    private float yDown;
    private float yMove;

    private static final int PULL_TO_REFRESH=0;
    private static final int RELEASE_TO_REFRESH=1;
    private static final int REFRESHING=2;
    private static final int REFRESH_DONE=3;
    private int mLastStatus=REFRESH_DONE;
    private int mCurStatus=mLastStatus;
    public RefreshableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        initHeaderView(context);
        listView=(ListView)getChildAt(1);
        listView.setOnTouchListener(this);
    }
    private void initHeaderView(Context context){
        headerView=LayoutInflater.from(context).inflate(R.layout.refreshable_view_header,this,false);
        arrow=headerView.findViewById(R.id.refreshable_arrow);
        progressBar=headerView.findViewById(R.id.refreshable_pb);
        statusText=headerView.findViewById(R.id.refreshable_status_text);
        updatedDateText=headerView.findViewById(R.id.refreshable_updated_text);
        addView(headerView);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //todo 设置headerView的topMargin，应该是在onMeasure()还是在onLayout()中
        headerHeight=headerView.getMeasuredHeight();
        headerParams=(MarginLayoutParams)headerView.getLayoutParams();
        headerParams.topMargin=-headerHeight;
        //todo 这里需要headerView.setLayoutParams么？
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(isAbleToPull(event)){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown=event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yMove=event.getRawY();
                    int offset=(int)(yMove-yDown);
                    if(offset<0){
                        return false;
                    }
                    //todo 这里需不需要设置offset/2
                    headerParams.topMargin=-headerHeight+offset;
                    if(headerParams.topMargin>=0){
                        mCurStatus=RELEASE_TO_REFRESH;
                    }else{
                        mCurStatus=PULL_TO_REFRESH;
                    }
                    //todo 还有什么？
                    updateHeaderViewStatus();
                    break;
                case MotionEvent.ACTION_UP:
                    //todo 根据当前的状态，判断是不是想收回headerView还是执行刷新操作
                    break;
            }
        }
        return false;
    }
    private void updateHeaderViewStatus(){
        changeArrow();
        changeStatusText();
    }

    private void changeArrow(){
        if(mLastStatus==mCurStatus){
            return;
        }
        float pivotX=arrow.getWidth()/2;
        float pivotY=arrow.getHeight()/2;
        RotateAnimation animation1=new RotateAnimation(0,180,pivotX,pivotY);
        RotateAnimation animation2=new RotateAnimation(180,360,pivotX,pivotY);
        if(mCurStatus==PULL_TO_REFRESH){
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.startAnimation(animation2);
        }else if(mCurStatus==RELEASE_TO_REFRESH){
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.setAnimation(animation1);
        }else if(mCurStatus==REFRESHING){
            arrow.clearAnimation();
            arrow.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        mLastStatus=mCurStatus;
    }
    private void changeStatusText(){
        if(mLastStatus==mCurStatus){
            return;
        }
        if (mCurStatus == PULL_TO_REFRESH) {
            statusText.setText(getResources().getString(R.string.pull_to_refresh));
        }else if(mCurStatus==RELEASE_TO_REFRESH){
            statusText.setText(getResources().getString(R.string.release_to_refresh));
        }else if(mCurStatus==REFRESHING){
            statusText.setText(getResources().getString(R.string.refreshing));
        }

    }
    private void setUpdatedDateText(){
        //todo 获取之前的日期，与现状的日期相比较，setText
    }

    /**
     * 判断当前是在滑动ListView还是在下拉header
     * @param event
     */
    private boolean isAbleToPull(MotionEvent event){
        View temp=listView.getChildAt(0);
        if(listView.getFirstVisiblePosition()==0&&temp.getTop()==0){
            if(!isAbleToPull){
                yDown=event.getRawY();
            }
            isAbleToPull=true;
        }else{
            //todo 这里为什么要header.setTopMargin=-headerHeight
            isAbleToPull=false;
        }
        return isAbleToPull;
    }
}
