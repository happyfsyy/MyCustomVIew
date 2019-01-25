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
import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.viewgroup.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class SlidingMenuAct extends AppCompatActivity {
    private ListView listView;
    private List<String> data=new ArrayList<>();
    private Button toggle;
    private SlidingMenu slidingMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_menu);
        //这些是为了测试屏幕宽度，测试horizontalScrollView的功能的
        float density=getResources().getDisplayMetrics().density;
        int widthpx=getResources().getDisplayMetrics().widthPixels;
        float widthDp=widthpx/density;
        LogUtil.e("density: "+density+"\twidthpx: "+widthpx+
                "\twidthDp: "+widthDp);

        listView=findViewById(R.id.sliding_menu_listview);
        toggle=findViewById(R.id.sliding_menu_toggle);
        slidingMenu=findViewById(R.id.sliding_menu);
        for(int i=0;i<20;i++){
            data.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SlidingMenuAct.this, data.get(position), Toast.LENGTH_SHORT).show();
            }
        });
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingMenu.toggle();
            }
        });

    }
}
