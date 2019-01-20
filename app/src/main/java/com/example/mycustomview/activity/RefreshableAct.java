package com.example.mycustomview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.viewgroup.RefreshableView;

import java.util.ArrayList;
import java.util.List;

public class RefreshableAct extends AppCompatActivity {
    private RefreshableView refreshableView;
    private ListView listView;
    private List<String> list=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refreshable_view);
        refreshableView=findViewById(R.id.refreshable_view);
        listView=findViewById(R.id.refreshable_listview);
        for(int i=0;i<10;i++){
            list.add(String.valueOf(i));
        }
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RefreshableAct.this,list.get(position),Toast.LENGTH_SHORT).show();
            }
        });
        refreshableView.setOnRefreshListener(new RefreshableView.OnRefreshListener() {
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
                                refreshableView.finishRefreshing();
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                }).start();
            }
        });


    }
}
