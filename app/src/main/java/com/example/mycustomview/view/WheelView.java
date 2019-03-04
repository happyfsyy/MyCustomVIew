package com.example.mycustomview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.DrawableWrapper;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.example.mycustomview.R;
import com.example.mycustomview.adapter.WheelAdapter;
import com.example.mycustomview.interfaces.IWheelViewData;
import com.example.mycustomview.listener.LoopViewGestureListener;
import com.example.mycustomview.listener.OnItemSelectedListener;
import com.example.mycustomview.timer.InertiaTimerTask;
import com.example.mycustomview.timer.MessageHandler;
import com.example.mycustomview.timer.SmoothScrollTimerTask;

import java.util.EventListener;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private boolean isOptions=false;
    private int drawCenterContentStart=0;//中间选中文字开始绘制位置
    private int drawOutContentStart=0;//非中间选中文字开始绘制位置

    private static final float SCALE_CONTENT=0.8f;//非中间文字则用此控制高度，压扁形成3D错觉

    private int textXOffset;
    private long startTime=0;

    private ScheduledExecutorService mExecutor=Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;

    private float previousY=0;

    private int mOffset=0;

    private OnItemSelectedListener onItemSelectedListener;

    private static final int VELOCITY_FLING=5;

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
                startX=(measuredWidth-maxTextWidth)/2f-12;
            }else{
                //todo 这个除以4是个什么情况
                startX=(measuredWidth-maxTextWidth)/4f-12;
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

        //只显示选中项label文字的模式，并且label文字不为空，则进行绘制
        if(!TextUtils.isEmpty(label)&&isCenterLabel){
            //绘制文字，靠右并留出空隙
            int drawRightContentStart=measuredWidth-getTextWidth(paintCenterText,label);
            //todo 这里应该将label离右边距有点距离，距离多少该如何设置
            canvas.drawText(label, drawRightContentStart,centerY,paintCenterText);
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

                //如果是label每项都显示的模式，并且item内容不为空，label也不为空
                if(!isCenterLabel&&!TextUtils.isEmpty(label)&&!TextUtils.isEmpty(getContentText(visibles[counter]))){
                    contentText=getContentText(visibles[counter])+label;
                }else{
                    contentText=getContentText(visibles[counter]);
                }

                //重新计算textSize
                reMeasureTextSize(contentText);
                //计算开始绘制的位置
                measuredCenterContentStart(contentText);
                measuredOutContentStart(contentText);

                float translateY=(float)(radius-Math.sin(radian)*maxTextHeight/2-Math.cos(radian)*radius);
                canvas.translate(0,translateY);
                //canvas.scale(1.0f,(float)Math.sin(radian));
                if(translateY<=firstLineY&&maxTextHeight+translateY>=firstLineY){
                    //条目经过第一条线
                    canvas.save();
                    canvas.clipRect(0,0,measuredWidth,firstLineY-translateY);
                    //todo 这里的scaleContent啥意思？？？
                    canvas.scale(1.0f,(float)Math.sin(radian)*SCALE_CONTENT);
                    canvas.drawText(contentText,drawOutContentStart,maxTextHeight,paintOuterText);
                    canvas.restore();
                    //todo 为什么要这主要做，剪切两次，写两次text，不同的scaleY
                    //这里的10同样是Center_Conten_Offset
                    canvas.save();
                    canvas.clipRect(0,firstLineY-translateY,measuredWidth,itemHeight);
                    canvas.scale(1.0f,(float)Math.sin(radian)*1.0f);
                    canvas.drawText(contentText,drawCenterContentStart,maxTextHeight-10,paintCenterText);
                    canvas.restore();
                }else if(translateY<=secondLineY && maxTextHeight+translateY>=secondLineY){
                    //条目经过第二条线
                    canvas.save();
                    canvas.clipRect(0,0,measuredWidth,secondLineY-translateY);
                    canvas.scale(1.0f,(float)Math.sin(radian)*1.0f);
                    canvas.drawText(contentText,drawCenterContentStart,maxTextHeight-10,paintCenterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0,secondLineY-translateY,measuredWidth,itemHeight);
                    canvas.scale(1.0f,(float)Math.sin(radian)*SCALE_CONTENT);
                    canvas.drawText(contentText,drawOutContentStart,maxTextHeight,paintOuterText);
                    canvas.restore();
                }else if(translateY>=firstLineY &&maxTextHeight+translateY<=secondLineY){
                    //中间条目，让文字居中
                    float Y=maxTextHeight-10;//因为圆弧角换算的向下取值，导致角度稍微有点偏差，加上画笔的基线会偏上，因此需要偏移量修正一下
                    canvas.drawText(contentText,drawCenterContentStart,Y,paintCenterText);

                    //设置选中项
                    selectedItem=preCurrentIndex-(itemVisible/2-counter);
                }else{
                    //其他条目
                    canvas.save();
                    canvas.clipRect(0,0,measuredWidth,itemHeight);
                    canvas.scale(1.0f,(float)Math.sin(radian)*SCALE_CONTENT);
                    //控制文字倾斜角度
                    //todo setTextSkewX
                    paintOuterText.setAlpha((int)((1-offsetCoefficient)*255));
                    canvas.drawText(contentText,drawOutContentStart+textXOffset*offsetCoefficient,maxTextHeight,paintOuterText);
                    canvas.restore();
                }
                canvas.restore();
                paintCenterText.setTextSize(textSize);
            }
            counter++;
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

    public int getTextWidth(Paint paint,String str){
        int iRet=0;
        if(str!=null&&str.length()>0){
            int len=str.length();
            float[] widths=new float[len];
            //todo 这里直接getTextBounds或者measureText也行啊
            paint.getTextWidths(str,widths);
            for(int i=0;i<len;i++){
                iRet+=(int)Math.ceil(widths[i]);
            }
        }
        return iRet;
    }
    //重新设置文本的textSize好让内容能够完全展现
    private void reMeasureTextSize(String contentText){
        Rect rect=new Rect();
        //todo 这里需不需要paint.measureText
        paintCenterText.getTextBounds(contentText,0,contentText.length(),rect);
        int width=rect.width();
        int size=textSize;
        while(width>measuredWidth){
            size--;
            paintCenterText.setTextSize(textSize);
            paintCenterText.getTextBounds(contentText,0,contentText.length(),rect);
            width=rect.width();
        }
        paintOuterText.setTextSize(size);
    }
    private void measuredCenterContentStart(String content){
        Rect rect=new Rect();
        paintCenterText.getTextBounds(content,0,content.length(),rect);
        switch (mGravity){
            case Gravity.CENTER://显示内容居中
                //todo 这个isOptions有啥用
                if(isOptions||label==null||label.equals("")||!isCenterLabel){
                    drawCenterContentStart=(int)((measuredWidth-rect.width()*0.5f));
                }else{//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    //todo 这个0.25也是拍脑袋想出来的么
                    drawCenterContentStart=(int)((measuredWidth-rect.width())*0.25f);
                }
                break;
            case Gravity.LEFT:
                drawCenterContentStart=0;
                break;
            case Gravity.RIGHT://添加偏移量
                //todo 这里的10，完全是为了给右边留空，随手写的
                drawCenterContentStart=measuredWidth-rect.width()-10;
                break;
        }
    }
    //todo 这个方法和上面的方法完全一样，只是drawCenterContentStart换成了drawOutContentStart
    private void measuredOutContentStart(String content){
        Rect rect=new Rect();
        paintCenterText.getTextBounds(content,0,content.length(),rect);
        switch (mGravity){
            case Gravity.CENTER://显示内容居中
                if(isOptions||label==null||label.equals("")||!isCenterLabel){
                    drawOutContentStart=(int)((measuredWidth-rect.width()*0.5f));
                }else{//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    drawOutContentStart=(int)((measuredWidth-rect.width())*0.25f);
                }
                break;
            case Gravity.LEFT:
                drawOutContentStart=0;
                break;
            case Gravity.RIGHT://添加偏移量
                drawOutContentStart=measuredWidth-rect.width()-10;
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eventConsumed=gestureDetector.onTouchEvent(event);
        boolean isIgnore=false;//超过边界滑动时，不再绘制UI

        float top=-initPosition*itemHeight;
        float bottom=(adapter.getItemCount()-1-initPosition)*itemHeight;
        float ratio=0.25f;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startTime=System.currentTimeMillis();
                cancelFuture();
                previousY=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dy=previousY-event.getRawY();
                previousY=event.getRawY();
                totalScrollY=totalScrollY+dy;

                //normal mode
                if(!isLoop){
                    if((totalScrollY-itemHeight*ratio<top&&dy<0)||(totalScrollY+itemHeight*ratio>bottom&&dy>0)){
                        //ps这里是再滑动0.25个itemHeight就超过边界了，ratio就是这个意思
                        //快滑动到边界了，设置已滑动到边界的标志
                        //todo 这里把dy减掉什么意思？不让继续滑动了么
                        totalScrollY-=dy;
                        isIgnore=true;
                    }else{
                        isIgnore=false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                if(!eventConsumed){//未消费掉事件
                    float y=event.getY();
                    double L=Math.acos((radius-y)/radius)*radius;
                    //item0有一半在不可见区域，所以需要加上itemHeight/2
                    //todo 下面这三行什么意思，我实在没看懂
                    int circlePosition=(int)((L+itemHeight/2)/itemHeight);
                    float extraOffset=(totalScrollY%itemHeight);
                    //已经滑动的弧长值
                    mOffset=(int)((circlePosition-itemVisible/2)*itemHeight-extraOffset);
                    if((System.currentTimeMillis()-startTime)>120){
                        //处理拖拽事件
                        smoothScroll(ACTION.DAGGLE);
                    }else{
                        //处理点击事件
                        smoothScroll(ACTION.CLICK);
                    }
                }
        }
        if(!isIgnore&&event.getAction()!=MotionEvent.ACTION_DOWN){
            invalidate();
        }
        return true;
    }
    public void cancelFuture(){
        if(mFuture!=null&&!mFuture.isCancelled()){
            mFuture.cancel(true);
            mFuture=null;
        }
    }
    public void smoothScroll(ACTION action){//平滑滚动的实现
        cancelFuture();
        if(action==ACTION.FLING||action==ACTION.DAGGLE){
            mOffset=(int)(totalScrollY%itemHeight);
            if((float)mOffset>itemHeight/2.0f){//如果超过item高度的一半，则滚动到下一个item去
                mOffset=(int)(itemHeight-(float)mOffset);
            }else{
                mOffset=-mOffset;
            }
        }

        mFuture=mExecutor.scheduleWithFixedDelay(new SmoothScrollTimerTask(this,mOffset),0,10, TimeUnit.MICROSECONDS);
    }
    public void setTotalScrollY(float totalScrollY){
        this.totalScrollY=totalScrollY;
    }
    public float getTotalScrollY(){
        return totalScrollY;
    }
    public boolean isLoop(){
        return isLoop;
    }
    public float getItemHeight(){
        return itemHeight;
    }
    public int getInitPosition(){
        return initPosition;
    }
    @Override
    public Handler getHandler() {
        return handler;
    }
    public int getItemCount(){
        return adapter!=null?adapter.getItemCount():0;
    }
    public final void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener){
        this.onItemSelectedListener=onItemSelectedListener;
    }
    public final int getCurrentItem(){
        if(adapter==null) return 0;
        if(isLoop&&(selectedItem<0||selectedItem>=adapter.getItemCount())){
            return Math.max(0,Math.min(Math.abs(selectedItem-adapter.getItemCount()),adapter.getItemCount()-1));
        }
        return Math.max(0,Math.min(selectedItem,adapter.getItemCount()-1));
    }
    public final void onItemSelected(){
        if(onItemSelectedListener!=null){
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    onItemSelectedListener.onItemSelected(getCurrentItem());
                }
            },200);
        }
    }
    public final void scrollBy(float velocityY){//滚动惯性的实现
        cancelFuture();
        mFuture=mExecutor.scheduleWithFixedDelay(new InertiaTimerTask(this,velocityY),0,VELOCITY_FLING,TimeUnit.MILLISECONDS);
    }
}
