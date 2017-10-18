package com.johyun.videoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by JH on 2017. 10. 17..
 */

public class CustomVideoView extends RelativeLayout {

    private static final String TAG = CustomVideoView.class.getSimpleName();
    private Context context;

    public CustomVideoView(Context context) {
        super(context);
        initView(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.custom_video, this, true);
    }
}
