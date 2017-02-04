package com.jimro.wclflipview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jimro.wclflipview.view.WclFlipView;

public class MainActivity extends AppCompatActivity implements WclFlipView.CardCheckoutListener {

    private LinearLayout containerView;
    private boolean isClicked = true;
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerView = (LinearLayout) findViewById(R.id.container_wclflipview);
        //设置检查监听
        setCheckout();

    }

    @Override
    protected void onResume() {
        super.onResume();
        width = getResources().getDisplayMetrics().widthPixels;
    }

    private void setCheckout() {
        for (int i = 0; i < containerView.getChildCount(); i++) {
            ((WclFlipView) containerView.getChildAt(i)).setCardCheckoutListener(this);
        }
    }


    /**
     * 接口回调，要求每次点击都要检查当前ViewGroup中是否有卡片已经被点击了
     */
    @Override
    public void checkoutCard() {
        //检查是否有卡片已经被打开了
        for (int i = 0; i < containerView.getChildCount(); i++) {
            WclFlipView wclFlipView = (WclFlipView) containerView.getChildAt(i);
            if (!wclFlipView.getIsFront()) {
                isClicked = false;
            }
        }
        //如果当前viewGroup中有卡片已经被点击了，设置所有卡片不可点击
        if (!isClicked) {
            for (int i = 0; i < containerView.getChildCount(); i++) {
                ((WclFlipView) containerView.getChildAt(i)).setIsFront(false);
            }
        }
    }

    /**
     * 点击重置，之后删除当前的卡片重新创建新的卡片
     *
     * @param view
     */
    public void onClick(View view) {
        isClicked = true;
        for (int i = 0; i < containerView.getChildCount(); i++) {
            containerView.removeViewAt(i);
            WclFlipView wclFlipView = new WclFlipView(this);
            wclFlipView.setCardCheckoutListener(this);
            ViewGroup.LayoutParams layoutParams =
                    new ViewGroup.LayoutParams(width / 3, width / 3 * 2);
            wclFlipView.setLayoutParams(layoutParams);
            containerView.addView(wclFlipView, i);
        }
    }
}
