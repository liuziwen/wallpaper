package com.example.lzw.wallpaper.sqliteDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuziwen on 17/1/5.
 */

public class DBUtil {
    private static MyDatabaseHelper myDatabaseHelper;
    private static DBUtil dbUtil;
    private static SQLiteDatabase db;

    private DBUtil() {
        if (myDatabaseHelper == null) {
            myDatabaseHelper = new MyDatabaseHelper();
            db = myDatabaseHelper.getReadableDatabase();
        }
    }

    public static DBUtil getInstance() {
        if (dbUtil == null) {
            synchronized (DBUtil.class) {
                if (dbUtil == null) {
                    dbUtil = new DBUtil();
                }
            }
        }
        return dbUtil;
    }

    public List<Map<String, String>> queryAll() {
        Cursor cursor = db.rawQuery("select * from data ", null);
        List<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<String, String>();
            // 取出查询记录中第2列、第3列的值
            map.put("id", cursor.getString(0));
            map.put("name", cursor.getString(1));
            map.put("uri", cursor.getString(2));
            if (!new File(cursor.getString(2)).exists()) {
                delete("id", cursor.getString(0));
            } else {
                result.add(map);
            }
        }
        cursor.close();
        return result;
    }

    public void insert(String name, String url) {
        db.execSQL("insert into data values(null,?,?)", new String[]{name, url});
    }

    public boolean delete(String key, String value) {
        int i = db.delete(MyDatabaseHelper.TABLE, key+" = ?", new String[]{value});
        return i > 0;
    }

    public boolean update(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("id", key);
        int i = db.update(MyDatabaseHelper.TABLE, cv, key + " = ?", new String[]{value});
        return i > 0;
    }
}
