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
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Administrator on 2017/12/16.
 */

public class ZipVideoPlayerView extends FrameLayout implements View.OnClickListener{

    private View mRootView;
    private ZipVideoControllerListener mZipVideoControllerListener;
    private TextureView mTextureView;
    private TextView mCurrentTime, mEndTime;
    private SeekBar mSeekBar;
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

    private void initView() {
        mRootView = inflate(this.getContext(), R.layout.video_player_view_layout, this);
        mRootView.findViewById(R.id.video_controller_back).setOnClickListener(this);
        mRootView.findViewById(R.id.video_controller_play).setOnClickListener(this);
        mRootView.findViewById(R.id.video_controller_fullscreen).setOnClickListener(this);
        mTextureView = mRootView.findViewById(R.id.video_texture_view);

        mCurrentTime =  mRootView.findViewById(R.id.video_controller_currentTime);
        mEndTime =  mRootView.findViewById(R.id.video_controller_endTime);
        mSeekBar =  mRootView.findViewById(R.id.video_controller_seekBar);
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
                    Log.e("ziq", "setCurrentProgress: "+ progress);
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
        mZipVideoControllerListener.setSourceUri(Uri.parse("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4"));
        mZipVideoControllerListener.setCallback(new Callback() {
            @Override
            public void onStatusUpdate(int duration, int currentPosition, int bufferPercent) {
                mCurrentTime.setText(generateTime(currentPosition));
                mEndTime.setText(generateTime(duration));
                mSeekBar.setMax(duration);
                mSeekBar.setProgress(currentPosition);
                mSeekBar.setSecondaryProgress(bufferPercent);
                mSeekBar.setOnTouchListener(null);
                Log.e("ziq", "onStatusUpdate: "+bufferPercent);
            }
        });
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

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
            case R.id.video_controller_play:
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
        void onStatusUpdate(int duration, int currentPosition, int bufferPercent);
    }

    // 为了以后拓展用 其他 播放器，
    static class SystemPlayer implements ZipVideoControllerListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener{

        private final static int UPDATE_SEEK_BAR = 1;

        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Context mContext;
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
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mContext, uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
                }
            }
        }

        @Override
        public void onPause() {
            mIsPause = true;
            mMediaPlayer.pause();
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }

        @Override
        public void onSeekTo(int progress) {
            mMediaPlayer.seekTo(progress);
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
                            mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent);
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
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(mCallback != null){
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent);
            }
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }



    }

}
