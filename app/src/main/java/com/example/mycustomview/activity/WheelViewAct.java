package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.adapter.ArrayWheelAdapter;
import com.example.mycustomview.adapter.WheelAdapter;
import com.example.mycustomview.listener.OnItemSelectedListener;
import com.example.mycustomview.view.WheelView;

import java.util.ArrayList;
import java.util.List;

public class WheelViewAct extends AppCompatActivity {
    private Button button;
    private WheelView hourView;
    private WheelView minuteView;
    private ArrayWheelAdapter<String> adapter;
    private List<String> list=new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_view);
        for(int i=0;i<15;i++){
            list.add("item"+i);
        }
        adapter=new ArrayWheelAdapter<>(list);
        hourView=findViewById(R.id.hour_view);
        button=findViewById(R.id.wheel_button);
        hourView.setAdapter(adapter);
        hourView.setInitPosition(1);
//        hourView.setLabel("时");
//        hourView.isCenterLabel(true);
        hourView.setLoop(false);
        hourView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                Toast.makeText(WheelViewAct.this, list.get(index), Toast.LENGTH_SHORT).show();
            }
        });

        minuteView=findViewById(R.id.minute_view);
        minuteView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hourView.setCurrentPosition(5);
            }
        });
    }
}
