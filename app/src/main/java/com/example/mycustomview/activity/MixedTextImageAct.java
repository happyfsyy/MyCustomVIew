package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.mycustomview.R;
import com.example.mycustomview.viewgroup.MixedTextImageLayout;

public class MixedTextImageAct extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixed_text_img);
        MixedTextImageLayout mixedTextImageLayout=findViewById(R.id.mixed_text_img_layout);
        String str1="一点私密性都没有，都不能随时随时地被“壁咚”!莫奈姆瓦夏，希腊国人最眷恋的蜜月岛屿；";
        String imgUrl1="<img>https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1551760658&di=9bc8b0a1d3b2c952df58523510fce7d4&imgtype=jpg&er=1&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F150715%2F14-150G510241N23.jpg</img>";
        String str2="-奥比斯都，全世界最浪漫的结婚之城；";
        String imgUrl2="<img>https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1551165987964&di=257c01ef83d837f471c5b118bb373e15&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F160819%2F103-160Q9121559631.jpg</img>";
        String str3="-卑尔根，远离喧嚣，宁静感受一份爱";
        mixedTextImageLayout.setContent(str1+imgUrl1+str2+imgUrl2+str3);
    }
}
