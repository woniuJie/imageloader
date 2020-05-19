package com.culiu.core.widget.zoomable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.culiu.core.imageloader.Utils;
import com.facebook.drawee.drawable.DrawableUtils;
import com.facebook.drawee.drawable.ProgressBarDrawable;

/**
 * Created by wangsai on 2015/12/21.
 */
public class CircleProgressDrawable extends ProgressBarDrawable {

    private int DEFAULT_PROGRESS_WIDTH = 4;

    private int DEFAULT_BACKGROUND_WIDTH = 2;

    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final int DEFAULT_PROGRESS_COLOR = Color.WHITE;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mRadius;

    private int mPadding;

    private RectF mCircleRect;

    private int mBgWidth = DEFAULT_BACKGROUND_WIDTH;

    public CircleProgressDrawable(Context context) {
        mRadius = Utils.getScreenWidth(context) / 15;
        mCircleRect = new RectF(0, 0, mRadius * 2, mRadius * 2);

        DEFAULT_PROGRESS_WIDTH = Utils.dip2px(context, 2);
        DEFAULT_BACKGROUND_WIDTH = Utils.dip2px(context, 1);

        setBarWidth(DEFAULT_PROGRESS_WIDTH);
        setColor(DEFAULT_PROGRESS_COLOR);
        setBackgroundColor(DEFAULT_BACKGROUND_COLOR);

        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setRadius(int radius) {
        mRadius = radius;
        mCircleRect = new RectF(0, 0, mRadius * 2, mRadius * 2);
        invalidateSelf();
    }

    @Override
    public void setPadding(int padding) {
        super.setPadding(padding);
        mPadding = padding;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return DrawableUtils.getOpacityFromColor(mPaint.getColor());
    }

    @Override
    public void draw(Canvas canvas) {
        if (getHideWhenZero() && getLevel() == 0) {
            return;
        }
        drawBar(canvas, 10000, getBackgroundColor(), mBgWidth);
        drawBar(canvas, getLevel(), getColor(), getBarWidth());
    }

    private void drawBar(Canvas canvas, int level, int color, int width) {
        Rect bounds = getBounds();

        mPaint.setColor(color);
        mPaint.setStrokeWidth(width);
        RectF circleBounds = new RectF(mCircleRect);
        circleBounds.offset(bounds.centerX() - mCircleRect.centerX(), bounds.centerY() - mCircleRect.centerY());
        circleBounds.inset(mPadding, mPadding);
        canvas.drawArc(circleBounds, 0, level / 10000f * 360, false, mPaint);
    }
}