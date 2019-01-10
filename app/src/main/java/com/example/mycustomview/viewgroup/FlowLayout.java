package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mycustomview.R;
import com.example.mycustomview.bean.ViewCoordinate;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private TagClickListener mListener;
    private int childCount;

    private List<ViewCoordinate> coordinateList=new ArrayList<>();

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

        //定义FlowLayout的布局为wrap_content的时候的自适应宽度和高度。
        //这里的width代表去掉最后一行之外，其他所有行中最大的宽度。
        //这里的height是去掉最下面一行，累加所得的高度。
        int width=0,height=0;

        //定义每一行的宽度，width就是其中的最大值。具体的办法可以每一行都与width作比较。
        //这里的lineWidth和lineHeight都代表最下面那一行.
        int lineWidth=0;
        //定义每一行的高度，就是这一行所有的子view的高度最大值，具体的办法就是这一行中的每个子view都与它进行比较
        int lineHeight=0;

        //测量所有的子view，获得所有子View的measuredWidth。
        measureChildren(widthMeasureSpec,heightMeasureSpec);

        //定义所有子view的数量
        childCount=getChildCount();

        //定义需要在for循环中重复定义的变量。
        View childView;
        MarginLayoutParams childParams;
        int childWidth;
        int childHeight;

        for(int i=0;i<childCount;i++){
            //获取每个子view
            childView=getChildAt(i);
            //对于每个子view都需要margin，不过我们通常都是设置成相同的。
            childParams=(MarginLayoutParams)childView.getLayoutParams();
            //这里的子View的measuredWidth已经加入了padding，但是FlowLayout还没有加入padding，可以支持padding
            childWidth=childView.getMeasuredWidth()+childParams.leftMargin+childParams.rightMargin;
            childHeight=childView.getMeasuredHeight()+childParams.topMargin+childParams.bottomMargin;

            //对于每一个子View都定义一个坐标实体
            ViewCoordinate coordinate=new ViewCoordinate();

            //考虑问题要全面，i=0到i=childCount-2都适用这个条件
            if(lineWidth+childWidth>widthSize){
                //宽度超过最大宽度，那么当前的childView就放在下一行中。
                //无论是计算width或者height，都是等待这一行结束之后，也就是不能再容纳下一个childView之后。
                //宽度就拿当前行的宽度与width进行比较，高度直接加上当前行的高度。
                width=Math.max(lineWidth,width);
                height+=lineHeight;
                //另起一行之后，重新设置新一行的宽度和高度。
                //这里有个问题，如果这个childView的宽度直接超过了最大宽度怎么办？？？？？
                //这里先不做考虑，我们只是测试，不会让textView超过group的宽度的.
                lineWidth=childWidth;
                lineHeight=childHeight;
            }else{
                //一般情况下，当前的childView加入当前行，那么宽度就叠加，高度就比较。
                lineWidth+=childWidth;
                lineHeight=Math.max(lineHeight,childHeight);
            }
            //首先当前的childView作为最“新”的view，肯定是在最下面一行；
            // left就是最新一行的宽度（lineWidth)减去childWidth，再加上leftMargin
            coordinate.setLeft(lineWidth-childWidth+childParams.leftMargin);
            //如果这一行有其他view的高度特别高，我们并不让这个view居中，而是直接采用topMargin。
            //这样导致的结果，就是会很不好看,这一行如果所有的view的topMargin都不同，所有的view都会起起伏伏，不在同一个水平线上。
            //正常来说，应该是确定好布局之后，知道某一行的高度之后，根据当前这个childView的高度，设置让它居中。
            // lineHeight/2-childHeight/2，再加上前面所有行的高度.
            //这里我们为了简单用途，之后layout文件中，所有的textView都是同样的高度。
            //因为这里的lineHeight不是这一行的最终的lineHeight。
            coordinate.setTop(height+childParams.topMargin);
            coordinate.setRight(coordinate.getLeft()+childView.getMeasuredWidth());
            coordinate.setBottom(coordinate.getTop()+childView.getMeasuredHeight());
            //这里并不需要对childCount-1进行特殊处理
            coordinateList.add(coordinate);


            //但是，到了i=childCount-1的时候，同样是分两种情况，但是多了一重考虑。
            //另起一行之后，我要重新比较width和最后那个childView的宽度childWidth，高度要加上最后一行的childHeight；
            //不另起一行的话，width同样要和最后一行的宽度lineWidth进行比较，高度要加上这一行的lineHeight。
            if(i==childCount-1){
                //因为前面在if的大括号内，我们将lineWidth=childWidth,lineHeight=childHeight了。
                width=Math.max(lineWidth,width);
                height+=lineHeight;
                //处理之后：
                //这里的width已经是最终的width，代表FlowLayout的宽度
                //这里的height也是最终的height，代表FlowLayout的高度
            }
        }

        setMeasuredDimension(widthMode==MeasureSpec.EXACTLY?widthSize:width,
                heightMode==MeasureSpec.EXACTLY?heightSize:height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(int i=0;i<childCount;i++){
            View childView=getChildAt(i);
            //这里，我需要每个view的l,t,r,b，其实，只要l，t就够了。
            //保持getWidth和getMeasuredWidth()的一致性，我们只需要在onMeasure()过程中记录，然后得到就可以了。
            childView.layout(coordinateList.get(i).getLeft(),coordinateList.get(i).getTop(),
                    coordinateList.get(i).getRight(),coordinateList.get(i).getBottom());
        }
    }

    public void addData(List<String> labels){
        for(int i=0;i<labels.size();i++){
            TextView textView=new TextView(this.getContext());
            MarginLayoutParams params=new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT,
                    MarginLayoutParams.WRAP_CONTENT);
            params.setMargins(DisplayUtil.dp2px(5),DisplayUtil.dp2px(5),
                    DisplayUtil.dp2px(5),DisplayUtil.dp2px(5));
            textView.setLayoutParams(params);
            textView.setTextSize(16);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.flow_item_bg);
            textView.setText(labels.get(i));
            this.addView(textView);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                for(int i=0;i<childCount;i++){
                    if(isContain(event,coordinateList.get(i))){
                        mListener.onClick(i);
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    private boolean isContain(MotionEvent event,ViewCoordinate coordinate){
        float x=event.getX();
        float y=event.getY();
        int left=coordinate.getLeft();
        int right=coordinate.getRight();
        int top=coordinate.getTop();
        int bottom=coordinate.getBottom();
        boolean condition1=x>=left&&x<=right;
        boolean condition2=y>=top&&y<=bottom;
        if(condition1&&condition2){
            return true;
        }else{
            return false;
        }
    }

    public void setOnTagClickListener(TagClickListener listener){
        this.mListener=listener;
    }
    public interface TagClickListener{
        void onClick(int i);
    }
}
