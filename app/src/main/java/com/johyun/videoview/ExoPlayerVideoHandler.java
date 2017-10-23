package com.johyun.videoview;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlaybackException;
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
 * Created by JH on 2017. 10. 23..
 */

public class ExoPlayerVideoHandler implements Player.EventListener {

    private final static String TAG = ExoPlayerVideoHandler.class.getSimpleName();

    private static ExoPlayerVideoHandler instance;
    private ExoPlayerVideoHandler() {}

    public static ExoPlayerVideoHandler getInstance() {
        if (instance == null) {
            instance = new ExoPlayerVideoHandler();
        }
        return instance;
    }

    private SimpleExoPlayer exoPlayer;
    private Uri playerUri;
    private ProgressBar pb_exo_player_progressBar;

    public void prepareExoPlayerForUri(Context context, Uri uri, RelativeLayout exoWrapper) {

        if (context != null && uri != null && exoWrapper.findViewById(R.id.exo_player) != null) {

            final SimpleExoPlayerView simpleExoPlayerView = exoWrapper.findViewById(R.id.exo_player);
            pb_exo_player_progressBar = exoWrapper.findViewById(R.id.pb_exo_player_progressBar);

            // 새로운 플레이어가 필요할때
            if (!uri.equals(playerUri) || exoPlayer == null) {
                playerUri = uri;

                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayer"));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource videoSource = new ExtractorMediaSource(playerUri, dataSourceFactory, extractorsFactory, null, null);

                exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
                exoPlayer.prepare(videoSource);
            }

            exoPlayer.addListener(this);
            exoPlayer.clearVideoSurface();
            exoPlayer.setVideoSurfaceView((SurfaceView) simpleExoPlayerView.getVideoSurfaceView());
            simpleExoPlayerView.setPlayer(exoPlayer);
        }
    }

    // 볼륨 토글
    public void toggleVolume(View soundIcon) {
        if (exoPlayer.getVolume() == 0) {
            exoPlayer.setVolume(1);
            soundIcon.setAlpha(1.0f);
        } else {
            exoPlayer.setVolume(0);
            soundIcon.setAlpha(0.6f);
        }
    }

    // 재생중인지 아닌지 플레그 리턴
    public boolean isPlaying() {
        Log.d(TAG, "exoPlayer.getPlayWhenReady() = " + exoPlayer.getPlayWhenReady());
        return exoPlayer.getPlayWhenReady();
    }

    // 현재 재생중인 위치 리턴
    public long getCurrentPosition() {
        Log.d(TAG, "getCurrentPosition = " + exoPlayer.getCurrentPosition());
        return exoPlayer.getCurrentPosition();
    }

    // 백그라운드로 들어갈때
    public void goToBackground() {
        Log.d(TAG, "goToBackground");
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    // 포어그라운드로 올라올때
    public void goToForeground(long seekTo, boolean isPlaying) {
        Log.d(TAG, "goToForeground = " + seekTo);
        if (exoPlayer != null) {
            exoPlayer.seekTo(seekTo);
            exoPlayer.setPlayWhenReady(isPlaying);
        }
    }

    // 비디오 플레이어 해제
    public void releaseVideoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        exoPlayer = null;
    }

    /*
    * Player.EventListener implements
    * */

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                Log.d(TAG, "onPlayerStateChanged STATE_BUFFERING");
                pb_exo_player_progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_IDLE:
                Log.d(TAG, "onPlayerStateChanged STATE_IDLE");
                pb_exo_player_progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_READY:
                Log.d(TAG, "onPlayerStateChanged STATE_READY");
                Log.d(TAG, "playWhenReady = " + playWhenReady);
                pb_exo_player_progressBar.setVisibility(View.GONE);
                exoPlayer.setPlayWhenReady(playWhenReady);
                break;
            case Player.STATE_ENDED:
                Log.d(TAG, "onPlayerStateChanged STATE_ENDED");
                // 처음으로 돌아가서 정지
                exoPlayer.setPlayWhenReady(false);
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
