package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lzw.wallpaper.Net.NetPicture;
import com.example.lzw.wallpaper.PictureSet.BitmapUtilities;
import com.example.lzw.wallpaper.PictureSet.SetBeauty;
import com.example.lzw.wallpaper.SDFileExplorer.SDFileExplorer;
import com.example.lzw.wallpaper.sqliteDatabase.MyDatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyActivity extends Activity {
    //相片路径
    String mCurrentPhotoPath;
    Button  cancle, delete;
    //MyBaseAdapter adapter;
    public static GridView grid;
    int n, visi = View.GONE;
    MyDatabaseHelper db;
    String path;
    String id[];
    ArrayList<Map<String, String>> result;
    static boolean ischeck[];
    BaseAdapter mba;
    RelativeLayout main;
    Holder holder;
    RelativeLayout relativeLayout;
    boolean bb = true,startorstop=true;
    LayoutInflater viewInflator;
    TextView count;
    public static int screenWidth;
    public static int screenHeight;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences=getSharedPreferences("mytime", Context.MODE_PRIVATE);
        editor=preferences.edit();
        int count1=preferences.getInt("count",0);
        if(count1==0){Intent in=new Intent(MyActivity.this,BjActivity.class);
            startActivity(in);}
        editor.putInt("count",++count1);
        editor.commit();

        editor=preferences.edit();
        setContentView(R.layout.activity_my);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        main = (RelativeLayout) findViewById(R.id.main);

        cancle = (Button) findViewById(R.id.cancle);
        delete = (Button) findViewById(R.id.delete);
        grid = (GridView) findViewById(R.id.grid1);
        LinearLayout add = (LinearLayout) findViewById(R.id.add);
        //add2 = (ImageButton) findViewById(R.id.add2);
        LinearLayout add2b = (LinearLayout) findViewById(R.id.add2b);
        LinearLayout tp = (LinearLayout) findViewById(R.id.takephoto);
        count = (TextView) findViewById(R.id.textcount);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        holder = new Holder();

        db = new MyDatabaseHelper(this, "my.path", 1);


        result = quary();
        mba = new MyBaseAdapter(this);

        grid.setAdapter(mba);

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent picture = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, 1);

            }
        });

        add2b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this, SDFileExplorer.class);
                startActivity(intent);
            }
        });

        //拍照
        tp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File wallpaper = new File("/sdcard/wallpaper/");
                if (!wallpaper.exists()) {
                    wallpaper.mkdirs();
                }
                File storageDir = new File("/mnt/sdcard/wallpaper/takephoto");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
//                // Save a file: path for use with ACTION_VIEW intents

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                        mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();

                        if (!photoFile.exists()) photoFile.mkdir();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, 2);
                    }
                }


            }
        });



        cancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                relativeLayout.setVisibility(View.GONE);
                visi = View.GONE;
                //grid.setAdapter(mba);
                bb = true;

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                for (int i = 0; i < n; i++) {
                    System.out.println(i+"=="+ischeck[i]);
                    if (ischeck[i]) {
                        deletei(db.getReadableDatabase(), id[i]);
                    }
                }
                Toast.makeText(MyActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.GONE);
                visi = View.GONE;
                refresh();
                bb = true;
            }
        });

final SwipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
       // final TextView tv=(TextView)findViewById(R.id.textView1);
        //设置刷新时动画的颜色，可以设置4个
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                //tv.setText("正在刷新");
                // TODO Auto-generated method stub
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        //tv.setText("刷新完成");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
                refresh();
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Cursor c = this.getContentResolver().query(selectedImage, null, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(MediaStore.Images.Media.DATA);
            String picturePath = c.getString(columnIndex);
            String name=c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            c.close();
            insert(db.getReadableDatabase(),name,picturePath);
        }

        if (requestCode == 2 && resultCode == Activity.RESULT_OK ) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }




    public final class Holder {
        public CheckBox cb;
        public ImageView image;

    }

    public class MyBaseAdapter extends BaseAdapter {

        public MyBaseAdapter(Context context) {
            viewInflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return n;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            convertView = viewInflator.inflate(R.layout.cell, null);
            final ImageView image = (ImageView) convertView.findViewById(R.id.image1);
            final CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb1);
            cb.setVisibility(visi);

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {

                    ischeck = new boolean[n];
                    for (int i = 0; i < n; i++) {
                        ischeck[i] = false;
                    }
                    relativeLayout.setVisibility(View.VISIBLE);
                    visi = View.VISIBLE;
                    //grid.setAdapter(mba);
                    bb = false;
                    return true;
                }
            });

            id = new String[n];
            Map map = result.get(position);
            path = (String) map.get("uri");
            final String name=(String) map.get("name");

                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(bb){
                        //image.setAlpha(0.5f);
                        Bundle bundle = new Bundle();
                        Map map = result.get(position);
                        path = (String) map.get("uri");
                        bundle.putString("uri", path);
                        bundle.putString("name", name);

                        Intent intent = new Intent(MyActivity.this, SetBeauty.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_anim_in,R.anim.activity_anim_out);
                        }
                        else {

                            if (cb.isChecked()) {
                                cb.setChecked(false);
                                ischeck[position] = false;
                            } else {
                                cb.setChecked(true);
                                ischeck[position] = true;
                            }
                            System.out.println(position+"=="+ischeck[position]);
                        }
                    }
                });


            for (int i = 0; i < n; i++) {
                map = result.get(i);
                id[i] = (String) map.get("id");
            }
            GetBitmap task=new GetBitmap(image);
            task.execute(path);
            image.setImageResource(R.drawable.ic_refresh_white_36dp);
            return convertView;
        }


        class GetBitmap extends AsyncTask<String,Integer,Bitmap>{
            ImageView iv;
            GetBitmap(ImageView iv){this.iv=iv;}
            @Override
            protected Bitmap doInBackground(String... params) {
                return BitmapUtilities.returnSquareBitmap(params[0]);
            }
            protected void onPostExecute(Bitmap bitmap){
                iv.setImageBitmap(bitmap);
            }
        }
    }



    public ArrayList<Map<String, String>> quary() {
        Cursor cursor = db.getReadableDatabase().rawQuery("select * from data ", null);
        ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
        n = 0;
        while (cursor.moveToNext()) {

            Map<String, String> map = new HashMap<String, String>();
            // 取出查询记录中第2列、第3列的值
            //判断uri对应的图片是否存在
            File whether=new File(cursor.getString(2));
            if(whether.exists()){
                n++;
                map.put("id", cursor.getString(0));
                map.put("name", cursor.getString(1));
                map.put("uri", cursor.getString(2));

                result.add(map);}

          else  deletei(db.getReadableDatabase(), cursor.getString(0));;

        }

        return result;
    }




//    public void onDestroy() {
//
//        super.onDestroy();
//
//            File netimagecache=new File("/mnt/sdcard/wallpaper/netimagecache");
//            if(netimagecache.exists()){filedelete(netimagecache);}
//        }


        public static void filedelete(File file) {
            if (file.isFile()) {
                file.delete();
                return;
            }

            if(file.isDirectory()){
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                    return;
                }

                for (int i = 0; i < childFiles.length; i++) {
                    filedelete(childFiles[i]);
                }
                file.delete();
            }
        }

    public void refresh(){
        result=quary();
        mba.notifyDataSetChanged();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.set_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理动作按钮的点击事件
        switch (item.getItemId()) {
            case R.id.settime: {
                Intent intent = new Intent(MyActivity.this, SetTime.class);
                startActivity(intent);
            }
            return true;

            case R.id.setchange: {
                Intent intent = new Intent(MyActivity.this, Hlp.class);
                startActivity(intent);
            }
            return true;

            case R.id.net: {
                Intent intent = new Intent(MyActivity.this, NetPicture.class);
                startActivity(intent);
            }
            return true;

            case R.id.s: {
                final Intent intent = new Intent(MyActivity.this, ChangeService.class);
                if(startorstop){
                    if(n==0){ Toast.makeText(MyActivity.this, "请先添加一些图片！", Toast.LENGTH_SHORT).show();}
                    else {
                        startService(intent);
                        Toast.makeText(MyActivity.this, "更换壁纸成功！", Toast.LENGTH_SHORT).show();
                    }
                    startorstop=false;
                    item.setIcon(R.drawable.ic_pause_circle_outline_white_36dp);
                }
                else {
                    Toast.makeText(MyActivity.this, "已停止！", Toast.LENGTH_SHORT).show();
                    stopService(intent);

                    item.setIcon(R.drawable.ic_play_circle_outline_white_36dp);
                    startorstop=true;

                }

            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deletei(SQLiteDatabase db, String n) {
        db.delete("data", "id=?", new String[]{n});
    }

    private void insert(SQLiteDatabase db,String ed1,String ed2){
            db.execSQL("insert into data values(null,?,?)",new String[]{ed1,ed2});
            Toast.makeText(MyActivity.this, "图片已添加！！", Toast.LENGTH_SHORT).show();
    }

    public void onResume() {
        super.onResume();
        System.out.println("resumeIIIIIIIIIIIIIIIIIIIIIIII");
    }

    public void onRestart() {
        super.onRestart();
        System.out.println("restartIIIIIIIIIIIIIIIIIIIIIIII");
    }

    public void onPause() {
        super.onPause();
        System.out.println("pauseIIIIIIIIIIIIIIIIIIIIIIII");
    }

    public void onDestroy() {
        super.onDestroy();
        System.out.println("destroyIIIIIIIIIIIIIIIIIIIIIIII");
    }


}

