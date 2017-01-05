package com.example.lzw.wallpaper.PictureSet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.lzw.wallpaper.MyActivity;

public class BitmapUtilities {

    public BitmapUtilities() {
        // TODO Auto-generated constructor stub
    }

    public static Bitmap getBitmapThumbnail(String path, int width, int height) {
        Bitmap bm;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        //这个isjustdecodebounds很重要
        opt.inJustDecodeBounds = true;
        bm = BitmapFactory.decodeFile(path, opt);

        //获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        //获取屏的宽度和高度
        int screenWidth = 100;
        int screenHeight = 100;
        //isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opt.inSampleSize = 1;
        //根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > screenWidth)
                opt.inSampleSize = picWidth / screenWidth;
        } else {
            if (picHeight > screenHeight)
                opt.inSampleSize = picHeight / screenHeight;
        }

        //这次再真正地生成一个有像素的，经过缩放了的bitmap
        opt.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, opt);
        return bm;
    }

    public Bitmap getBitmapThumbnail(Bitmap bmp, int width, int height) {
        Bitmap bitmap = null;
        if (bmp != null) {
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();
            if (width != 0 && height != 0) {
                Matrix matrix = new Matrix();
                float scaleWidth = ((float) width / bmpWidth);
                float scaleHeight = ((float) height / bmpHeight);
                matrix.postScale(scaleWidth, scaleHeight);
                bitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
            } else {
                bitmap = bmp;
            }
        }
        return bitmap;
    }

    //NetBaseAdapter
    public static Bitmap returnSquareBitmap(String str) {
        Bitmap bm;
        //把加载的图片缩小与剪切
        BitmapFactory.Options opt = new BitmapFactory.Options();
        //这个isjustdecodebounds很重要
        opt.inJustDecodeBounds = true;
        bm = BitmapFactory.decodeFile(str, opt);
        //获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        int pWidth = (MyActivity.screenWidth) / 3;
        int pHeight = (MyActivity.screenHeight) / 3;

        //isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opt.inSampleSize = 1;
        //根据屏的大小和图片大小计算出缩放比例
        if (picWidth > picHeight) {
            if (picWidth > pWidth)
                opt.inSampleSize = picWidth / pWidth;
        } else {
            if (picHeight > pHeight)

                opt.inSampleSize = picHeight / pHeight;
        }

        //这次再真正地生成一个有像素的，经过缩放了的bitmap
        opt.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(str, opt);

        Bitmap bm2;
        if (bm.getWidth() >= bm.getHeight()) {
            bm2 = Bitmap.createBitmap(bm, (bm.getWidth() - bm.getHeight()) / 2, 0, bm.getHeight(), bm.getHeight());
        } else
            bm2 = Bitmap.createBitmap(bm, 0, (bm.getHeight() - bm.getWidth()) / 2, bm.getWidth(), bm.getWidth());
        return bm2;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
