package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.viewgroup.FlowLayout;

import java.util.Arrays;

public class FlowLayoutAct extends AppCompatActivity {
    private static final String TAG = "FlowLayoutAct";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.flow_layout);
        FlowLayout flowLayout=new FlowLayout(this);
        flowLayout.addData(Arrays.asList("hello","hello1","hello2",
                "hello3","hello4","hello5","hello6","hello7","hello8",
                "hello9","hello10","hello11"));

        flowLayout.setOnTagClickListener(new FlowLayout.TagClickListener() {
            @Override
            public void onClick(int i) {
                Toast.makeText(FlowLayoutAct.this,String.format(getResources().getString(R.string.tag_number),i),
                        Toast.LENGTH_SHORT).show();
            }
        });
        setContentView(flowLayout);
    }
}
