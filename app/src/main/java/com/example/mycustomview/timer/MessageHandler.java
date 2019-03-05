package com.example.mycustomview.timer;

import android.os.Handler;
import android.os.Message;

import com.example.mycustomview.utils.LogUtil;
import com.example.mycustomview.view.WheelView;

public class MessageHandler extends Handler {
    public static final int WHAT_INVALIDATE_LOOP_VIEW=1000;
    public static final int WHAT_SMOOTH_SCROLL=2000;
    public static final int WHAT_ITEM_SELECTED=3000;
    private final WheelView wheelView;
    public MessageHandler(WheelView wheelView){
        this.wheelView=wheelView;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case WHAT_INVALIDATE_LOOP_VIEW:
//                LogUtil.e("what_invalidate");
                wheelView.invalidate();
                break;
            case WHAT_SMOOTH_SCROLL:
                LogUtil.e("what_smooth_scroll");
                wheelView.smoothScroll(WheelView.ACTION.FLING);
                break;
            case WHAT_ITEM_SELECTED:
                wheelView.onItemSelected();
                break;
        }
    }
}
