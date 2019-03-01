package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;

import com.example.mycustomview.R;
import com.example.mycustomview.adapter.WheelAdapter;
import com.example.mycustomview.interfaces.IWheelViewData;
import com.example.mycustomview.listener.LoopViewGestureListener;
import com.example.mycustomview.timer.MessageHandler;

public class WheelView extends View {
    public enum ACTION{
        CLICK,FLING,DAGGLE
    }
    public enum DividerType{
        FILL,WRAP
    }
    private Paint paintOuterText;
    private Paint paintCenterText;
    private Paint paintIndicator;

    private String label;//附加单位
    private int textSize;
    private int maxTextWidth;
    private int maxTextHeight;

    private int textColorOut;
    private int textColorCenter;
    private int dividerColor;

    private float lineSpacingMultiplier;
    private boolean isLoop;
    private float firstLineY;
    private float secondLineY;
    private float centerY;//中间label绘制的Y坐标

    private float totalScrollY;//当前滚动总高度y值
    private int initPosition;//初始化默认选择项
    private int selectedItem;//选择的item是第几个
    private int preCurrentIndex;
    private int change;//滚动偏移值，用于记录滚动了多少个item
    private int itemVisible=11;//绘制几个条目，实际上第一项和最后一项Y轴压缩成0%了，所以可见的数目实际为9
    private int measuredHeight;//控件高度
    private int measuredWidth;
    private int radius;
    private int mGravity= Gravity.RIGHT;

    private Context context;
    private Handler handler;
    private GestureDetector gestureDetector;

    private Typeface typeface=Typeface.MONOSPACE;//字体样式，默认是等宽字体

    private WheelAdapter adapter;

    private static final String[] TIME_NUM={"00","01","02","03","04","05","06","07","08","09"};

    private float itemHeight;//每行高度
    private Paint.FontMetrics fontMetrics;
    private DividerType dividerType;
    private boolean isCenterLabel=true;

