package com.jimro.audiobackgroundview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jimro.audiobackgroundview.view.AudioBackgroundView;

public class MainActivity extends AppCompatActivity {

    private AudioBackgroundView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (AudioBackgroundView) findViewById(R.id.audio_background_view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int[] mHeight = new int[view.getRectCount()];
                    for (int i = 0; i < view.getRectCount(); i++) {
                        mHeight[i] = (int) (Math.random() * view.getHeight());
                    }
                    view.setCurRectHeight(mHeight);
                }
            }
        }).start();
    }


}
