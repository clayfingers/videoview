package com.johyun.videoview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class PortraitVideoActivity extends AppCompatActivity {

    private final static String TAG = PortraitVideoActivity.class.getSimpleName();

    private String videoPath = "https://www.rmp-streaming.com/media/bbb-360p.mp4";
    private long playPosition = 0;
    private boolean isPlaying = false;
    private RelativeLayout rl_exo_player_wrapper;

    //todo 스크롤로 인해 화면 밖으로 나갔을 때 자동으로 영상이 정지 되어야 합니다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_portrait);
        rl_exo_player_wrapper = (RelativeLayout)findViewById(R.id.rl_exo_player_wrapper);

        setVideoViewHeight();

        videoPath = getIntent().getStringExtra("video_path");

        //todo temp
        if(videoPath == null) {
            videoPath = "https://www.rmp-streaming.com/media/bbb-360p.mp4";
        }

        // 풀스크린 클릭리스너
        findViewById(R.id.iv_exo_player_full_screen).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "onClick FULL SCREEN");
                Intent intent = new Intent(getApplicationContext(), FullscreenVideoActivity.class);
                intent.putExtra("video_path", videoPath);
                intent.putExtra("position", ExoPlayerVideoHandler.getInstance().getCurrentPosition());
                intent.putExtra("is_playing", ExoPlayerVideoHandler.getInstance().isPlaying());
                startActivityForResult(intent, 10000);
            }
        });

        // 공유 클릭리스너
        findViewById(R.id.iv_exo_player_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick SHARE");
                //todo share logic
            }
        });

        // 사운드 클릭리스너
        findViewById(R.id.iv_custom_video_sound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick SOUND");
                ExoPlayerVideoHandler.getInstance().toggleVolume(view);
            }
        });

        // 최초 한번만 썸내일 적용
        new CreateExoThumbnailAsyncTask(new CreateExoThumbnailAsyncTask.CreateExoThumbnailCompleteListener() {
            @Override
            public void onCreateComplete(Bitmap bitmap) {
                Log.d(TAG, "createThumbnail bitmap = " + bitmap);
                // todo 썸내일 적용
                rl_exo_player_wrapper.setBackground(new BitmapDrawable(getResources(), bitmap));
            }
        }).execute(videoPath);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(videoPath != null){
            ExoPlayerVideoHandler.getInstance().prepareExoPlayerForUri(getApplicationContext(), Uri.parse(videoPath), rl_exo_player_wrapper);
            ExoPlayerVideoHandler.getInstance().goToForeground(playPosition, isPlaying);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        switch (requestCode) {
            case 10000:
                if (resultCode == 10000) {
                    playPosition = data.getLongExtra("position", 0);
                    isPlaying = data.getBooleanExtra("is_playing", false);

                    Log.d(TAG, "videoPath = " + videoPath);
                    Log.d(TAG, "playPosition = " + playPosition);
                    Log.d(TAG, "isPlaying = " + isPlaying);
                }
                break;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
        ExoPlayerVideoHandler.getInstance().goToBackground();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ExoPlayerVideoHandler.getInstance().releaseVideoPlayer();
    }

    private void setVideoViewHeight() {
        RelativeLayout rl_exo_player_wrapper = (RelativeLayout) findViewById(R.id.rl_exo_player_wrapper);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_exo_player_wrapper.getLayoutParams();
        layoutParams.height = dpToPx((getDeviceWidth() * 9) / 16);
        rl_exo_player_wrapper.setLayoutParams(layoutParams);

        Log.d(TAG, "getDeviceWidth() = " + getDeviceWidth());
        Log.d(TAG, "(getDeviceWidth() * 9) / 16 = " + (getDeviceWidth() * 9) / 16);
    }

    private int getDeviceWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        return Math.round(displayMetrics.widthPixels / displayMetrics.density);
    }

    private int dpToPx(int dp) {
        DisplayMetrics xDisplayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = xDisplayMetrics.density;

        return (int) (dp * density);
    }
}
