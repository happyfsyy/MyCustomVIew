package com.example.mycustomview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mycustomview.R;
import com.example.mycustomview.viewgroup.PullRefreshLayout;

public class PullRefreshLayoutAct extends AppCompatActivity implements AbsListView.OnScrollListener {
    private Handler mHandler=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_refresh_layout);
        final PullRefreshLayout pullRefreshLayout = findViewById(R.id.pull_refresh_layout);
        pullRefreshLayout.setOnPullDownRefreshListener(new PullRefreshLayout.OnPullDownRefreshListener() {
            @Override
            public void onPullDownRefresh() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefreshLayout.setRefreshFinished();
                    }
                }, 3000);
            }
        });

        ListView listView =  findViewById(R.id.list_view);
        String data[] = new String[50];
        for (int i = 0; i < 50; i++) {
            data[i] = "item " + i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PullRefreshLayoutAct.this, "click " + position, Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
