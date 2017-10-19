package com.johyun.videoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;

/**
 * Created by JH on 2017. 10. 17..
 */

public class CustomVideoView extends RelativeLayout {

    private static final String TAG = CustomVideoView.class.getSimpleName();
    private Context context;

    private VideoView vv_custom_video;
    private ImageView iv_custom_video_thumbnail;
    private RelativeLayout rl_custom_video_controller_wrapper;
    private RelativeLayout rl_custom_video_wrapper;
    private ImageView iv_custom_video_full_screen;
    private ImageView iv_custom_video_share;
    private ImageView iv_custom_video_play;
    private ImageView iv_custom_video_stop;
    private TextView tv_custom_video_elapsed_time;
    private TextView tv_custom_video_total_time;
    private ImageView iv_custom_video_sound;
    private SeekBar sb_custom_video_seekbar;
    private ProgressBar pb_custom_video_progressBar;

    private int pausePos = 0;
    private boolean isStopVideo = false;
    private long updateCycle = 100;

    private Handler updateSeekBarAndTimeTextHandler = new Handler(Looper.getMainLooper());

//    private String tempUrl = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_2mb.mp4";
    private String tempUrl = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4";

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.custom_video, this, true);
        initUi();
        setVideoViewHeight();

        rl_custom_video_controller_wrapper.setVisibility(GONE); // hide all controller
        pb_custom_video_progressBar.setVisibility(VISIBLE);
        iv_custom_video_thumbnail.setVisibility(VISIBLE);
        vv_custom_video.setVisibility(GONE);

        loadVideo(tempUrl, true, true);

        vv_custom_video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            // Progress 출력
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_START");
                            break;

                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            // Progress 삭제
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
                            break;
                    }
                    return false;
                }
            }
        );

        //동영상 준비 이벤트
        vv_custom_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "onPrepared");

                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
                        Log.d(TAG, "onBufferingUpdate percent = " + percent);
                    }
                });

                rl_custom_video_controller_wrapper.setVisibility(VISIBLE); // show all controller
                pb_custom_video_progressBar.setVisibility(GONE);
                iv_custom_video_thumbnail.setVisibility(GONE);
                vv_custom_video.setVisibility(VISIBLE);

                sb_custom_video_seekbar.setMax(vv_custom_video.getDuration());

                // seekbar 와 timeText 업데이트
                updateSeekBarAndTimeTextHandler.postDelayed(updateSeekBarAndTimeTextRunnable, updateCycle);

                // 동영상 플레이 버튼
                iv_custom_video_play.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iv_custom_video_play.setVisibility(GONE);
                        iv_custom_video_stop.setVisibility(VISIBLE);

                        isStopVideo = false;
                        seekTo(pausePos);
                        startVideo();
                    }
                });

                // 동영상 일시정지 버튼
                iv_custom_video_stop.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iv_custom_video_play.setVisibility(VISIBLE);
                        iv_custom_video_stop.setVisibility(GONE);

                        isStopVideo = true;
                        pauseVideo();
                    }
                });

                iv_custom_video_share.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo share logic
                    }
                });

                iv_custom_video_full_screen.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo full screen logic
                    }
                });

                iv_custom_video_sound.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //todo sound logic
                    }
                });

                vv_custom_video.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        rl_custom_video_controller_wrapper.setVisibility(rl_custom_video_controller_wrapper.getVisibility() == GONE ? VISIBLE : GONE);
                    }
                });
            }
        });

        //동영상 재생 완료 이벤트
        vv_custom_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                iv_custom_video_play.setVisibility(VISIBLE);
                iv_custom_video_stop.setVisibility(GONE);

                isStopVideo = true;
                pauseVideo();
                seekTo(0);
            }
        });

        // 동영상 에러 이벤트
        vv_custom_video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                String msg;
                if (what == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    msg = "Video Load Time Out";
                } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    msg = "Video Load Server Die";
                } else {
                    msg = "Video Load Unknown Error";
                }
                Log.d(TAG, msg);
                vv_custom_video.stopPlayback();
                return false;
            }
        });

        //재생위치 변경
        sb_custom_video_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseVideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
                pausePos = seekBar.getProgress();
                if (!isStopVideo) {
                    startVideo();
                }
            }
        });
    }

    //영상정보 로드
    //todo 스트리밍 됬다 안됬다 하는 이유?????????
    public void loadVideo(String url, boolean timeShow, boolean thumbnailShow) {
        Log.d(TAG, "loadVideo");
        vv_custom_video.setVideoURI(Uri.parse(url));
        vv_custom_video.requestFocus();

        // 재생시간 숨김
        if (!timeShow) {
            tv_custom_video_elapsed_time.setVisibility(GONE);
            tv_custom_video_total_time.setVisibility(GONE);
        }

        // Thumbnail Task
        if (thumbnailShow) {
            new CreateThumbnailAsyncTask(new CreateThumbnailAsyncTask.CreateThumbnailCompleteListener() {
                @Override
                public void onCreateComplete(ByteArrayOutputStream stream) {
                    Glide.with(context)
                            .load(stream.toByteArray())
                            .asBitmap()
//                .placeholder(R.drawable.placeholder_image)
//                .error(R.drawable.error_image)
                            .into(iv_custom_video_thumbnail);
                }
            }).execute(tempUrl);
        }
    }

    //동영상 플레이
    public void startVideo() {
        vv_custom_video.start();
    }

    //동영상 일시정지
    public void pauseVideo() {
        vv_custom_video.pause();
        pausePos = vv_custom_video.getCurrentPosition(); // 현재 재생위치 저장
    }

    //동영상 재생 위치
    public void seekTo(long timeSec) {
        vv_custom_video.seekTo((int)timeSec);
    }

    private Runnable updateSeekBarAndTimeTextRunnable = new Runnable(){
        public void run(){
            if (!isStopVideo) {
                sb_custom_video_seekbar.setProgress(vv_custom_video.getCurrentPosition());
                //todo setText 에러나는 이유???????
             tv_custom_video_elapsed_time.setText(vv_custom_video.getCurrentPosition());
             tv_custom_video_total_time.setText(vv_custom_video.getDuration());

                updateSeekBarAndTimeTextHandler.postDelayed(this, updateCycle);
            }
        }
    };

    private void setVideoViewHeight() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rl_custom_video_wrapper.getLayoutParams();
        layoutParams.height = dpToPx((getDeviceWidth() * 9) / 16);
        rl_custom_video_wrapper.setLayoutParams(layoutParams);

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

    //todo 스크롤해서 화면에서 안보이면 영상 정지
    private boolean isShowVideoView() {
        return rl_custom_video_wrapper.isShown();
    }

    //UI 초기화
    private void initUi() {
        vv_custom_video = findViewById(R.id.vv_custom_video);
        iv_custom_video_thumbnail = findViewById(R.id.iv_custom_video_thumbnail);
        iv_custom_video_full_screen = findViewById(R.id.iv_custom_video_full_screen);
        iv_custom_video_share = findViewById(R.id.iv_custom_video_share);
        iv_custom_video_play = findViewById(R.id.iv_custom_video_play);
        iv_custom_video_stop = findViewById(R.id.iv_custom_video_stop);
        iv_custom_video_sound = findViewById(R.id.iv_custom_video_sound);
        rl_custom_video_controller_wrapper = findViewById(R.id.rl_custom_video_controller_wrapper);
        rl_custom_video_wrapper = findViewById(R.id.rl_custom_video_wrapper);
        tv_custom_video_elapsed_time = findViewById(R.id.tv_custom_video_elapsed_time);
        tv_custom_video_total_time = findViewById(R.id.tv_custom_video_total_time);
        sb_custom_video_seekbar = findViewById(R.id.sb_custom_video_seekbar);
        pb_custom_video_progressBar = findViewById(R.id.pb_custom_video_progressBar);
    }
}
