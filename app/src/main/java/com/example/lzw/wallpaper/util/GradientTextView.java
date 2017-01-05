package com.example.lzw.wallpaper.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by liuziwen on 17/1/5.
 */

public class GradientTextView extends TextView {
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;
    private Paint paint;
    private int filterColor = Color.parseColor("#DD000000");
    public Rect rect;
    public int gradientOrientation;
    private Bitmap bitmap;

    private ValueAnimator animator;
    ;

    public GradientTextView(Context context) {
        super(context);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        rect = new Rect();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(new PorterDuffColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP));
    }

    public void setFilterColor(int color) {
        if (animator == null || !animator.isRunning()) {
            filterColor = color;
        }
    }

    public void setGradientOrientation(int orientation) {
        if (animator == null || !animator.isRunning()) {
            gradientOrientation = orientation;
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (gradientOrientation == HORIZONTAL) {
            rect.set(0, 0, 0, getMeasuredHeight());
        } else {
            rect.set(0, 0, getMeasuredWidth(), 0);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, rect, rect, paint);
        }
    }

    public void startAnimator() {
        if (animator != null && animator.isRunning()) {
            return;
        }
        buildDrawingCache();
        bitmap = getDrawingCache();
        if (gradientOrientation == HORIZONTAL) {
            animator = ValueAnimator.ofInt(0, getMeasuredWidth());
            animator.setDuration(3000);
        } else {
            animator = ValueAnimator.ofInt(0, getMeasuredHeight());
            animator.setDuration(2000);
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (gradientOrientation == HORIZONTAL) {
                    rect.set(0, 0, value, getMeasuredHeight());
                } else {
                    rect.set(0, 0, getMeasuredWidth(), value);
                }
                invalidate();
            }
        });

        animator.start();
    }
}
