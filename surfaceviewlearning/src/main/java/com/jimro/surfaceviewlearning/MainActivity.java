package com.jimro.surfaceviewlearning;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private MySurfaceView mySurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySurfaceView = (MySurfaceView) findViewById(R.id.my_surfaceView);
    }

    /**
     * 自己处理当用户点击换回按钮时销毁surfaceView
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                mySurfaceView.surfaceDestroyed(mySurfaceView.getHolder());
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}

