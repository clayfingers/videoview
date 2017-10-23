package com.johyun.videoview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FullscreenVideoActivity extends AppCompatActivity {

    private final static String TAG = FullscreenVideoActivity.class.getSimpleName();

    private String videoPath;
    private long playPosition = 0;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        // 아이콘 바꿔줌
        ImageView iv_exo_player_full_screen = (ImageView)findViewById(R.id.iv_exo_player_full_screen);
        iv_exo_player_full_screen.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.btn_shrink));

        // 풀스크린 클릭리스너
        iv_exo_player_full_screen.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("video_path", videoPath);
                        intent.putExtra("position", ExoPlayerVideoHandler.getInstance().getCurrentPosition());
                        intent.putExtra("is_playing", ExoPlayerVideoHandler.getInstance().isPlaying());
                        setResult(10000, intent);
                        finish();
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
    }

    @Override
    protected void onResume(){
        super.onResume();

        videoPath = getIntent().getStringExtra("video_path");
        playPosition = getIntent().getLongExtra("position", 0);
        isPlaying = getIntent().getBooleanExtra("is_playing", false);

        Log.d(TAG, "videoPath = " + videoPath);
        Log.d(TAG, "playPosition = " + playPosition);
        Log.d(TAG, "isPlaying = " + isPlaying);

        if(videoPath != null) {
            ExoPlayerVideoHandler.getInstance().prepareExoPlayerForUri(getApplicationContext(), Uri.parse(videoPath),
                    (RelativeLayout)findViewById(R.id.rl_exo_player_wrapper));
            ExoPlayerVideoHandler.getInstance().goToForeground(playPosition, isPlaying);
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onPause(){
        super.onPause();
        ExoPlayerVideoHandler.getInstance().goToBackground();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
