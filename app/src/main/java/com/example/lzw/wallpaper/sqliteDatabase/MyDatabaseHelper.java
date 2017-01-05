package com.example.lzw.wallpaper.sqliteDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lzw.wallpaper.util.MyApp;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public final static String DBName = "image_path_db";
    public final static String TABLE = "data";
    private static final int VERSION = 1;
    final String CREATE_TABLE_SQL = "create table data(id integer primary key autoincrement, name , uri)";

    public MyDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    public MyDatabaseHelper(){
        this(MyApp.context, DBName, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db
            , int oldVersion, int newVersion) {
        System.out.println("--------onUpdate Called--------"
                + oldVersion + "--->" + newVersion);
    }


}