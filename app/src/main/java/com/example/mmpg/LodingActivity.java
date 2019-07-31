package com.example.mmpg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LodingActivity extends Activity {

    private ProgressBar mProgress;
    private int mProgressStatus = 0;
    int i = 0;
    TextView text;
    Intent intent;

    public static Activity _LoadingActivity;    // 게임 액티비티에서 로딩 액티비티를 종료하기 위해 선언

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_loading);
        mProgress = findViewById(R.id.progress_bar);

        _LoadingActivity = LodingActivity.this; // 게임 액티비티에서 로딩 액티비티를 종료하기 위해 선언

         new Thread(new Runnable() {    // 스레드로 프로그레스 바의 값을 증가시킨다.
             @Override
             public void run() {
                 while (mProgressStatus < 100) {
                     try {
                         Thread.sleep(50);
                     } catch (InterruptedException e) {
                     }
                     mProgressStatus = i++;

                     mProgress.post(new Runnable() {
                         @Override
                         public void run() {
                             mProgress.setProgress(mProgressStatus);
                         }
                     });
                 }
                 text = findViewById(R.id.loadingText);
                 text.setText("화면을 터치하세요...");
             }

         }).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mProgressStatus >= 100) {   // 프로그레스 바가 다 차고 나서 터치하면 게임플레이 액티비티로 전환
           intent = new Intent(LodingActivity.this,GamePlayActivity.class);
           startActivity(intent);
           return true;
        }
        return super.onTouchEvent(event);
    }
}


