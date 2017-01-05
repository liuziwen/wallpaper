package com.example.lzw.wallpaper.PictureSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lzw.wallpaper.MyActivity;
import com.example.lzw.wallpaper.R;

import java.io.File;
import java.io.FileOutputStream;


public class ProcessPictureActivity extends Activity implements View.OnClickListener{
    ImageView sourceImage, icon;//添加的小图片
    TextView inputtext;
    RelativeLayout beautyLayout;
    public static String ppath, pname;
    private Canvas canvas;
    //涂鸦画笔
    private Paint graffitiPaint;
    //文字画笔
    Paint textPaint = new Paint();
    private Matrix ma = new Matrix();
    AlertDialog.Builder pbuilder;
    public static Bitmap bitmap, baseBitmap;
    ProgressBar progressBar;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bbutton1:
                if (baseBitmap != null) {
                    saveBitmapToSDCard(baseBitmap, pname);
                } else saveBitmapToSDCard(bitmap, pname);
                Toast.makeText(ProcessPictureActivity.this, "成功保存到wallpaper文件夹！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bbutton2:
                sourceImage.setDrawingCacheEnabled(true);
                bitmap = Bitmap.createBitmap(sourceImage.getDrawingCache());
                sourceImage.setDrawingCacheEnabled(false);
                baseBitmap = blurBitmap(bitmap);
                sourceImage.setImageBitmap(baseBitmap);
                bitmap = baseBitmap;
                baseBitmap = null;
                break;
            case R.id.bp:
                addPicture();
                break;
            case R.id.bbutton3:
                changeGraffitiPaint();
                break;
            case R.id.bbutton4:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_TEXT, "I have successfully share my message through my app ");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/mnt/sdcard/wallpaper/" + pname + ".png")));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                break;
            case R.id.bbutton5:
                addText();
                break;
        }
    }

    class GetBitmap extends AsyncTask<String, Integer, Bitmap> {
        ImageView iv;

        GetBitmap(ImageView iv) {
            this.iv = iv;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return bitmap = BitmapFactory.decodeFile(params[0]);
        }

        protected void onPostExecute(Bitmap bitmap) {
            iv.setImageBitmap(bitmap);
            beautyLayout.removeView(progressBar);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ppath = bundle.getString("uri");
        pname = bundle.getString("name");

        setContentView(R.layout.activity_set_beauty);
        beautyLayout = (RelativeLayout) findViewById(R.id.beautyLayout);
        inputtext = (TextView) findViewById(R.id.inputtext);
        sourceImage = (ImageView) findViewById(R.id.iv);
        icon = (ImageView) findViewById(R.id.piv);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        Button save = (Button) findViewById(R.id.bbutton1);
        Button mohu = (Button) findViewById(R.id.bbutton2);
        Button color = (Button) findViewById(R.id.bbutton3);
        Button share = (Button) findViewById(R.id.bbutton4);
        Button text = (Button) findViewById(R.id.bbutton5);
        Button bp = (Button) findViewById(R.id.bp);
        graffitiPaint = new Paint();
        graffitiPaint.setStrokeWidth(5);
        graffitiPaint.setColor(Color.GREEN);

        GetBitmap getBitmap = new GetBitmap(sourceImage);
        getBitmap.execute(ppath);

        save.setOnClickListener(this);
        mohu.setOnClickListener(this);
        bp.setOnClickListener(this);
        share.setOnClickListener(this);
        color.setOnClickListener(this);
        text.setOnClickListener(this);

        sourceImage.setOnTouchListener(graffiti);
        icon.setOnTouchListener(moveIcon);
        inputtext.setOnTouchListener(moveText);

    }

    public void changeGraffitiPaint(){
        final LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.color, null);
        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekbar);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.l);
        final TextView colorText = new TextView(ProcessPictureActivity.this);
        ColorPickerView colorPick = new ColorPickerView(ProcessPictureActivity.this, Color.parseColor("#FFFFFF"), 2, colorText);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        colorPick.setLayoutParams(lp);

        layout.addView(colorPick);
        layout.addView(colorText);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ProcessPictureActivity.this, R.style.mDialog))
                .setTitle("编辑")
                .setIcon(R.drawable.main)
                .setView(view)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                graffitiPaint.setColor(Integer.parseInt(colorText.getText().toString()));
                                graffitiPaint.setAntiAlias(true);
                            }
                        });
        builder.create();

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    graffitiPaint.setStrokeWidth(progress);
                } catch (Exception e) {
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    graffitiPaint.setStrokeWidth(seekbar.getProgress());
                } catch (Exception e) {
                }
            }
        });
        builder.show();
    }

    public void addPicture(){
        final RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.addpicture, null);
        final GridView p = (GridView) view.findViewById(R.id.addp);
        p.setAdapter(new BaseA());

        pbuilder = new AlertDialog.Builder(new ContextThemeWrapper(ProcessPictureActivity.this, R.style.mDialog))
                .setTitle("添加图片")
                .setIcon(R.drawable.main)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        pbuilder.create().show();
    }

    public void addText(){
        final LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.text, null);
        SeekBar size = (SeekBar) view.findViewById(R.id.size);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.textlauout);
        final TextView colorText = new TextView(ProcessPictureActivity.this);
        ColorPickerView colorPick = new ColorPickerView(ProcessPictureActivity.this, Color.parseColor("#FFFFFF"), 2, colorText);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        colorPick.setLayoutParams(lp);
        layout.addView(colorPick);
        layout.addView(colorText);

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ProcessPictureActivity.this, R.style.mDialog))
                .setTitle("编辑")
                .setIcon(R.drawable.main)
                .setView(view)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText text = (EditText) view.findViewById(R.id.text);
                                inputtext.setText(text.getText().toString());
                                textPaint.setColor(Integer.parseInt(colorText.getText().toString()));
                                inputtext.setTextColor(Integer.parseInt(colorText.getText().toString()));
                            }
                        });
        builder.create()
                .show();

        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    inputtext.setTextSize(progress);
                } catch (Exception e) {
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public View.OnTouchListener moveText = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int lastX = 0, lastY = 0;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;

                    int left = v.getLeft() + dx;
                    int top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy;
                    // 设置不能出界
                    if (left < 0) {
                        left = 0;
                        right = left + v.getWidth();
                    }
                    if (right > MyActivity.screenWidth) {
                        right = MyActivity.screenWidth;
                        left = right - v.getWidth();
                    }
                    if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }
                    if (bottom > MyActivity.screenHeight) {
                        bottom = MyActivity.screenHeight;
                        top = bottom - v.getHeight();
                    }
                    // v.layout(left, top, right, bottom);
                    v.layout((int) event.getRawX() - v.getWidth() / 2, (int) event.getRawY() - v.getHeight() / 2, (int) event.getRawX() + v.getWidth() / 2, (int) event.getRawY() + v.getHeight() / 2);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    textPaint.setTextAlign(Paint.Align.CENTER);
                    sourceImage.setDrawingCacheEnabled(true);
                    bitmap = Bitmap.createBitmap(sourceImage.getDrawingCache());
                    sourceImage.setDrawingCacheEnabled(false);

                    baseBitmap = Bitmap.createBitmap(MyActivity.screenWidth, MyActivity.screenHeight, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawBitmap(bitmap, 0, 0, textPaint);
                    textPaint.setAntiAlias(true);
                    textPaint.setTextSize(inputtext.getTextSize());

                    canvas.drawText(inputtext.getText().toString(), event.getRawX(), event.getRawY(), textPaint);
                    sourceImage.setImageBitmap(baseBitmap);
                    bitmap = baseBitmap;
                    baseBitmap = null;
                    inputtext.setText("");
                    RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    inputtext.setLayoutParams(rParams);
                    break;
            }
            return true;
        }

    };

    public View.OnTouchListener moveIcon = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    v.layout((int) event.getRawX() - v.getWidth() / 2, (int) event.getRawY() - v.getHeight() / 2, (int) event.getRawX() + v.getWidth() / 2, (int) event.getRawY() + v.getHeight() / 2);
                    break;
                case MotionEvent.ACTION_UP:
                    final Paint paint1 = new Paint();
                    paint1.setTextAlign(Paint.Align.CENTER);
                    sourceImage.setDrawingCacheEnabled(true);
                    bitmap = Bitmap.createBitmap(sourceImage.getDrawingCache());
                    sourceImage.setDrawingCacheEnabled(false);
                    baseBitmap = Bitmap.createBitmap(MyActivity.screenWidth, MyActivity.screenHeight, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawBitmap(bitmap, 0, 0, paint1);
                    paint1.setAntiAlias(true);
                    paint1.setTextSize(inputtext.getTextSize());
//                    sourceImage.setDrawingCacheEnabled(true);
//                    bitmap = Bitmap.createBitmap(sourceImage.getDrawingCache());
//                    sourceImage.setDrawingCacheEnabled(false);

                    icon.setDrawingCacheEnabled(true);
                    Bitmap pbitmap = Bitmap.createBitmap(icon.getDrawingCache());
                    icon.setDrawingCacheEnabled(false);
                    canvas.drawBitmap(pbitmap, event.getRawX() - v.getWidth() / 2, event.getRawY() - v.getHeight() / 2, paint1);
                    sourceImage.setImageBitmap(baseBitmap);
                    bitmap = baseBitmap;
                    baseBitmap = null;

                    icon.setImageBitmap(null);
                    RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    icon.setLayoutParams(rParams);
                    break;
            }
            return true;
        }
    };

    public View.OnTouchListener graffiti = new View.OnTouchListener() {
        float startX;
        float startY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (baseBitmap == null) {
                float x = (float) MyActivity.screenWidth / (float) MyActivity.screenHeight;
                if (bitmap.getWidth() > x * bitmap.getHeight()) {
                    ma.setScale((float) MyActivity.screenWidth / (float) bitmap.getWidth(), (float) MyActivity.screenWidth / (float) bitmap.getWidth());
                } else {
                    ma.setScale((float) MyActivity.screenHeight / (float) bitmap.getHeight(), (float) MyActivity.screenHeight / (float) bitmap.getHeight());
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), ma, true);
                baseBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(baseBitmap);
                canvas.translate(0, -(MyActivity.screenHeight - bitmap.getHeight()) / 2);
                canvas.drawBitmap(bitmap, 0, (MyActivity.screenHeight - bitmap.getHeight()) / 2, graffitiPaint);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float stopX = event.getX();
                    float stopY = event.getY();
                    canvas.drawLine(startX, startY, stopX, stopY, graffitiPaint);
                    startX = event.getX();
                    startY = event.getY();
                    sourceImage.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }

            return true;


        }
    };
    //  整体模糊
    public Bitmap blurBitmap(Bitmap bitmap) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(getApplicationContext());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        blurScript.setRadius(25.f);
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        allOut.copyTo(outBitmap);
        bitmap.recycle();
        rs.destroy();
        return outBitmap;
    }


    public static void saveBitmapToSDCard(Bitmap bitmap, String imagename) {
        File sdf = Environment.getExternalStorageDirectory();
        //File wallpaper=new File(sdf.getCanonicalPath()+"wallpaper");
        File wallpaper = new File("/mnt/sdcard/wallpaper");
        if (!wallpaper.exists()) {
            wallpaper.mkdirs();
        }
        File f = new File(wallpaper.getPath() + "/", imagename + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class BaseA extends BaseAdapter {
        int[] id;
        public BaseA() {}

        @Override
        public int getCount() {
            id = new int[]{
                    R.drawable.a1,
                    R.drawable.a10,
                    R.drawable.a11,
                    R.drawable.a12,
                    R.drawable.a13,
                    R.drawable.a14,
                    R.drawable.a15,
                    R.drawable.a16,
                    R.drawable.a17,
                    R.drawable.a18,
                    R.drawable.a19,
                    R.drawable.a2,
                    R.drawable.a20,
                    R.drawable.a3,
                    R.drawable.a4,
                    R.drawable.a5,
                    R.drawable.a6,
                    R.drawable.a7,
                    R.drawable.a8,
                    R.drawable.a9,
            };
            return id.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView im = new ImageView(ProcessPictureActivity.this);
            im.setImageResource(id[position]);
            im.setMaxWidth(100);
            im.setMaxHeight(100);
            im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon.setImageResource(id[position]);
                }
            });
            return im;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_beauty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
