package com.example.lzw.wallpaper.util;

import android.app.Application;

/**
 * Created by liuziwen on 17/1/5.
 */

public class MyApp extends Application {
    public static MyApp context;

    public void onCreate(){
        super.onCreate();
        context = this;
    }
}
