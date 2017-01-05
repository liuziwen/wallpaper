package com.example.lzw.wallpaper.SDFileExplorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lzw.wallpaper.picture.BitmapUtilities;
import com.example.lzw.wallpaper.R;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by lzw on 2015/6/6.
 */
public class MyBaseAdapter extends BaseAdapter {

    private static Context mContext = null;
    private LayoutInflater mLayoutInflater = null;
    private List<Map<String, Object>> mList = null;

    private int width = 100;//每个Item的宽度,可以根据实际情况修改
    private int height = 100;//每个Item的高度,可以根据实际情况修改


    public static class MyGridViewHolder {
        public ImageView imageview_thumbnail;
        public TextView textview_test;
    }

    public MyBaseAdapter(Context context, List<Map<String, Object>> mList, Bitmap bit) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mList = mList;
        mLayoutInflater = LayoutInflater.from(context);
    }


    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Map map = mList.get(position);
        String url = (String) map.get("uri");
        String name = (String) map.get("fileName");

        MyGridViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new MyGridViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.cell2, null);
            viewHolder.imageview_thumbnail = (ImageView) convertView.findViewById(R.id.imagecell2);
            viewHolder.textview_test = (TextView) convertView.findViewById(R.id.textcell2);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (MyGridViewHolder) convertView.getTag();
        }

        //首先我们先通过cancelPotentialLoad方法去判断imageview是否有线程正在为它加载图片资源，
        //如果有现在正在加载，那么判断加载的这个图片资源（url）是否和现在的图片资源一样，不一样则取消之前的线程（之前的下载线程作废）。
        //见下面cancelPotentialLoad方法代码
        if (url == null) {
            viewHolder.imageview_thumbnail.setImageResource(R.drawable.folder);
        } else {
            if (cancelPotentialLoad(url, viewHolder.imageview_thumbnail)) {
                AsyncLoadImageTask task = new AsyncLoadImageTask(viewHolder.imageview_thumbnail);
                LoadedDrawable loadedDrawable = new LoadedDrawable(task);
                viewHolder.imageview_thumbnail.setImageDrawable(loadedDrawable);
                task.execute(position);
            }
        }
        viewHolder.textview_test.setText(name);
        return convertView;
    }


    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        bitmap = SDFileExplorer.gridviewBitmapCaches.get(url);
        if (bitmap != null) {
            System.out.println(url);
            return bitmap;
        }

        bitmap = BitmapUtilities.getBitmapThumbnail(url, width, height);
        return bitmap;
    }

    //加载图片的异步任务
    private class AsyncLoadImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private String url = null;
        private final WeakReference<ImageView> imageViewReference;

        public AsyncLoadImageTask(ImageView imageview) {
            super();
            // TODO Auto-generated constructor stub
            imageViewReference = new WeakReference<ImageView>(imageview);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            Bitmap bitmap = null;
            Map map = mList.get(params[0]);
            this.url = (String) map.get("uri");
            bitmap = getBitmapFromUrl(url);
            SDFileExplorer.gridviewBitmapCaches.put((String) map.get("uri"), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {
            // TODO Auto-generated method stub
            if (isCancelled()) {
                resultBitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageview = imageViewReference.get();
                AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
                if (this == loadImageTask) {
                    imageview.setImageBitmap(resultBitmap);
                    imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
            super.onPostExecute(resultBitmap);
        }
    }


    private boolean cancelPotentialLoad(String url, ImageView imageview) {
        AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);

        if (loadImageTask != null) {
            String bitmapUrl = loadImageTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                loadImageTask.cancel(true);
            } else {
                // 相同的url已经在加载中.
                return false;
            }
        }
        return true;

    }

    //当 loadImageTask.cancel(true)被执行的时候，则AsyncLoadImageTask 就会被取消，
    //当AsyncLoadImageTask 任务执行到onPostExecute的时候，如果这个任务加载到了图片，
    //它也会把这个bitmap设为null了。
    //getAsyncLoadImageTask代码如下：
    private AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview) {
        if (imageview != null) {
            Drawable drawable = imageview.getDrawable();
            if (drawable instanceof LoadedDrawable) {
                LoadedDrawable loadedDrawable = (LoadedDrawable) drawable;
                return loadedDrawable.getLoadImageTask();
            }
        }
        return null;
    }

    //该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
    public static class LoadedDrawable extends BitmapDrawable {
        private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

        public LoadedDrawable(AsyncLoadImageTask loadImageTask) {
            super(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher)
            );
            loadImageTaskReference =
                    new WeakReference<AsyncLoadImageTask>(loadImageTask);
        }

        public AsyncLoadImageTask getLoadImageTask() {
            return loadImageTaskReference.get();
        }

    }
}