package com.example.mycustomview.viewgroup;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.mycustomview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class RichTextEditor extends ScrollView {
    private static final int EDIT_PADDING=10;//edittext常规padding是10dp
    private static final int EDIT_FIRST_PADDING_TOP=10;//第一个EditText的paddingTop值

    private int viewTagIndex=1;//新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout;//这个是所有子view的容器，scrollview内部唯一一个ViewGroup
    private LayoutInflater inflater;
    private OnKeyListener keyListener;//所有EditText的软键盘监听器
    private OnClickListener btnListener;//图片右上角红叉按钮监听器
    private OnFocusChangeListener focusListener;//所有EditText的焦点监听listener
    private EditText lastFocusEdit;//最近被聚焦的EditText
    private LayoutTransition mTransitioner;//只在图片view添加或者remove的时候，触发transition动画
    private int editNormalPadding=0;
    private int disappearingImageIndex=0;
    public RichTextEditor(Context context) {
        this(context,null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater=LayoutInflater.from(context);
        //1、初始化allLayout
        allLayout=new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        allLayout.setBackgroundColor(Color.WHITE);
        setupLayoutTransitions();
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(allLayout,layoutParams);

        //2、初始化键盘退格监听
        //主要用来处理点击回删按钮时，view的一些列合并操作
        keyListener=new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()==KeyEvent.ACTION_DOWN
                        &&event.getKeyCode()==KeyEvent.KEYCODE_DEL){
                    EditText editText=(EditText)v;
                    onBackspacePress(editText);
                }
                return false;
            }
        };

        //3、图片叉掉处理
        btnListener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout parentView=(RelativeLayout)v.getParent();
                onImageCloseClick(parentView);
            }
        };

        focusListener=new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    lastFocusEdit=(EditText)v;
                }
            }
        };
        //todo 这里的高度是match_parent，改为wrap_content
        LinearLayout.LayoutParams firstEditParams=new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editNormalPadding=dip2px(EDIT_PADDING);
        EditText firstEdit=createEditText("input here",dip2px(EDIT_FIRST_PADDING_TOP));
        allLayout.addView(firstEdit,firstEditParams);
        lastFocusEdit=firstEdit;

    }
    private void setupLayoutTransitions(){
        mTransitioner=new LayoutTransition();
        allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {

            }
            @Override
            public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                //todo transition动画结束，合并EditText
            }
        });
        mTransitioner.setDuration(300);
    }

    /**
     * 处理软键盘backspace回退事件
     * @param editText  光标所在的文本输入框
     */
    private void onBackspacePress(EditText editText){
        int startSelection=editText.getSelectionStart();
        //只有光标已经到文本输入框的最前方，再判定是否删除之前的图片，或两个view合并
        if(startSelection==0){
            int editIndex=allLayout.indexOfChild(editText);
            View preView=allLayout.getChildAt(editIndex-1);
            if(null!=preView){
                if(preView instanceof RelativeLayout){
                    //光标EditText的上一个View对应的是图片，删除上一张图片
                    onImageCloseClick(preView);
                }else if(preView instanceof EditText){
                    //光标EditText的上一个view对应的还是文本框EditText
                    EditText preEditText=(EditText)preView;
                    String str1=preEditText.getText().toString();
                    String str2=editText.getText().toString();

                    //合并文本view时，不需要transition动画
                    allLayout.setLayoutTransition(null);
                    allLayout.removeView(editText);
                    allLayout.setLayoutTransition(mTransitioner);//恢复transition动画

                    //文本合并
                    preEditText.setText(str1+str2);
                    preEditText.requestFocus();
                    preEditText.setSelection(str1.length());
                    lastFocusEdit=preEditText;
                }
            }
        }
    }

    /**
     * 处理图片叉掉的点击事件
     * @param view  整个image对应的relativeLayout view
     * @type 删除类型 0代表backspace删除，1代表按红叉按钮删除
     */
    private void onImageCloseClick(View view){
        if(!mTransitioner.isRunning()){
            disappearingImageIndex=allLayout.indexOfChild(view);
            allLayout.removeView(view);
        }
    }

    private int dip2px(float dpValue){
        float density=getContext().getResources().getDisplayMetrics().density;
        return (int)(dpValue*density+0.5f);
    }

    /**
     * 生成文本输入框
     */
    private EditText createEditText(String hint,int paddingTop){
        EditText editText=(EditText)inflater.inflate(R.layout.edit_item1,null);
        editText.setOnKeyListener(keyListener);
        editText.setTag(viewTagIndex++);
        editText.setPadding(editNormalPadding,paddingTop,editNormalPadding,0);
        editText.setOnFocusChangeListener(focusListener);
        return editText;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout(){
        RelativeLayout layout=(RelativeLayout)inflater.inflate(R.layout.edit_imageview,null);
        layout.setTag(viewTagIndex++);
        View closeView=layout.findViewById(R.id.image_close);
        closeView.setTag(layout.getTag());
        closeView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 对外开放的接口，根据绝对路径添加view
     */
    public void insertImage(String imagePath){
        Bitmap bitmap=getScaledBitmap(imagePath,getWidth());
        insertImage(bitmap,imagePath);
    }

    /**
     * 插入一张图片
     */
    private void insertImage(Bitmap bitmap,String imagePath){
        String lastEditStr=lastFocusEdit.getText().toString();
        int cursorIndex=lastFocusEdit.getSelectionStart();
        String editStr1=lastEditStr.substring(0,cursorIndex).trim();
        int lastEditIndex=allLayout.indexOfChild(lastFocusEdit);
        if(lastEditStr.length()==0||editStr1.length()==0){
            //如果EditText为空或者光标已经在EditText的最前面，则直接插入图片，并且EditText下移即可
            addImageViewAtIndex(lastEditIndex,bitmap,imagePath);
        }else{
            //如果EditText非空且光标不在最顶端，则需要添加新的ImageView和EditText
            lastFocusEdit.setText(editStr1);
            String editStr2=lastEditStr.substring(cursorIndex).trim();
            if(allLayout.getChildCount()-1==lastEditIndex||editStr2.length()>0){
                addEditTextAtIndex(lastEditIndex+1,editStr2);
            }
            addImageViewAtIndex(lastEditIndex+1,bitmap,imagePath);
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr1.length(),editStr1.length());
            hideKeyBoard();
        }
    }
    public void hideKeyBoard(){
        InputMethodManager imm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(),0);
    }

    /**
     * 在特定位置插入EditText
     * @param index 位置
     * @param editStr   EditText显示的文字
     */
    private void addEditTextAtIndex(int index,String editStr){
        EditText editText=createEditText("",
                getResources().getDimensionPixelSize(R.dimen.edit_padding_top));
        editText.setText(editStr);
        //EditText添加、或删除不触动Transition动画
        allLayout.setLayoutTransition(null);
        allLayout.addView(editText,index);
        allLayout.setLayoutTransition(mTransitioner);
    }
    private void addImageViewAtIndex(final int index, Bitmap bitmap, String imagePath){
        final RelativeLayout imageLayout=createImageLayout();
        DataImageView imageView=imageLayout.findViewById(R.id.edit_imageView);
        imageView.setImageBitmap(bitmap);
        imageView.setBitmap(bitmap);
        imageView.setAbsolutePath(imagePath);

        //调整ImageView的高度
        int imgHeight=getWidth()*bitmap.getHeight()/bitmap.getWidth();
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,imgHeight);
        imageView.setLayoutParams(lp);

        //onActivityResult无法触发动画，此处post处理
        allLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                allLayout.addView(imageLayout,index);
            }
        },200);
    }


    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     * @param width view的宽度
     */
    private Bitmap getScaledBitmap(String filePath,int width){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(filePath,options);
        int inSampleSize=options.outWidth>width?options.outWidth/width+1:1;
        options.inJustDecodeBounds=false;
        options.inSampleSize=inSampleSize;
        return BitmapFactory.decodeFile(filePath,options);
    }

    /**
     * 图片删除的时候，如果上下方都是EditText，则合并处理
     */
    private void mergeEditText(){
        View preView=allLayout.getChildAt(disappearingImageIndex-1);
        View nextView=allLayout.getChildAt(disappearingImageIndex);
        if(preView!=null&&preView instanceof EditText&&null!=nextView
                &&nextView instanceof EditText) {
            EditText preEdit = (EditText) preView;
            EditText nextEdit = (EditText)nextView;
            String str1=preEdit.getText().toString();
            String str2=nextEdit.getText().toString();
            String mergeText="";
            if(str2.length()>0){
                mergeText=str1+"\n"+str2;
            }else{
                mergeText=str1;
            }
            allLayout.setLayoutTransition(null);
            allLayout.removeView(nextEdit);
            preEdit.setText(mergeText);
            preEdit.requestFocus();
            preEdit.setSelection(str1.length(),str1.length());
            allLayout.setLayoutTransition(mTransitioner);
        }
    }
    public List<EditData> buildEditData(){
        List<EditData> dataList=new ArrayList<>();
        int num=allLayout.getChildCount();
        for(int index=0;index<num;index++){
            View itemView=allLayout.getChildAt(index);
            EditData itemData=new EditData();
            if(itemView instanceof EditText){
                EditText item=(EditText)itemView;
                itemData.inputStr=item.getText().toString();
            }else if(itemView instanceof RelativeLayout){
                DataImageView item=itemView.findViewById(R.id.edit_imageView);
                itemData.imagePath=item.getAbsolutePath();
                itemData.bitmap=item.getBitmap();
            }
            dataList.add(itemData);
        }
        return dataList;
    }

    class EditData{
        String inputStr;
        String imagePath;
        Bitmap bitmap;
    }
}
