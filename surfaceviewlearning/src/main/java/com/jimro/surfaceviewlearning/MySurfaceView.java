package com.jimro.surfaceviewlearning;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * surfaceView 的基本用法
 * Created by lixichang on 2017/1/19.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MySurfaceView";
    private SurfaceHolder surfaceHolder = null;
    private CanvasThread canvasThread = null;

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        canvasThread = new CanvasThread(surfaceHolder);
    }

    /**
     * 当surfaceView被创建的时候
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (canvasThread != null) {
            canvasThread.isRun = true;
            canvasThread.start();
        }
    }

    /**
     * 当SurfaceView发生改变时
     *
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e(TAG, "surfaceChanged:  i=" + i + "i1=" + i1 + "i2=" + i2);
    }

    /**
     * 当SurfaceView被销毁时调用，释放资源
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (canvasThread != null) {
            canvasThread.isRun = false;
        }
    }

    /**
     * 子线程类用来绘制图像
     */
    class CanvasThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private Boolean isRun;
        private Paint paint;
        private TextPaint textPaint;

        public CanvasThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            isRun = true;
            paint = new Paint();
            textPaint = new TextPaint();

            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            textPaint.setTextSize(15);
            textPaint.setColor(Color.BLACK);
            textPaint.setAntiAlias(true);


        }

        @Override
        public void run() {
            super.run();
            long count = 0;
            while (isRun) {
                Canvas canvas = null;
                if (surfaceHolder != null) {
                    synchronized (surfaceHolder) {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.BLACK);
                        canvas.drawRect(100, 50, 300, 250, paint);
                        count = System.currentTimeMillis();
//                        String text = count / 1000 / 3600 % 24 + " : " + count / 1000 / 60 % 60 + " : " + count / 1000 % 60;
                        Date date = new Date(count);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy - MM - dd HH:mm:ss");
                        String text = format.format(date);
                        canvas.drawText(text, 200 - (textPaint.measureText(text) / 2), 150, textPaint);
                        try {
                            Thread.sleep(200);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
