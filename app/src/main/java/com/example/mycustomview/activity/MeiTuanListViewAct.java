package com.example.mycustomview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.example.mycustomview.R;
import com.example.mycustomview.listview.MeiTuanListView;
import com.example.mycustomview.listview.MeiTuanListView2;
import com.example.mycustomview.utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeiTuanListViewAct extends AppCompatActivity {
    private MeiTuanListView2 listView;
    private ArrayAdapter<String> adapter;
    private List<String> list=new ArrayList<>();
    private Handler mHandler=new Handler();
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
        listView.setOnMeiTuanRefreshListener(new MeiTuanListView2.OnMeiTuanRefreshListener() {
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
                                listView.setOnRefreshComplete();
                                adapter.notifyDataSetChanged();
                                LogUtil.e("adapter.notifyDataSetChanged()");
//                                listView.setSelection(0);
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
