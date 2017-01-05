package com.example.lzw.wallpaper;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.example.lzw.wallpaper.sqliteDatabase.DBUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChangeWallpaperService extends Service {
    public static final String log = "ChangeWallpaperService";
    public static boolean isServiceRunning = false;
    ArrayList<Map<String, String>> result;
    WallpaperManager wm;
    int current = 0;
    SharedPreferences preferences;

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (wm == null) {
                wm = WallpaperManager.getInstance(ChangeWallpaperService.this);
            }
            try {
                if (current >= result.size()) {
                    current = 0;
                }
                Map map1 = result.get(current++);
                String path = (String) map1.get("uri");
                wm.setStream(new FileInputStream(new File(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(log, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(log, "onCreate");
        isServiceRunning = true;
        preferences = getSharedPreferences("mytime", Context.MODE_PRIVATE);
        result = (ArrayList<Map<String, String>>) DBUtil.getInstance().queryAll();
        wm = WallpaperManager.getInstance(this);
        timer.schedule(timerTask, 0, preferences.getInt("time", 5) * 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(log, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(log, "onDestroy");
        super.onDestroy();
        isServiceRunning = false;
        timer.cancel();
    }
}
