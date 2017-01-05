package com.example.lzw.wallpaper;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.lzw.wallpaper.sqliteDatabase.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.graphics.BitmapFactory.decodeFile;

public class ChangeService extends Service {
    int n=0;
    ArrayList<Map<String, String>> result;
    Change change;
    MyDatabaseHelper db;
    WallpaperManager wm;
    int current=0;
    Message mes;
    boolean b;
    SharedPreferences preferences;





    public int onStartCommand(Intent intent ,int flags,int startId) {
        System.out.println("s***********ervice is onstartcommand!");
        System.out.println("**************n=" + n);
            return START_STICKY;

    }

    public void onCreate(){
        super.onCreate();
        b=true;
        preferences=getSharedPreferences("mytime", Context.MODE_PRIVATE);
        System.out.println("************service is created!");
        db=new MyDatabaseHelper(this,"my.path",1);
        Cursor cursor = db.getReadableDatabase().rawQuery("select * from data ",null);
        result = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext())
        {n++;
            System.out.println("ssss*********n+"+n);

            Map<String, String> map = new HashMap<String, String>();
            // 取出查询记录中第2列、第3列的值

            map.put("name", cursor.getString(1));
            map.put("uri", cursor.getString(2));

            result.add(map);

        }
        cursor.close();
        db.close();
        wm=WallpaperManager.getInstance(this);

        change=new Change();
        change.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    class Change extends Thread{
        public void run(){System.out.println("thread run start!");

                    while(b){

                        if (n != 0) {
                            if (current >= n) {
                                current = 0;
                            }
                            System.out.println("thread  start!");

                            Map map1 = result.get(current++);
                            String path = (String) map1.get("uri");
                            try {
                                wm.setBitmap(BitmapFactory.decodeFile(path));

                                 Thread.sleep(preferences.getInt("time", 5) * 1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                }

        }
    }
   public boolean bb(){
       b=false;
       return b;
   }

    public void onDestroy(){
        System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwssssssssssstop");
        bb();
        super.onDestroy();

    }




}
