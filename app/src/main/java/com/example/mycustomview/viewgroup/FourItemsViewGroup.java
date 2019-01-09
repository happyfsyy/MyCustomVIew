package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FourItemsViewGroup extends ViewGroup {
    public FourItemsViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 这里我们只需要ViewGroup能够支持margin即可，我们直接系统的MarginLayoutParams。
     * <p>重写父类的该方法，返回MarginLayoutParams的实例，这样就为我们的ViewGroup指定了其LayoutParams为MarginLayoutParams。
     *
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    /**
     * ViewGroup中并没有onMeasure()方法，这里如果执行super.onMeasure()，那么就会直接调用到View.onMeasure()方法中。
     * <p>由此可见，viewGroup的的widthMeasureSpec和heightMeasureSpec也都是由上一级父容器传递过来的，通过
     * setMeasuredDimension(getDefaultSize(suggestedMinimum,widthMeasureSpec),getDefaultSize(...))方法。
     * <p>但是getDefaultSize()有一个明显的缺陷，就是在widthMeasureSpec为wrap_content的时候，也就是(AT_MOST)的时候，
     * 直接采用父容器的尺寸，效果类似于Match_Parent。
     * <p>所以，我们在自定义ViewGroup的时候，需要重写onMeasure()方法，这个方法是为了计算viewGroup在wrap_content时候的尺寸。
     *
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);

        //获取所有子View的测量宽度和高度
        measureChildren(widthMeasureSpec,heightMeasureSpec);

        int childCount=getChildCount();

        //定义上面两个View的宽度，下面两个View的宽度，之后取大值为ViewGroup的宽度。
        int topWidth=0;
        int bottomWidth=0;
        //定义左边两个View的高度，右边两个View的高度，取大值为ViewGroup的高度。
        int leftHeight=0;
        int rightHeight=0;

        //定义viewGroup是wrap_content时候的宽和高。
        int wrapWidth;
        int wrapHeight;

        //这些都是局部变量，为了防止在for循环中被不断的定义。
        View childView;
        MarginLayoutParams childParams;
        int childWidth;
        int childHeight;

        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            childParams=(MarginLayoutParams)childView.getLayoutParams();
            //很明显，这里不能使用getWidth(),因为还未经过viewgroup的onlayout过程，getWidth=0;
            childWidth=childView.getMeasuredWidth();
            childHeight=childView.getMeasuredHeight();

            //这里可以加入padding，同时支持padding。
            //int paddingLeft=childView.getPaddingLeft();
            if(i==0||i==1){
                topWidth=topWidth+childParams.leftMargin+childParams.rightMargin+childWidth;
            }
            if(i==2||i==3){
                bottomWidth=bottomWidth+childParams.leftMargin+childParams.rightMargin+childWidth;
            }
            if(i==0||i==2){
                leftHeight=leftHeight+childParams.topMargin+childParams.bottomMargin+childHeight;
            }
            if(i==1||i==3){
                rightHeight=rightHeight+childParams.topMargin+childParams.bottomMargin+childHeight;
            }
        }
        wrapWidth=Math.max(topWidth,bottomWidth);
        wrapHeight=Math.max(leftHeight,rightHeight);

        setMeasuredDimension((widthMode==MeasureSpec.EXACTLY)?widthSize:wrapWidth,
                (heightMode==MeasureSpec.EXACTLY)?heightSize:wrapHeight);
    }

    /**
     *
     * 在这里可以直接使用getWidth()，因为这个FourItemsViewGroup的layout过程已经在它的父布局中执行完成了。
     * <p>这个方法是由父视图来确定子视图的显示位置，对所有childView进行定位，设置childView的绘制区域。
     *
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount=getChildCount();
        //定义viewGroup宽和高，这里可以使用getWidth()，虽然getWidth()是在layout()结束之后才可以获取。
        //但是那是在这个viewGroup的父布局的layout之中，这里的onLayout()方法是为了确定子视图的位置。
        int width=getWidth();
        int height=getHeight();

        //局部变量，防止重复定义
        View childView;
        MarginLayoutParams childParams;
        int childWidth;
        int childHeight;

        for(int i=0;i<childCount;i++){
            childView=getChildAt(i);
            childParams=(MarginLayoutParams)childView.getLayoutParams();
            //这里不能使用childView.getWidth()结果为0，因为layout过程还未结束，这里获得的getWidth()=0
            childWidth=childView.getMeasuredWidth();
            childHeight=childView.getMeasuredHeight();

            int left=0,top=0,right=0,bottom=0;
            //保持良好的习惯，直接使用measuredWidth，这会让子view的getWidth和getMeasuredWidth()相同
            switch (i){
                case 0:
                    left=childParams.leftMargin;
                    top=childParams.topMargin;
                    break;
                case 1:
                    left=width-childParams.rightMargin-childWidth;
                    top=childParams.topMargin;
                    break;
                case 2:
                    left=childParams.leftMargin;
                    top=height-childParams.bottomMargin-childHeight;
                    break;
                case 3:
                    left=width-childParams.rightMargin-childWidth;
                    top=height-childParams.bottomMargin-childHeight;
                    break;
            }
            right=left+childWidth;
            bottom=top+childHeight;
            //将view和它的子view就定在(l,t,r,b）这个位置，设置它的绘制区域。
            childView.layout(left,top,right,bottom);
        }
    }
}
