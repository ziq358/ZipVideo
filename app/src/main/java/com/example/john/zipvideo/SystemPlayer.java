package com.example.john.zipvideo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.example.john.zipvideo.ZipVideoPlayerView;
import com.example.john.zipvideo.ZipVideoPlayerView.Callback;
import com.example.john.zipvideo.ZipVideoPlayerView.ZipVideoControllerListener;

import java.io.IOException;

public class SystemPlayer implements ZipVideoControllerListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{

        private final static int UPDATE_SEEK_BAR = 1;

        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Context mContext;
        private Uri sourceUri;
        private boolean mIsPause;
        private int mBufferPercent;
        private Callback mCallback;
        private Surface mSurface;

        public SystemPlayer(Context context) {
            mContext = context;
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        @Override
        public void release() {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            if(mCallback != null){
                mCallback.onStatusUpdate(0, 0, 0, false);
            }
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }

        @Override
        public void setSourceUri(Uri uri) {
            release();
            mMediaPlayer = new MediaPlayer();
            if(mSurface != null){
                mMediaPlayer.setSurface(mSurface);
            }
            sourceUri = uri;
            mIsPause = false;
        }



        @Override
        public void setSurface(Surface surface) {
            mSurface = surface;
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
                            mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent, isPlaying());
                        }
                        mHandler.sendEmptyMessageDelayed(UPDATE_SEEK_BAR, 1000);
                        break;
                }
            }
        };

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercent = percent;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(mCallback != null){
                mCallback.onStatusUpdate(getDuration(), getCurrentProgress(), mBufferPercent, false);
            }
            mHandler.removeMessages(UPDATE_SEEK_BAR);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return true;
        }
    }