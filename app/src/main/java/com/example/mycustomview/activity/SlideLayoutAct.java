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
import com.example.mycustomview.viewgroup.SlidingLayout;

public class SlideLayoutAct extends AppCompatActivity {
    private ListView contentListView;
    private Button menuButton;
    private String[] contents=new String[]{"item1","item2","item3","item4","item5","item6",
            "item7","item8","item9","item10","item11","item12","item13","item14","item15",
            "item16","item17","item18","item19","item20"};
    private SlidingLayout slidingLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_layout);

        slidingLayout=findViewById(R.id.sliding_layout);
        contentListView=findViewById(R.id.contentListView);
        menuButton=findViewById(R.id.slide_layout_menu_button);

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,contents);
        contentListView.setAdapter(adapter);
        contentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SlideLayoutAct.this, contents[position], Toast.LENGTH_SHORT).show();
            }
        });

        slidingLayout.setScrollEvent(contentListView);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(slidingLayout.isLeftLayoutVisible()){
                    slidingLayout.scrollToRightLayout();
                }else{
                    slidingLayout.scrollToLeftLayout();
                }
            }
        });
    }

}
