package com.example.mycustomview.activity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import com.example.mycustomview.R;
import com.example.mycustomview.view.MeiTuanRefreshFirstStepView;
import com.example.mycustomview.view.MeiTuanRefreshSecondStepView;

public class MeiTuanHeaderViewAnimationAct extends AppCompatActivity {
    private SeekBar seekBar;
    private MeiTuanRefreshFirstStepView firstStepView;
    private MeiTuanRefreshSecondStepView secondStepView;
    private MeiTuanRefreshSecondStepView thirdStepView;
    private AnimationDrawable thirdAnim;
    private Handler mHandler=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_bitmap);
        seekBar=findViewById(R.id.scale_bitmap_seekbar);
        firstStepView=findViewById(R.id.scale_bitmap_first_step_view);
        secondStepView=findViewById(R.id.scale_bitmap_second_step_view);
        secondStepView.setBackgroundResource(R.drawable.meituan_second_step_anim);
        thirdStepView=findViewById(R.id.scale_bitmap_third_step_view);
        final AnimationDrawable secondAnim=(AnimationDrawable)secondStepView.getBackground();
        thirdAnim=(AnimationDrawable)thirdStepView.getBackground();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale=(float)progress/seekBar.getMax();

                if(progress==seekBar.getMax()){
                    firstStepView.setVisibility(View.GONE);
                    secondStepView.setVisibility(View.VISIBLE);
                    secondAnim.start();
                    int length=0;
                    for(int i=0;i<secondAnim.getNumberOfFrames();i++){
                        length+=secondAnim.getDuration(i);
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            secondAnim.stop();
                            secondStepView.setVisibility(View.GONE);
                            thirdStepView.setVisibility(View.VISIBLE);
                            thirdAnim.start();
                        }
                    },length);
                }else{
                    firstStepView.setVisibility(View.VISIBLE);
                    secondAnim.stop();
                    secondStepView.setVisibility(View.GONE);
                    thirdStepView.setVisibility(View.GONE);
                    firstStepView.setProgress(scale);
                    firstStepView.postInvalidate();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

}
