package com.example.mmpg;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class BGMService extends Service {

    private static final String TAG = "MusicService";
    MediaPlayer player;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void onCreate(){     // 서비스가 생성된다.
        Log.d(TAG,"onCreate()");
        player = MediaPlayer.create(this, R.raw.crunk_knight);
        player.setLooping(true);
    }
    public void onDestroy() {    // 서비스를 소멸 시키고 BGM 재생을 중지한다.
        Log.d(TAG, "onDestroy()");
        player.stop();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {      // 서비스를 시작한다.
        Log.d(TAG, "onStart()");
        player.start();
        return super.onStartCommand(intent, flags, startId);
    }
}
