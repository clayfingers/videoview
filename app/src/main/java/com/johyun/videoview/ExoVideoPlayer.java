package com.johyun.videoview;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by JH on 2017. 10. 19..
 */

public class ExoVideoPlayer extends RelativeLayout implements ExoPlayer.EventListener {

    private final static String TAG = ExoVideoPlayer.class.getSimpleName();
    private Context context;

    private RelativeLayout rl_exo_player_wrapper;
    private ImageView iv_exo_player_full_screen;
    private ImageView iv_exo_player_share;
    private ImageView iv_custom_video_sound;
    private ProgressBar pb_exo_player_progressBar;

    private SimpleExoPlayer exoPlayer;
    private SimpleExoPlayerView playerView;

    private boolean isPlayerPlaying;

    private String tempUrl = "https://www.rmp-streaming.com/media/bbb-360p.mp4";


    public ExoVideoPlayer(Context context) {
        super(context);
        initPlayer(context);
    }

    public ExoVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPlayer(context);
    }

    public ExoVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPlayer(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExoVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPlayer(context);
    }

    private void initUi() {
        rl_exo_player_wrapper = findViewById(R.id.rl_exo_player_wrapper);
        pb_exo_player_progressBar = findViewById(R.id.pb_exo_player_progressBar);
        iv_exo_player_full_screen = findViewById(R.id.iv_exo_player_full_screen);
        iv_exo_player_share = findViewById(R.id.iv_exo_player_share);
        iv_custom_video_sound = findViewById(R.id.iv_custom_video_sound);
    }

    private void initPlayer(final Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_exo_player, this, true);
        initUi();
        setVideoViewHeight();

        pb_exo_player_progressBar.setVisibility(VISIBLE);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayer"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(tempUrl), dataSourceFactory, extractorsFactory, null, null);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        exoPlayer.prepare(videoSource);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(false);

        playerView = findViewById(R.id.exo_player);
        playerView.setPlayer(exoPlayer);
        playerView.setKeepScreenOn(true);
        playerView.requestFocus();
        playerView.setUseArtwork(true);

        createThumbnail();
        initFullscreen();

        //todo stop video when video view out of screen

        // 풀스크린 클릭리스너
        iv_exo_player_full_screen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick FULL SCREEN");
                if (!isExoPlayerFullscreen)
                    openFullscreen();
                else
                    closeFullscreen();
            }
        });

        // 공유 클릭리스너
        iv_exo_player_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick SHARE");
                //todo share logic
            }
        });

        // 사운드 클릭리스너
        iv_custom_video_sound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick SOUND");
                if (exoPlayer.getVolume() == 0) {
                    exoPlayer.setVolume(1);
                    view.setAlpha(1.0f);
                } else {
                    exoPlayer.setVolume(0);
                    view.setAlpha(0.6f);
                }
            }
        });
    }

    //todo bitmap 적용 안됨
    private void createThumbnail() {
        new CreateExoThumbnailAsyncTask(new CreateExoThumbnailAsyncTask.CreateExoThumbnailCompleteListener() {
            @Override
            public void onCreateComplete(Bitmap bitmap) {
                Log.d(TAG, "createThumbnail bitmap = " + bitmap);
                playerView.setDefaultArtwork(bitmap);
            }
        }).execute(tempUrl);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.d(TAG, "onTimelineChanged timeline.getPeriodCount() = " + timeline.getPeriodCount());
        Log.d(TAG, "onTimelineChanged manifest = " + manifest);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.d(TAG, "onTracksChanged trackGroups = " + trackGroups);
        Log.d(TAG, "onTracksChanged trackSelections = " + trackSelections);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d(TAG, "onLoadingChanged = " + isLoading);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                Log.d(TAG, "onPlayerStateChanged STATE_BUFFERING");
                pb_exo_player_progressBar.setVisibility(VISIBLE);
                break;
            case Player.STATE_IDLE:
                Log.d(TAG, "onPlayerStateChanged STATE_IDLE");
                pb_exo_player_progressBar.setVisibility(VISIBLE);
                break;
            case Player.STATE_READY:
                Log.d(TAG, "onPlayerStateChanged STATE_READY");
                pb_exo_player_progressBar.setVisibility(GONE);
                break;
            case Player.STATE_ENDED:
                Log.d(TAG, "onPlayerStateChanged STATE_ENDED");
                // 처음으로 돌아가서 정지
                exoPlayer.seekTo(0);
                exoPlayer.setPlayWhenReady(false);
                break;
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.d(TAG, "onPlaybackParametersChanged playbackParameters = " + playbackParameters);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.d(TAG, "onRepeatModeChanged repeatMode = " + repeatMode);
    }

    // exoPlayer 해제
    public void releaseExoPlayer(){
        if(exoPlayer != null) {
            exoPlayer.release();
        }
        exoPlayer = null;
    }

    private Dialog fullScreenDialog;
    private boolean isExoPlayerFullscreen = false;

    private void initFullscreen() {

        fullScreenDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (isExoPlayerFullscreen)
                    closeFullscreen();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreen() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        fullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        isExoPlayerFullscreen = true;
        fullScreenDialog.show();
        //todo app 이 landscape 지원 하는 지 확인, 지원하지 않는다면 full screen 의 영상이 가로모드인지 확인
//        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void closeFullscreen() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        rl_exo_player_wrapper.addView(playerView);
        isExoPlayerFullscreen = false;
        fullScreenDialog.dismiss();
//        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.d(TAG, "onPlayerError = " + error.getMessage());
    }

    @Override
    public void onPositionDiscontinuity() {
        // 스트리밍 잘 안될때
        Log.d(TAG, "onPositionDiscontinuity");
    }

    private void setVideoViewHeight() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_exo_player_wrapper.getLayoutParams();
        layoutParams.height = dpToPx((getDeviceWidth() * 9) / 16);
        rl_exo_player_wrapper.setLayoutParams(layoutParams);

        Log.d(TAG, "getDeviceWidth() = " + getDeviceWidth());
        Log.d(TAG, "(getDeviceWidth() * 9) / 16 = " + (getDeviceWidth() * 9) / 16);
    }

    private int getDeviceWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        return Math.round(displayMetrics.widthPixels / displayMetrics.density);
    }

    private int dpToPx(int dp) {
        DisplayMetrics xDisplayMetrics = Resources.getSystem().getDisplayMetrics();
        float density = xDisplayMetrics.density;

        return (int) (dp * density);
    }
}
