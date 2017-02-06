package com.jimro.audiobackgroundview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.jimro.audiobackgroundview.R;

/**
 * 音频条形图自定义view
 * Created by lixichang on 2017/2/6.
 */

public class AudioBackgroundView extends View {

    //渐变色顶部的颜色
    private int mTopColor;
    //渐变色底部的颜色
    private int mBottomColor;
    //view延迟绘制的时间
    private int mDelayTime;

    //小矩形的数量
    private int mRectCount;
    //每个小矩形之间的偏移量
    private float mRectOffset;
    //小矩形的宽度
    private int mRectWidth;
    //整个view的高度
    private int mHeight;
    //整个view的宽度
    private int mWidth;
    //当前小矩形随机的高度
    private int[] mCurRectHeight;
    //画小矩形的画笔SS
    private Paint mPaint;
    //小矩形颜色渐变的效果(线性渐变)
    private LinearGradient mLinearGradient;

    public AudioBackgroundView(Context context) {
        this(context, null);
    }

    public AudioBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        //获取属性集合
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AudioBackgroundView);
        if (typedArray != null) {
            //获取小矩形渐变顶部的颜色
            mTopColor = typedArray.getColor(R.styleable.AudioBackgroundView_topColor, Color.BLUE);
            //获取小矩形渐变底部的颜色
            mBottomColor = typedArray.getColor(R.styleable.AudioBackgroundView_bottomColor, Color.RED);
            //获取小矩形的总数
            mRectCount = typedArray.getInt(R.styleable.AudioBackgroundView_rectCount, 20);
            //获取view延迟重绘的时间
            mDelayTime = typedArray.getInt(R.styleable.AudioBackgroundView_delayTime, 300);
            //获取每个小矩形之间的偏移量
            mRectOffset = typedArray.getDimension(R.styleable.AudioBackgroundView_rectOffset, 3);
            typedArray.recycle();
        }
    }


    /**
     * 重写view的测量方法
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取宽高的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        //获取宽高的精确值
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //设置view的默认宽高
        int defaultWidth = 400;
        int defaultHeight = 300;

        //如果layout_width和layout_height为match_parent或者具体的数值，则就是用测量的宽高
        //如果layout_width和layout_height为wrap_content，则就是用默认的宽高
        setMeasuredDimension((MeasureSpec.EXACTLY == widthMode) ? widthSize : defaultWidth,
                (MeasureSpec.EXACTLY == heightMode) ? heightSize : defaultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurRectHeight != null) {
            for (int i = 0; i < mRectCount; i++) {
                canvas.drawRect((mRectOffset + mRectWidth) * i, mHeight - mCurRectHeight[i],
                        (mRectOffset + mRectWidth) * i + mRectWidth, mHeight, mPaint);
            }
        } else {
            //使用随机高度
            for (int i = 0; i < mRectCount; i++) {
                float left = (mRectOffset + mRectWidth) * i;
                float top = (float) (mHeight - Math.random() * mHeight);
                float right = (mRectOffset + mRectWidth) * i + mRectWidth;
                float bottom = mHeight;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }

        //延迟重绘条形图
        //如果用户没有设置mCurRectHeight数组的话，onDraw会一直随机生成并重绘
        //用户在外设置mCurRectHeight是要不断改变mRectHeight的值，才能到达动态音频条形图的效果，
        //如果在外只设置一次mCurRectHeight的话，onDraw会一直提交同一个数值，所以达不到动态的目的
        postInvalidateDelayed(mDelayTime);
    }

    /**
     * 在测量之后获取整个view的宽高
     * <p>
     * 继承与View和继承与现有控件都是下面的顺序，但是控件的大小是生成之后就固定的，不会再次改变。
     * onMeasure()→onSizeChanged()→onLayout()→onMeasure()→onLayout()→onDraw()
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getHeight();
        //计算小矩形的宽度
        mRectWidth = (int) (mWidth / mRectCount - mRectOffset);
        //设置渲染效果
        mLinearGradient = new LinearGradient(0, 0, mRectWidth, mHeight,
                mBottomColor, mTopColor, Shader.TileMode.CLAMP);
        mPaint.setShader(mLinearGradient);
    }


    /**
     * 对外提供设置当前条形图高度的方法
     * 用户如果想要达到条形图动态变化，就必须要不断的改变mCurRectHeight的值
     *
     * @param mCurRectHeight
     */
    public void setCurRectHeight(int[] mCurRectHeight) {
        this.mCurRectHeight = mCurRectHeight;
    }

    /**
     * 对外提供条形图小矩形的总个数
     *
     * @return
     */
    public int getRectCount() {
        return this.mRectCount;
    }
}
