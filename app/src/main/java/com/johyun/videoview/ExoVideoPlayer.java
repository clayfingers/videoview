package com.johyun.videoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
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

import static com.johyun.videoview.R.id.exo_player;

/**
 * Created by JH on 2017. 10. 19..
 */

public class ExoVideoPlayer extends RelativeLayout implements ExoPlayer.EventListener {

    private final static String TAG = ExoVideoPlayer.class.getSimpleName();
    private Context context;

    private RelativeLayout rl_exo_player_wrapper, rl_exo_player_controller_wrapper;
    private ImageView iv_exo_player_full_screen, iv_exo_player_share;
    private ProgressBar pb_exo_player_progressBar;

    private SimpleExoPlayer exoPlayer;
    private SimpleExoPlayerView playerView;
//    private String tempUrl = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4";
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
        rl_exo_player_controller_wrapper = findViewById(R.id.rl_exo_player_controller_wrapper);
        iv_exo_player_full_screen = findViewById(R.id.iv_exo_player_full_screen);
        iv_exo_player_share = findViewById(R.id.iv_exo_player_share);
    }

    private void initPlayer(final Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_exo_player, this, true);
        initUi();
        setVideoViewHeight();

        pb_exo_player_progressBar.setVisibility(VISIBLE);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        playerView = findViewById(exo_player);
        playerView.setPlayer(exoPlayer);
        playerView.setKeepScreenOn(true);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayer"));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(tempUrl), dataSourceFactory, extractorsFactory, null, null);

        exoPlayer.prepare(videoSource);
        exoPlayer.addListener(this);
        playerView.requestFocus();
        exoPlayer.setPlayWhenReady(false);
        playerView.setUseController(false);// 콘트롤러 사용 못함

        //todo 콘트롤러 UI & 로직 추가
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
            case ExoPlayer.STATE_BUFFERING:
                Log.d(TAG, "onPlayerStateChanged STATE_BUFFERING");
                pb_exo_player_progressBar.setVisibility(VISIBLE);
                break;
            case ExoPlayer.STATE_IDLE:
                Log.d(TAG, "onPlayerStateChanged STATE_IDLE");
                pb_exo_player_progressBar.setVisibility(VISIBLE);
                break;
            case ExoPlayer.STATE_READY:
                Log.d(TAG, "onPlayerStateChanged STATE_READY");
                pb_exo_player_progressBar.setVisibility(GONE);
                break;
            case ExoPlayer.STATE_ENDED:
                Log.d(TAG, "onPlayerStateChanged STATE_ENDED");
                // 처음으로 돌아가서 정지
                exoPlayer.seekTo(0);
                exoPlayer.setPlayWhenReady(false);
                break;
        }
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
