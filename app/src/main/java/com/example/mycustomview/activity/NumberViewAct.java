package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.mycustomview.R;

public class NumberViewAct extends AppCompatActivity {
    private static final String TAG = "NumberViewAct";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_view);

        final TextView textView1=findViewById(R.id.textView1);
        textView1.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"textView1.width: "+textView1.getMeasuredWidth());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
