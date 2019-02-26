package com.example.mycustomview.viewgroup;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mycustomview.utils.DisplayUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考链接：https://blog.csdn.net/jhg1204/article/details/51325845
 * 这是根据内容显示的控件。
 */
public class MixedTextImageLayout extends LinearLayout {
    private int startPos=0;
    private Context context;
    private final String imageRegex="<img>(.*?)</img>";
    public MixedTextImageLayout(Context context) {
        super(context);
        this.context=context;
        setOrientation(VERTICAL);
    }

    public MixedTextImageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setOrientation(VERTICAL);
    }

    public MixedTextImageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        setOrientation(VERTICAL);
    }

    /**
     * 设置图文混排控件要显示的内容
     * @param content
     */
    public void setContent(String content){
        String text;
        setGravity(Gravity.CENTER_HORIZONTAL);
        Pattern pattern=Pattern.compile(imageRegex);
        Matcher matcher=pattern.matcher(clearNeedlessChars(content));
        while(matcher.find()){
            text=content.substring(startPos,matcher.start());
            if(!TextUtils.isEmpty(text)){
                appendTextView(clearNewLineChar(text));
            }
            //substring inclusive exclusive
            appendImageView(content.substring(matcher.start()+5,matcher.end()-6));
            startPos=matcher.end();
        }
        text=content.substring(startPos);
        if(!TextUtils.isEmpty(text)){
            appendTextView(clearNewLineChar(text));
        }
    }
    private void appendTextView(String content){
        if(!TextUtils.isEmpty(content)){
            TextView textView=new TextView(context);
            textView.setTextIsSelectable(true);
            textView.setText(content);
            textView.setGravity(Gravity.LEFT);
            textView.getPaint().setTextSize(42);
            textView.setLineSpacing(0,1.4f);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin= DisplayUtil.dp2px(12);
            params.leftMargin=DisplayUtil.dp2px(10);
            params.rightMargin=DisplayUtil.dp2px(10);
            textView.setLayoutParams(params);
            addView(textView);
        }
    }
    private void appendImageView(String imgUrl){
        ImageView imageView=new ImageView(context);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin=DisplayUtil.dp2px(12);
        params.leftMargin=DisplayUtil.dp2px(10);
        params.rightMargin=DisplayUtil.dp2px(10);
        imageView.setLayoutParams(params);
        Glide.with(context).load(imgUrl).into(imageView);
        addView(imageView);
    }

    /**
     * 清楚多余的字符
     * @param str
     * @return
     */
    private String clearNeedlessChars(String str){
        str=str.replaceAll("&amp;","&");
        str=str.replaceAll("&quot;","\"");//"
        str=str.replaceAll("&nbsp;&nbsp;","\t");//替换跳格
        str=str.replaceAll("&nbsp;"," ");//替换空格
        str=str.replaceAll("&lt;","<");
        str=str.replaceAll("&gt;",">");
        str=str.replaceAll("\r","");
        str=str.replaceAll("\n","");
        str=str.replaceAll("\t","");
        return str;
    }

    /**
     * 清除多余的尾部换行符 注意replaceFirst不会替换字符串本身的内容
     * @param content
     * @return
     */
    private String clearNewLineChar(String content){
        int startPos=0;
        int endPos=content.length()-1;
        while(startPos<=endPos){
            if(content.charAt(startPos)=='\n'||content.charAt(startPos)=='\r'){
                startPos++;
                if(startPos>endPos){
                    content="";
                    endPos=endPos-startPos;
                    break;
                }
            }else{
                content=content.substring(startPos);
                endPos=endPos-startPos;
                break;
            }
        }
        while(endPos>0){
            if(content.charAt(endPos)=='\n'||content.charAt(endPos)=='\r'){
                endPos--;
            }else{
                content=content.substring(0,endPos+1);
                break;
            }
        }
        return content;
    }
}
