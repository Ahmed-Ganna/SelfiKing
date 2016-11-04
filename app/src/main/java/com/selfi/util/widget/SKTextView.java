package com.selfi.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.selfi.android.manager.FontManager;


public class SKTextView extends TextView {

    public SKTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SKTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SKTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTypeface(FontManager.get(FontManager.MOGA_FONT_PATH,getContext()));
    }

}
