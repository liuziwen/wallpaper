package com.example.lzw.wallpaper.Net;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.lzw.wallpaper.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class NetPicture extends Activity {
    ListView gv;
    String[] title, linkimg;
    static String[] url;
    Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                gv.setAdapter(new MyNetBaseAdapter(NetPicture.this));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_picture);
        gv = (ListView) findViewById(R.id.nlv);
        ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            getjsoup.start();
        } else {
            Toast.makeText(NetPicture.this, "无互联网连接", Toast.LENGTH_SHORT).show();
        }
    }

    public static class MyHolder {
        public ImageView niv;
        public TextView ntv;
    }

    public class MyNetBaseAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater = null;
        private Context mContext = null;
        private RequestQueue mQueue;
        private ImageLoader mImageLoader;

        public MyNetBaseAdapter(Context con) {
            mContext = con;
            mLayoutInflater = LayoutInflater.from(con);
            mQueue = Volley.newRequestQueue(con);
            mImageLoader = new ImageLoader(mQueue, new BitmapCache());

        }

        @Override
        public int getCount() {
            return url.length;
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
            MyHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new MyHolder();
                convertView = mLayoutInflater.inflate(R.layout.netcell, null);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent in = new Intent(NetPicture.this, NetImage.class);
                        in.putExtra("url", url[position]);
                        startActivity(in);
                    }
                });

                viewHolder.niv = (ImageView) convertView.findViewById(R.id.netcellp);
                viewHolder.ntv = (TextView) convertView.findViewById(R.id.netcellt);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyHolder) convertView.getTag();
            }

            ImageLoader.ImageListener listener = ImageLoader.getImageListener(viewHolder.niv, android.R.drawable.ic_menu_rotate, android.R.drawable.ic_delete);
            mImageLoader.get(linkimg[position], listener);
            viewHolder.ntv.setText(title[position]);


            return convertView;
        }
    }


    public class BitmapCache implements ImageLoader.ImageCache {
        private LruCache<String, Bitmap> mCache;

        public BitmapCache() {
            int maxSize = 10 * 1024 * 1024;
            mCache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }
    }

    Thread getjsoup = new Thread() {
        public void run() {
            Looper.prepare();
            Document doc = null;
            try {
                doc = Jsoup.connect("http://sj.zol.com.cn/samsung/galaxys4_bizhi/").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements links = doc.getElementsByClass("photo-list-padding");
            System.out.println(links);
            title = new String[links.size()];
            url = new String[links.size()];
            linkimg = new String[links.size()];

            int i = 0;
            for (Element link : links) {
                title[i] = link.text();
                url[i] = link.select("a").attr("href");
                linkimg[i] = link.select("img").attr("src");
                i++;
            }

            mhandler.sendEmptyMessage(0x123);
            Looper.loop();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.net_picture, menu);
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
