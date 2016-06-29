package com.hanshan.circleprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    CircleProgressBar mBar1;
    CircleProgressBar mBar2;
    CircleProgressBar mBar3;
    CircleProgressBar mBar4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBar1 = (CircleProgressBar) findViewById(R.id.circle_progress1);
        mBar2 = (CircleProgressBar) findViewById(R.id.circle_progress2);
        mBar3 = (CircleProgressBar) findViewById(R.id.circle_progress3);
        mBar4 = (CircleProgressBar) findViewById(R.id.circle_progress4);
    }

    void hanleClick(View view) {
        mBar1.resetProgress();
        mBar2.resetProgress();
        mBar3.resetProgress();
        mBar4.resetProgress();
        for (int i = 0; i <= 100; i++) {
            mBar1.setProgressDelayed(i, 10);
            mBar2.setProgressDelayed(i, 50);
            mBar3.setProgressDelayed(i, 80);
            mBar4.setProgressDelayed(i, 100);
        }
    }
}
