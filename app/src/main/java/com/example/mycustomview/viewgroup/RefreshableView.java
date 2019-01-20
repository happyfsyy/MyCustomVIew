package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.sip.SipSession;
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
    private SharedPreferences preferences;
    private static final int ONE_MINUTE=60*1000;
    private static final int ONE_HOUR=60*ONE_MINUTE;
    private static final int ONE_DAY=24*ONE_HOUR;
    private static final int ONE_MONTH=30*ONE_DAY;
    private static final int ONE_YEAR=12*ONE_MONTH;
    private static final int SPEED=-20;
    private int touchSlop;
    private OnRefreshListener mListener;
    private boolean loadOnce=false;
    public RefreshableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        preferences=context.getSharedPreferences("updated_at",Context.MODE_PRIVATE);
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        initHeaderView(context);

    }
    private void initHeaderView(Context context){
        headerView=LayoutInflater.from(context).inflate(R.layout.refreshable_view_header,this,false);
        arrow=headerView.findViewById(R.id.refreshable_arrow);
        progressBar=headerView.findViewById(R.id.refreshable_pb);
        statusText=headerView.findViewById(R.id.refreshable_status_text);
        updatedDateText=headerView.findViewById(R.id.refreshable_updated_text);
        addView(headerView,0);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.e("onMeasure()");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //todo 为什么这里的if不能放在super.onMeasure()的前面？
        if(!loadOnce){
            headerHeight=headerView.getMeasuredHeight();
            headerParams=(MarginLayoutParams)headerView.getLayoutParams();
            headerParams.topMargin=-headerHeight;
            listView=(ListView)getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce=true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        LogUtil.e("onLayout()");

    }

    @Override
    protected void onDraw(Canvas canvas) {
        LogUtil.e("onDraw()");
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(isAbleToPull(event)){
            if(mCurStatus==REFRESHING){
                return false;
            }
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    LogUtil.e("ACTION_DOWN");
                    yDown=event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    LogUtil.e("ACTION_MOVE");
                    yMove=event.getRawY();
                    int offset=(int)(yMove-yDown);
                    //上滑不进行处理,小于touchSlop也不进行处理
                    if(offset<0){
                        return false;
                    }
                    if(offset<touchSlop){
                        return false;
                    }
                    headerParams.topMargin=-headerHeight+offset/2;
                    if(headerParams.topMargin>=0){
                        mCurStatus=RELEASE_TO_REFRESH;
                    }else{
                        mCurStatus=PULL_TO_REFRESH;
                    }
                    updateHeaderViewStatus();
                    headerView.setLayoutParams(headerParams);
                    break;
                case MotionEvent.ACTION_UP:
                    if(mCurStatus==PULL_TO_REFRESH){
                        new HideTask().execute(mCurStatus);
                    }else if(mCurStatus==RELEASE_TO_REFRESH){
                        new HideTask().execute(mCurStatus);
                    }
                    break;
                default:
            }
            //这里的return true代表后续的动作，只执行onTouch，不执行onTouchEvent。
            //也就是在下拉的过程中，不再响应listView的点击事件了。
            if(mCurStatus==PULL_TO_REFRESH||mCurStatus==RELEASE_TO_REFRESH){
                listView.setPressed(false);
                listView.setFocusable(false);
                listView.setFocusableInTouchMode(false);
                return true;
            }
        }
        return false;
    }
    public void setOnRefreshListener(OnRefreshListener listener){
        this.mListener=listener;
    }
    public interface OnRefreshListener{
        void onRefresh();
    }
    public void finishRefreshing(){
        new HideTask().execute(REFRESHING);
    }

    class HideTask extends AsyncTask<Integer,Integer,Integer>{
        @Override
        protected Integer doInBackground(Integer... status) {
            int topMargin;
            while(true){
                headerParams=(MarginLayoutParams)headerView.getLayoutParams();
                topMargin=headerParams.topMargin+SPEED;
                if(status[0]==PULL_TO_REFRESH){
                    if(topMargin<-headerHeight){
                        topMargin=-headerHeight;
                        break;
                    }
                }else if(status[0]==RELEASE_TO_REFRESH){
                    if(topMargin<0){
                        topMargin=0;
                        break;
                    }
                }else if(status[0]==REFRESHING){
                    if(topMargin<-headerHeight){
                        topMargin=-headerHeight;
                        break;
                    }
                }
                publishProgress(topMargin);
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            publishProgress(topMargin);
            return status[0];
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headerParams=(MarginLayoutParams)headerView.getLayoutParams();
            headerParams.topMargin=values[0];
            headerView.setLayoutParams(headerParams);
        }

        @Override
        protected void onPostExecute(Integer result) {
           if(result==PULL_TO_REFRESH){
               mCurStatus=REFRESH_DONE;
               mLastStatus=mCurStatus;
           }else if(result==RELEASE_TO_REFRESH){
               mCurStatus=REFRESHING;
               updateHeaderViewStatus();
               preferences.edit().putLong("updated_at",System.currentTimeMillis()).apply();
               if(mListener!=null){
                   mListener.onRefresh();
               }
           }else if(result==REFRESHING){
               mCurStatus=REFRESH_DONE;
               mLastStatus=mCurStatus;
           }
        }
    }
    private void updateHeaderViewStatus(){
        if(mLastStatus==mCurStatus){
            return;
        }
        changeArrow();
        changeStatusText();
        mLastStatus=mCurStatus;
    }
    private void changeArrow(){
        float pivotX=arrow.getWidth()/2;
        float pivotY=arrow.getHeight()/2;
        RotateAnimation animation1=new RotateAnimation(0,180,pivotX,pivotY);
        animation1.setFillAfter(true);
        animation1.setDuration(100);
        RotateAnimation animation2=new RotateAnimation(180,360,pivotX,pivotY);
        animation2.setFillAfter(true);
        animation2.setDuration(100);
        if(mCurStatus==PULL_TO_REFRESH){
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.startAnimation(animation2);
        }else if(mCurStatus==RELEASE_TO_REFRESH){
            arrow.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            arrow.startAnimation(animation1);
        }else if(mCurStatus==REFRESHING){
            arrow.clearAnimation();
            arrow.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }
    private void changeStatusText(){
        if (mCurStatus == PULL_TO_REFRESH) {
            statusText.setText(getResources().getString(R.string.pull_to_refresh));
        }else if(mCurStatus==RELEASE_TO_REFRESH){
            statusText.setText(getResources().getString(R.string.release_to_refresh));
        }else if(mCurStatus==REFRESHING){
            statusText.setText(getResources().getString(R.string.refreshing));
        }
        setUpdatedDateText();
    }
    private void setUpdatedDateText(){
        long lastTime=preferences.getLong("updated_at",-1);
        long curTime=System.currentTimeMillis();
        long offset=curTime-lastTime;
        if(lastTime==-1){
            updatedDateText.setText(getResources().getString(R.string.not_updated_yet));
        }else if(offset<0){
            updatedDateText.setText(getResources().getString(R.string.time_error));
        } else if (offset < ONE_MINUTE) {
            updatedDateText.setText(getResources().getString(R.string.updated_just_now));
        }else if(offset<ONE_HOUR){
            int num=(int)(offset/ONE_MINUTE);
            updatedDateText.setText(String.format(getResources().getString(R.string.updated_at),num+"分钟"));
        }else if(offset<ONE_DAY){
            int num=(int)(offset/ONE_HOUR);
            updatedDateText.setText(String.format(getResources().getString(R.string.updated_at),num+"小时"));
        }else if(offset<ONE_MONTH){
            int num=(int)(offset/ONE_DAY);
            updatedDateText.setText(String.format(getResources().getString(R.string.updated_at),num+"天"));
        }else if(offset<ONE_YEAR){
            int num=(int)(offset/ONE_MONTH);
            updatedDateText.setText(String.format(getResources().getString(R.string.updated_at),num+"月"));
        }else{
            int num=(int)(offset/ONE_YEAR);
            updatedDateText.setText(String.format(getResources().getString(R.string.updated_at),num+"年"));
        }
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
