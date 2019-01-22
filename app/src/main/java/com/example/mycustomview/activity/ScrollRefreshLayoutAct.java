package com.example.mycustomview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.viewgroup.ScrollRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class ScrollRefreshLayoutAct extends AppCompatActivity {
    private ScrollRefreshLayout scrollRefreshLayout;
    private List<String> data=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler mHandler=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scroll_refresh_layout);
        scrollRefreshLayout=findViewById(R.id.scroll_refresh_layout);
        for(int i=0;i<15;i++){
            data.add(String.valueOf(i));
        }
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data);
        scrollRefreshLayout.setAdapter(adapter);

        scrollRefreshLayout.setOnLoadListener(new ScrollRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int size=data.size();
                        for(int i=size;i<size+10;i++){
                            data.add(String.valueOf(i));
                        }
                        adapter.notifyDataSetChanged();
                        LogUtil.e("ScrollRefreshLayoutAct: visibleItemCount->"+scrollRefreshLayout.getVisibleItemCount());
                        scrollRefreshLayout.setSelection(size-scrollRefreshLayout.getVisibleItemCount()/2);
                        scrollRefreshLayout.finishLoading();
                    }
                },2000);
            }
        });

        scrollRefreshLayout.setOnRefreshListener(new ScrollRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollRefreshLayout.finishRefresh();
                        int size=data.size();
                        for(int i=size;i<size+10;i++){
                            data.add(String.valueOf(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                },2000);
            }
        });
        scrollRefreshLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ScrollRefreshLayoutAct.this, data.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
