package com.jimro.wclflipview.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.jimro.wclflipview.R;


/**
 * 正反面旋转的卡片View
 * Created by lixichang on 2017/2/4.
 */

public class WclFlipView extends FrameLayout {
    private View backView;
    private Context mContext;
    private View frontView;
    private View view;
    private boolean isFront = true;
    private Animator animatorIn;
    private Animator animatorOut;
    private CardCheckoutListener cardCheckoutListener;
    private int width;

    public WclFlipView(Context context) {
        this(context, null);

    }

    public WclFlipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public WclFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    public boolean getIsFront() {
        return isFront;
    }

    public WclFlipView setIsFront(boolean isFront) {
        this.isFront = isFront;
        return this;
    }

    public WclFlipView setCardCheckoutListener(CardCheckoutListener cardCheckoutListener) {
        this.cardCheckoutListener = cardCheckoutListener;
        return this;
    }

    /**
     * 初始化view
     */
    private void init() {
        view = LayoutInflater.from(mContext).inflate(R.layout.card_view, this, true);
        frontView = view.findViewById(R.id.front_card);
        backView = view.findViewById(R.id.back_card);

        //设置卡片的宽高，宽为当前屏幕的1/3，高为宽的两倍
        width = mContext.getResources().getDisplayMetrics().widthPixels;
        LayoutParams layoutParams = new LayoutParams(width / 3, width / 3 * 2);
        frontView.setLayoutParams(layoutParams);
        backView.setLayoutParams(layoutParams);

        //初始化动画
        initAnimator();

        // 改变视角距离, 贴近屏幕
        int distance = 16000;
        float scale = getResources().getDisplayMetrics().density * distance;
        frontView.setCameraDistance(scale);
        backView.setCameraDistance(scale);
    }

    /**
     * 初始化动画
     */
    private void initAnimator() {

        animatorIn = AnimatorInflater.loadAnimator(mContext, R.animator.anim_in);
        animatorOut = AnimatorInflater.loadAnimator(mContext, R.animator.anim_out);
    }

    /**
     * 拦截当前的点击事件交给onTouchEvent来处理
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                onTouchEvent(ev);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 处理用户的点击事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!isFront) {
                    animatorIn.setTarget(frontView);
                    animatorOut.setTarget(backView);
                    animatorIn.start();
                    animatorOut.start();
                    isFront = true;
                } else {
                    animatorIn.setTarget(backView);
                    animatorOut.setTarget(frontView);
                    animatorIn.start();
                    animatorOut.start();
                    isFront = false;
                }
                break;
        }

        //每次都要进行检查是否有卡片已经被点击了
        if (cardCheckoutListener != null) {
            cardCheckoutListener.checkoutCard();
        }
        //使得卡片被点击一次之后就不能点击
        if (isFront) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查卡片是否被点击的接口
     */
    public interface CardCheckoutListener {
        void checkoutCard();
    }
}
