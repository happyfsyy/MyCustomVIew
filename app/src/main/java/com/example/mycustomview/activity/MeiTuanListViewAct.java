package com.example.mycustomview.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.example.mycustomview.R;
import com.example.mycustomview.listview.MeiTuanListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeiTuanListViewAct extends AppCompatActivity {
    private MeiTuanListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> list=new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meituan_list_view);
        listView=findViewById(R.id.meituan_list_view);
        for(int i=0;i<20;i++){
            list.add(String.valueOf(i));
        }
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new MeiTuanListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Thread.sleep(2000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        int size=list.size();
                        for(int i=size;i<size+10;i++){
                            list.add(String.valueOf(i));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                listView.finishRefresh();
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
