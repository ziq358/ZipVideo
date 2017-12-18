package com.example.john.zipvideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/12/16.
 */

public class ZipVideoPlayerView extends FrameLayout implements View.OnClickListener{

    private View mRootView, mTopController, mBottomController;
    private ZipVideoControllerListener mZipVideoControllerListener;
    private TextureView mTextureView;
    private TextView mCurrentTime, mEndTime;
    private ImageView mCenterStatusIcon, mBottomStatusIcon;
    private SeekBar mSeekBar;
    private ProgressBar mLoading;
    private boolean mIsPlayFirstTap = true;

    public ZipVideoPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ZipVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZipVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setUri(Uri uri) {
        mIsPlayFirstTap = true;
        mZipVideoControllerListener.setSourceUri(uri);
    }

    private void initView() {
        mRootView = inflate(this.getContext(), R.layout.video_player_view_layout, this);
        mRootView.findViewById(R.id.video_controller_back).setOnClickListener(this);
        mRootView.findViewById(R.id.video_controller_play).setOnClickListener(this);
        mRootView.findViewById(R.id.video_controller_status_icon).setOnClickListener(this);
        mRootView.findViewById(R.id.video_controller_fullscreen).setOnClickListener(this);

        mRootView.findViewById(R.id.video_controller_gesture).setOnClickListener(this);
        mTopController = mRootView.findViewById(R.id.rl_video_controller_top);
        mBottomController = mRootView.findViewById(R.id.rl_video_controller_bottom);
        mLoading = mRootView.findViewById(R.id.video_controller_loading);
        mTextureView = (TextureView) mRootView.findViewById(R.id.video_texture_view);

        mCenterStatusIcon = (ImageView) mRootView.findViewById(R.id.video_controller_status_icon);
        mBottomStatusIcon = (ImageView) mRootView.findViewById(R.id.video_controller_play);

        mCurrentTime = (TextView) mRootView.findViewById(R.id.video_controller_currentTime);
        mEndTime = (TextView) mRootView.findViewById(R.id.video_controller_endTime);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.video_controller_seekBar);
        mSeekBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //禁止拖动
                return true;
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mZipVideoControllerListener.onSeekTo(progress);
                    mCurrentTime.setText(generateTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mZipVideoControllerListener = new SystemPlayer(this.getContext());
        mZipVideoControllerListener.setCallback(new Callback() {
            @Override
            public void onStatusUpdate(int duration, int currentPosition, int bufferPercent, boolean isPlaying) {
                mCurrentTime.setText(generateTime(currentPosition));
                mEndTime.setText(generateTime(duration));
                mSeekBar.setMax(duration);
                mSeekBar.setProgress(currentPosition);
                mSeekBar.setSecondaryProgress(duration * bufferPercent / 100);
                mSeekBar.setOnTouchListener(null);
                mLoading.setVisibility(View.GONE);
                if(isPlaying){
                    mCenterStatusIcon.setVisibility(View.INVISIBLE);
                    mCenterStatusIcon.setImageResource(R.drawable.ic_center_pause);
                    mBottomStatusIcon.setImageResource(R.drawable.ic_pause);
                }else{
                    mCenterStatusIcon.setVisibility(View.VISIBLE);
                    mCenterStatusIcon.setImageResource(R.drawable.ic_center_play);
                    mBottomStatusIcon.setImageResource(R.drawable.ic_play);
                }
            }
        });
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

    }

    private void showOrHideControllerPlane(){
        if(mTopController.getVisibility() == View.VISIBLE){
            mTopController.setVisibility(View.GONE);
            mBottomController.setVisibility(View.GONE);
        }else{
            mTopController.setVisibility(View.VISIBLE);
            mBottomController.setVisibility(View.VISIBLE);
        }

    }

    private String generateTime(int time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mZipVideoControllerListener.setSurface(new Surface(surface));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_controller_back:
                onBack();
                break;
            case R.id.video_controller_gesture:
                showOrHideControllerPlane();
                break;
            case R.id.video_controller_status_icon:
            case R.id.video_controller_play:
                if(mIsPlayFirstTap){
                    mLoading.setVisibility(View.VISIBLE);
                    mCenterStatusIcon.setVisibility(View.INVISIBLE);
                    mBottomStatusIcon.setImageResource(R.drawable.ic_pause);
                }
                mIsPlayFirstTap = false;
                if(mZipVideoControllerListener.isPlaying()){
                    mZipVideoControllerListener.onPause();
                }else{
                    mZipVideoControllerListener.onPlay();
                }
                break;
            case R.id.video_controller_fullscreen:
                onFullScreen();
                break;
        }
    }

    private void onBack(){

    }

    private void onFullScreen(){

    }

    interface  ZipVideoControllerListener{

        void setSourceUri(Uri uri);
        void setSurface(Surface surface);

        void onPlay();
        void onPause();
        void onSeekTo(int progress);

        int getDuration();
        int getCurrentProgress();
        boolean isPlaying();
        void setCallback(Callback callback);
    }

    interface Callback{
        void onStatusUpdate(int duration, int currentPosition, int bufferPercent, boolean isPlaying);
    }

    // 为了以后拓展用 其他 播放器，
    static class SystemPlayer implements ZipVideoControllerListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

        private final static int UPDATE_SEEK_BAR = 1;

        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Context mContext;
        private Uri sourceUri;
        private boolean mIsPause;
        private int mBufferPercent;
        private Callback mCallback;

        public SystemPlayer(Context context) {
            mContext = context;
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        @Override
        public void setSourceUri(Uri uri) {
            sourceUri = uri;
            mIsPause = false;
            mMediaPlayer.reset();
        }

        @Override
        public void setSurface(Surface surface) {
            mMediaPlayer.setSurface(surface);
        }

        @Override
        public void onPlay() {
            if(!mMediaPlayer.isPlaying()){
                if(mIsPause){
                    mIsPause = false;
                    mMediaPlayer.start();
                    mHandler.sendEmptyMessage(UPDATE_SEEK_BAR);
                }else{
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(mContext, sourceUri);
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setOnErrorListener(this);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnBufferingUpdateListener(this);
                        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mMediaPlayer.start();
                                mHandler.sendEmptyMessage(UPDATE_SEEK_BAR);
                            }
                        });
                        mMediaPlayer.setOnCompletionListener(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        @Override
        public void onPause() {
            mIsPause = true;
            mMediaPlayer.pause();
            if(mCallback != null){
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent, isPlaying());
            }
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }

        @Override
        public void onSeekTo(int progress) {
            mIsPause = false;
            mMediaPlayer.seekTo(progress);
            if(!isPlaying()){
                mMediaPlayer.start();
            }
            mHandler.sendEmptyMessage(UPDATE_SEEK_BAR);
        }

        @Override
        public int getDuration() {
            return mMediaPlayer.getDuration();
        }

        @Override
        public int getCurrentProgress() {
            return mMediaPlayer.getCurrentPosition();
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying() && !mIsPause;
        }


        private Handler mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_SEEK_BAR:
                        if(mCallback != null){
                            mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent,isPlaying());
                        }
                        mHandler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 1000);
                        break;
                }
            }
        };

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercent = percent;
            if(mCallback != null){
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent, isPlaying());
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(mCallback != null){
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent,isPlaying());
            }
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return true;
        }
    }

}
