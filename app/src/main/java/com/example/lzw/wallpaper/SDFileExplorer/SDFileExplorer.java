package com.example.lzw.wallpaper.SDFileExplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.LogRecord;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lzw.wallpaper.R;
import com.example.lzw.wallpaper.sqliteDatabase.MyDatabaseHelper;


public class SDFileExplorer extends Activity implements AbsListView.OnScrollListener
{
    ListView listView;
    TextView textView;
    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    //Bundle data;
    MyDatabaseHelper db;
    Cursor cursor;
    List<Map<String, Object>> listItems;

    MyBaseAdapter adapter;

    Map<String, Object> listItem;
    private int mFirstVisibleItem;
    //GridView中可见的图片的数量
    private int mVisibleItemCount;
    private boolean isFirstEnterThisActivity = true;
    public static Map<String,Bitmap> gridviewBitmapCaches = new HashMap<String,Bitmap>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdfile_explorer);
        //data=new Bundle();
        // 获取列出全部文件的ListView
        listView = (ListView) findViewById(R.id.list);
        textView = (TextView) findViewById(R.id.path);
        listView.setOnScrollListener(this);
        db=new MyDatabaseHelper(this,"my.path",1);
        cursor = db.getReadableDatabase().rawQuery("select * from data ",null);

        // 获取系统的SD卡的目录
        File root = new File("/mnt/sdcard/");
        // 如果 SD卡存在
        if (root.exists())
        {
            currentParent = root;
            currentFiles = root.listFiles();
            // 使用当前目录下的全部文件、文件夹来填充ListView
            inflateListView(currentFiles);
        }
        // 为ListView的列表项的单击事件绑定监听器
        listView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    final int position, long id)
            {
                if(isImage(getFileType(currentFiles[position].getName()))){
                    insert(db.getReadableDatabase(), currentFiles[position].getName(), currentFiles[position].getPath().toString());

                    Toast.makeText(SDFileExplorer.this
                            , "添加成功！",
                            Toast.LENGTH_SHORT).show();
                    Animation anim= AnimationUtils.loadAnimation(SDFileExplorer.this,R.anim.delete_listitem);
                            view.startAnimation(anim);
                }
                // 获取用户点击的文件夹下的所有文件
                else{
                File[] tmp = currentFiles[position].listFiles();
                if (tmp == null || tmp.length == 0)
                {
                    Toast.makeText(SDFileExplorer.this
                            , "当前路径不可访问或该路径下没有文件",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                    currentParent = currentFiles[position]; //②
                    // 保存当前的父文件夹内的全部文件和文件夹
                    currentFiles = tmp;
                    // 再次更新ListView
                    inflateListView(currentFiles);
                }
            }}
        });
        // 获取上一级目录的按钮
        Button parent = (Button) findViewById(R.id.parent);
        Button finish = (Button) findViewById(R.id.finish);
        parent.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View source)
            {
                try
                {
                    if (!currentParent.getCanonicalPath()
                            .equals("/"))
                    {
                        // 获取上一级目录
                        currentParent = currentParent.getParentFile();
                        // 列出当前目录下所有文件
                        currentFiles = currentParent.listFiles();
                        // 再次更新ListView
                        inflateListView(currentFiles);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });



        finish.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View source)
            {
                 db.close();
                SDFileExplorer.this.finish();
            }
        });
    }

    public static Bitmap getBitmapFromResources(Activity act, int resId) {
        Resources res = act.getResources();
        return BitmapFactory.decodeResource(res, resId);
    }

    private void inflateListView(final File[] files) //①
    {
        Bitmap bit=getBitmapFromResources(SDFileExplorer.this,R.drawable.folder);
        // 创建一个List集合，List集合的元素是Map
        listItems =new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++)
        {
           listItem =
                    new HashMap<String, Object>();
            // 如果当前File是文件夹，使用folder图标；否则使用file图标
            if (files[i].isDirectory())
            {
                listItem.put("uri", null);
            }
            else  if(isImage(getFileType(files[i].getName())))
            {

                listItem.put("uri", files[i].getPath());
            }
            else
            {
                listItem.put("uri", null);
            }
            listItem.put("fileName", files[i].getName());
            // 添加List项
            listItems.add(listItem);
        }

        adapter= new MyBaseAdapter(this, listItems,bit);
        listView.setAdapter(adapter);

        try
        {
            textView.setText("当前路径为：" + currentParent.getCanonicalPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static String getFileType(String fileName) {
        if (fileName != null) {
            int typeIndex = fileName.lastIndexOf(".");
            if (typeIndex != -1) {
                String fileType = fileName.substring(typeIndex + 1)
                        .toLowerCase();
                return fileType;
            }
        }
        return "";
    }

    /**
     * 根据后缀名判断是否是图片文件
     *
     * @param type
     * @return 是否是图片结果true or false
     */
    public static boolean isImage(String type) {
        if (type != null
                && (type.equals("jpg") || type.equals("gif")
                || type.equals("png") || type.equals("jpeg")
                || type.equals("bmp") || type.equals("wbmp")
                || type.equals("ico") || type.equals("jpe"))) {
            return true;
        }
        return false;
    }


    private void insert(SQLiteDatabase db,String ed1,String ed2){
            db.execSQL("insert into data values(null,?,?)",new String[]{ed1,ed2});
    }




    //释放图片的函数
    private void recycleBitmapCaches(int fromPosition,int toPosition){
        Bitmap delBitmap = null;
        for(int del=fromPosition;del<toPosition;del++){
            delBitmap = gridviewBitmapCaches.get(listItems.get(del).get("uri"));
            if(delBitmap != null){

                //如果非空则表示有缓存的bitmap，需要清理
                Log.e( "release position:" , "");
                //从缓存中移除该del->bitmap的映射
                gridviewBitmapCaches.remove(listItems.get(del).get("uri"));
                delBitmap.recycle();
                delBitmap = null;
            }
        }
    }




    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
        //注释：firstVisibleItem为第一个可见的Item的position，从0开始，随着拖动会改变
        //visibleItemCount为当前页面总共可见的Item的项数
        //totalItemCount为当前总共已经出现的Item的项数
        recycleBitmapCaches(0,firstVisibleItem);
        recycleBitmapCaches(firstVisibleItem+visibleItemCount, totalItemCount);

    }


    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
    }


}