    public WheelView(Context context) {
        this(context,null);
    }
    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //todo 根据屏幕密度，设置CENTER_CONTENT_OFFSET

        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.WheelView,0,0);
        mGravity=typedArray.getInt(R.styleable.WheelView_gravity,Gravity.CENTER);
        textColorOut=typedArray.getColor(R.styleable.WheelView_textColorOut,0xffa8a8a8);
        textColorCenter=typedArray.getColor(R.styleable.WheelView_textColorCenter,0xff2a2a2a);
        dividerColor=typedArray.getColor(R.styleable.WheelView_dividerColor,0xffd5d5d5);
        textSize=typedArray.getDimensionPixelSize(R.styleable.WheelView_android_textSize,
                getResources().getDimensionPixelSize(R.dimen.wheelView_textSize));
        lineSpacingMultiplier=typedArray.getFloat(R.styleable.WheelView_lineSpacingMultiplier,1.6f);
        typedArray.recycle();
        initLoopView(context);
    }
    private void initLoopView(Context context){
        this.context=context;
        handler=new MessageHandler(this);
        gestureDetector=new GestureDetector(context,new LoopViewGestureListener(this));
        gestureDetector.setIsLongpressEnabled(false);
        isLoop=true;
        totalScrollY=0;
        initPosition=-1;
        initPaints();
    }
    private void initPaints(){
        paintOuterText=new Paint();
        paintOuterText.setColor(textColorOut);
        paintOuterText.setAntiAlias(true);
        //todo typeface换成normal试试看
        paintOuterText.setTypeface(typeface);
        paintOuterText.setTextSize(textSize);

        paintCenterText=new Paint();
        paintCenterText.setColor(textColorCenter);
        paintCenterText.setAntiAlias(true);
        //todo  setTextScaleX这个有啥用，去掉试试看，变大试试看
        paintCenterText.setTextScaleX(1.1f);
        paintCenterText.setTypeface(typeface);
        paintCenterText.setTextSize(textSize);
        //todo 这里因为有textScaleX，会不会导致FontMetrics高度变大
        fontMetrics=paintCenterText.getFontMetrics();


        paintIndicator=new Paint();
        paintIndicator.setColor(dividerColor);
        paintIndicator.setAntiAlias(true);

        //todo 这一行有啥用，删掉试试看
        setLayerType(LAYER_TYPE_SOFTWARE,null);
    }
    private void reMeasure(){
        if(adapter==null){
            return;
        }
        measureTextWidthHeight();
        //todo 计算周长，控件宽度高度
        //半圆的周长=itemHeight乘以item的数目-1
        int halfCircumference=(int)(itemHeight*(itemVisible-1));
        //整个圆的周长除以PI得到直径，这个直径用作控件的总高度
        measuredHeight=(int)(2*halfCircumference/Math.PI);
        radius=(int)(halfCircumference/Math.PI);
        //计算两条横线和选中项画笔的基线Y位置
        firstLineY=(measuredHeight-itemHeight)/2;
        secondLineY=(measuredHeight+itemHeight)/2;
        //todo 这里修改为减去bottom
        centerY=secondLineY-(itemHeight-maxTextHeight)/2-fontMetrics.bottom;

        //todo 这里可以放在initPaints()中处理
        if(initPosition==-1){
            if(isLoop){
                initPosition=(adapter.getItemCount()+1)/2;
            }else{
                initPosition=0;
            }
        }
        preCurrentIndex=initPosition;
    }
    private void measureTextWidthHeight(){
        //todo 尝试我自己的测试字体宽度高度方法method2
        method1();
    }
    private void method1(){
        Rect rect=new Rect();
        for(int i=0;i<adapter.getItemCount();i++){
            String s1=getContentText(adapter.getItem(i));
            paintCenterText.getTextBounds(s1,0,s1.length(),rect);
            int textWidth=rect.width();
            if(textWidth>maxTextWidth){
                maxTextWidth=textWidth;
            }
        }
        //todo 这里把“星期”换成unicode编码试试看
        paintCenterText.getTextBounds("星期",0,2,rect);//星期的字符编码，以它为标准高度
        maxTextHeight=rect.height()+2;
        itemHeight=lineSpacingMultiplier*maxTextHeight;
    }
    private void method2(){
        for(int i=0;i<adapter.getItemCount();i++){
            String s1=getContentText(adapter.getItem(i));
            int textWidth=(int)paintCenterText.measureText(s1);
            if(textWidth>maxTextWidth){
                maxTextWidth=textWidth;
            }
        }
        float fontHeight=fontMetrics.bottom-fontMetrics.top;
        maxTextHeight=(int)fontHeight;
        itemHeight=lineSpacingMultiplier*maxTextHeight;
    }
    private String getContentText(Object item){
        if(item==null){
            return "";
        }else if(item instanceof IWheelViewData){
            return ((IWheelViewData) item).getWheelViewText();
        }else if(item instanceof Integer){
            //如果为整型，至少保留两位数
            //todo getFixNum
            return getFixNum((int)item);
        }
        return item.toString();
    }
    private String getFixNum(int timeNum){
        return timeNum>=0&&timeNum<10?TIME_NUM[timeNum]:String.valueOf(timeNum);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measuredWidth=MeasureSpec.getSize(widthMeasureSpec);
        reMeasure();
        setMeasuredDimension(measuredWidth,measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(adapter==null){
            return;
        }
        //todo 没看出这行代码的作用，建议删掉
        //initPosition越界会造成preCurrentIndex的值不正确
        initPosition=Math.min(Math.max(0,initPosition),adapter.getItemCount()-1);

        Object visibles[]=new Object[itemVisible];
        //滚动的Y值高度除去每行Item的高度，得到滚到了多少个Item，即change数
        change=(int)(totalScrollY/itemHeight);
        Log.e("change",""+change);
        try{
            preCurrentIndex=initPosition+change%adapter.getItemCount();
        }catch (ArithmeticException e){
            Log.e("WheelView","出错了！adapter.getItemCount=0,联动数据不匹配");
        }
        //todo 这里上下的代码的作用是什么
        if(!isLoop){
            if(preCurrentIndex<0) {
                preCurrentIndex = 0;
            }
            if(preCurrentIndex>adapter.getItemCount()-1){
                preCurrentIndex=adapter.getItemCount()-1;
            }
        }else{//循环
            if(preCurrentIndex<0){//如果总数是5，preCurrentIndex=-1，那么按照循环来说，preCurrentIndex其实是0的上面，也就是4的位置
                preCurrentIndex=adapter.getItemCount()+preCurrentIndex;
            }
            if(preCurrentIndex>adapter.getItemCount()-1){
                preCurrentIndex=preCurrentIndex-adapter.getItemCount();
            }
        }
        //todo 研究下这个参数有啥用，设置为0试试看
        //跟流动流畅度有关，总滑动距离与每个高度取余，即并不是一格格的滚动
        //每个item不一定滚到对应的Rect里的，这个item对应格子的偏移值
        float itemHeightOffset=totalScrollY%itemHeight;

        //设置数组中每个元素的值
        int counter=0;
        while(counter<itemVisible){
            //索引值，即当前在控件中间的item看作数据源的中间，计算出相对源数据源的index值
            int index=preCurrentIndex-(itemVisible/2-counter);
            //判断是否循环，如果是循环数据源也使用相对循环的position获取对应的item值；
            //如果不是循环则超出数据源范围使用""空白字符串填充，在界面上形成空白无数据的item项
            if(isLoop){
                index=getLoopMappingIndex(index);
                visibles[counter]=adapter.getItem(index);
            }else if(index<0){//todo 不循环的时候，index为啥会小于0
                visibles[counter]="";
            }else if(index>adapter.getItemCount()-1){
                visibles[counter]="";
            }else{
                visibles[counter]=adapter.getItem(index);
            }
            counter++;
        }

        //绘制中间两条横线
        if(dividerType==DividerType.WRAP){
            float startX,endX;
            if(TextUtils.isEmpty(label)){//隐藏label的情况
                startX=(measuredWidth-maxTextWidth)/2-12;
            }else{
                //todo 这个除以4是个什么情况
                startX=(measuredWidth-maxTextWidth)/4-12;
            }
            if(startX<=0){//如果超过了view的边缘
                startX=10;
            }
            endX=measuredWidth-startX;
            canvas.drawLine(startX,firstLineY,endX,firstLineY,paintIndicator);
            canvas.drawLine(startX,secondLineY,endX,secondLineY,paintIndicator);
        }else{
            canvas.drawLine(0,firstLineY,measuredWidth,firstLineY,paintIndicator);
            canvas.drawLine(0,secondLineY,measuredWidth,secondLineY,paintIndicator);
        }

        counter=0;
        while(counter<itemVisible){
            canvas.save();
            //求弧度a=L/r（弧长/半径) [0,π]
            double radian=(itemHeight*counter-itemHeightOffset)/radius;

            float angle=(float)(90-(radian/Math.PI)*180);
            //保证负90度和90度以外的不绘制
            if(angle>=90f||angle<=-90f){
                canvas.restore();
            }else{
                //根据当前角度计算出偏差系数，用以在绘制时控制文字的水平移动、透明度、倾斜程度
                //todo 这个系数是怎么会用这个算法的，怎么算出来的
                float offsetCoefficient=(float)Math.pow(Math.abs(angle)/90f,2.2);
                String contentText;

                //只显示选中项label文字的模式，并且label文字不为空，则进行绘制
                //我还不知道label是什么呢
                if(!TextUtils.isEmpty(label)&&isCenterLabel){

                }



            }
        }



    }
    //todo 这咋计算的？？
    //递归计算出对应的index
    private int getLoopMappingIndex(int index){
        if(index<0){
            index=index+adapter.getItemCount();
            index=getLoopMappingIndex(index);
        }else if(index>adapter.getItemCount()-1){
            index=index-adapter.getItemCount();
            index=getLoopMappingIndex(index);
        }
        return index;
    }
}
