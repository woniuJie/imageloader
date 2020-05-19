package com.culiu.core.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by wangsai on 2015/12/3.
 */
public class CustomImageView extends SimpleDraweeView {

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
