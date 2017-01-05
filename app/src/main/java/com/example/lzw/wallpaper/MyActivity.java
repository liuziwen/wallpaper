package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lzw.wallpaper.Net.NetPicture;
import com.example.lzw.wallpaper.PictureSet.BitmapUtilities;
import com.example.lzw.wallpaper.PictureSet.ProcessPictureActivity;
import com.example.lzw.wallpaper.SDFileExplorer.SDFileExplorer;
import com.example.lzw.wallpaper.sqliteDatabase.DBUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class MyActivity extends Activity implements View.OnClickListener {
    //拍照所得相片路径
    public String mCurrentPhotoPath;

    public GridView grid;
    public BaseAdapter mba;

    public ArrayList<Map<String, String>> result;

    int isCheckBoxVisible = View.GONE;
    public String imageId[];
    static boolean isCheckBoxCheck[];
    RelativeLayout relativeLayout;
    private boolean isCanJump = true;
    private boolean startorstop = true;
    LayoutInflater viewInflator;
    TextView count;

    public static int screenWidth;
    public static int screenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        Button cancle = (Button) findViewById(R.id.cancle);
        Button delete = (Button) findViewById(R.id.delete);

        grid = (GridView) findViewById(R.id.grid1);
        LinearLayout add = (LinearLayout) findViewById(R.id.add);
        LinearLayout add2b = (LinearLayout) findViewById(R.id.add2b);
        LinearLayout tp = (LinearLayout) findViewById(R.id.takephoto);
        count = (TextView) findViewById(R.id.textcount);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        result = (ArrayList<Map<String, String>>) DBUtil.getInstance().queryAll();
        mba = new MyBaseAdapter(this);
        grid.setAdapter(mba);

        add.setOnClickListener(this);
        add2b.setOnClickListener(this);
        tp.setOnClickListener(this);
        cancle.setOnClickListener(this);
        delete.setOnClickListener(this);
        final SwipeRefreshLayout
                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

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
            String name = c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            c.close();
            DBUtil.getInstance().insert(name, picturePath);
            Toast.makeText(MyActivity.this, "图片已添加！！", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, 1);
                break;
            case R.id.add2b:
                Intent intent = new Intent(MyActivity.this, SDFileExplorer.class);
                startActivity(intent);
                break;
            case R.id.takephoto:
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
                break;
            case R.id.cancle:
                relativeLayout.setVisibility(View.GONE);
                isCheckBoxVisible = View.GONE;
                isCanJump = true;
                break;
            case R.id.delete:
                for (int i = 0; i < result.size(); i++) {
                    System.out.println(i + "==" + isCheckBoxCheck[i]);
                    if (isCheckBoxCheck[i]) {
                        DBUtil.getInstance().delete("id", imageId[i]);
                    }
                }
                Toast.makeText(MyActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.GONE);
                isCheckBoxVisible = View.GONE;
                refresh();
                isCanJump = true;
                break;
        }
    }


    static class ViewHolder {
        public CheckBox checkBox;
        public ImageView image;
    }

    public class MyBaseAdapter extends BaseAdapter {

        public MyBaseAdapter(Context context) {
            viewInflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return result.size();
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
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = viewInflator.inflate(R.layout.cell, null);
                viewHolder = new ViewHolder();
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.cb1);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.image1);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.checkBox.setVisibility(isCheckBoxVisible);
            imageId = new String[result.size()];
            Map map = result.get(position);
            final String[] checkedImagePath = {(String) map.get("uri")};
            final String name = (String) map.get("name");

            for (int i = 0; i < result.size(); i++) {
                map = result.get(i);
                imageId[i] = (String) map.get("id");
            }
            GetBitmap task = new GetBitmap(viewHolder.image);
            task.execute(checkedImagePath[0]);
            viewHolder.image.setImageResource(R.drawable.ic_refresh_white_36dp);

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (isCanJump) {
                        //image.setAlpha(0.5f);
                        Bundle bundle = new Bundle();
                        Map map = result.get(position);
                        checkedImagePath[0] = (String) map.get("uri");
                        bundle.putString("uri", checkedImagePath[0]);
                        bundle.putString("name", name);
                        Intent intent = new Intent(MyActivity.this, ProcessPictureActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_anim_in, R.anim.activity_anim_out);
                    } else {
                        if (viewHolder.checkBox.isChecked()) {
                            viewHolder.checkBox.setChecked(false);
                            isCheckBoxCheck[position] = false;
                        } else {
                            viewHolder.checkBox.setChecked(true);
                            isCheckBoxCheck[position] = true;
                        }
                    }
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (isCheckBoxCheck == null || isCheckBoxCheck.length != result.size()) {
                        isCheckBoxCheck = new boolean[result.size()];
                    }
                    for (int i = 0; i < result.size(); i++) {
                        isCheckBoxCheck[i] = false;
                    }
                    relativeLayout.setVisibility(View.VISIBLE);
                    isCheckBoxVisible = View.VISIBLE;
                    isCanJump = false;
                    return true;
                }
            });
            return convertView;
        }


        class GetBitmap extends AsyncTask<String, Integer, Bitmap> {
            ImageView iv;

            GetBitmap(ImageView iv) {
                this.iv = iv;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                return BitmapUtilities.returnSquareBitmap(params[0]);
            }

            protected void onPostExecute(Bitmap bitmap) {
                iv.setImageBitmap(bitmap);
            }
        }
    }


    public static void filedelete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
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

    public void refresh() {
        result = (ArrayList<Map<String, String>>) DBUtil.getInstance().queryAll();
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
                final Intent intent = new Intent(MyActivity.this, ChangeWallpaperService.class);
                if (startorstop) {
                    if (result.size() == 0) {
                        Toast.makeText(MyActivity.this, "请先添加一些图片！", Toast.LENGTH_SHORT).show();
                    } else {
                        startService(intent);
                        Toast.makeText(MyActivity.this, "更换壁纸成功！", Toast.LENGTH_SHORT).show();
                    }
                    startorstop = false;
                    item.setIcon(R.drawable.ic_pause_circle_outline_white_36dp);
                } else {
                    Toast.makeText(MyActivity.this, "已停止！", Toast.LENGTH_SHORT).show();
                    stopService(intent);
                    item.setIcon(R.drawable.ic_play_circle_outline_white_36dp);
                    startorstop = true;
                }

            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onResume() {
        super.onResume();
        refresh();
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

