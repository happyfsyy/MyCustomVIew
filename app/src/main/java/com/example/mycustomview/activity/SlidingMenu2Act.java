package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.viewgroup.SlidingMenu2;

import java.util.ArrayList;
import java.util.List;

public class SlidingMenu2Act extends AppCompatActivity {
    private SlidingMenu2 slidingMenu2;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private Button toggle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_menu2);
        slidingMenu2=findViewById(R.id.sliding_menu2);
        listView=findViewById(R.id.sliding_menu2_listview);
        toggle=findViewById(R.id.sliding_menu2_toggle);
        final List<String> list=new ArrayList<>();
        for(int i=0;i<20;i++){
            list.add("我是个好人我是个好人"+i);
        }
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SlidingMenu2Act.this, list.get(position), Toast.LENGTH_SHORT).show();
            }
        });
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingMenu2.toggle();
            }
        });
    }
}
