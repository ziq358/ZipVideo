package com.example.john.zipvideo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by john on 20/12/2017.
 */

public class ZipVideoPlayerGestureView extends GestureView implements GestureView.Callback {

    private AudioManager mAudioManager;
    private int mMaxVoice, mCurrentVoice;

    public ZipVideoPlayerGestureView(@NonNull Context context) {
        this(context, null);
    }

    public ZipVideoPlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZipVideoPlayerGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        setCallback(this);
        setActivityLight(getContext(), getSystemBrightness(getContext())/255f);
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDown(MotionEvent e) {
        mMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onSingleTapUp(int viewWidth, int viewHeight, float eventX, float eventY) {

    }

    @Override
    public void onDoubleTap(int viewWidth, int viewHeight, float eventX, float eventY) {

    }

    @Override
    public void onLongPress(int viewWidth, int viewHeight, float eventX, float eventY) {

    }

    @Override
    public void onScroll(int viewWidth, int viewHeight, MotionEvent startPosition, MotionEvent currentPosition, float distanceX, float distanceY) {
        float startX = startPosition.getX();
        float startY = startPosition.getY();

        float currentX = currentPosition.getX();
        float currentY = currentPosition.getY();

        float dx = Math.abs(currentX - startX);
        float dy = Math.abs(currentY - startY);

        if(dx > dy){
            //横向
            Log.e("ziq", "onScroll: 横向");
        }else{
            //竖向
            if(0 < startX && startX < viewWidth / 2){
                // 左 亮度
                setActivityLight(getContext(), getActivityLight(getContext()) + distanceY / viewHeight);
            }else if (viewWidth / 2 < startX && startX < viewWidth){
                // 右 声音
                int result = mCurrentVoice + (int) (mMaxVoice * (startY - currentY) / (float)viewHeight) ;
                result = result > mMaxVoice ? mMaxVoice : result;
                result = result < 0 ? 0 : result;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, result, AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    private void setActivityLight(Context context, float brightValue){
        Activity activity = ZipVideoPlayerViewFullScreenUtil.convertActivity(context);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        brightValue = brightValue > 1f ? 1f : brightValue;
        brightValue = brightValue < 0.2f ? 0.2f : brightValue;
        lp.screenBrightness = brightValue;
        activity.getWindow().setAttributes(lp);
    }

    private float getActivityLight(Context context){
        Activity activity = ZipVideoPlayerViewFullScreenUtil.convertActivity(context);
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        return lp.screenBrightness;
    }

    public static int getSystemBrightness(Context context) {
        Activity activity = ZipVideoPlayerViewFullScreenUtil.convertActivity(context);
        int brightValue = 0;
        ContentResolver contentResolver = activity.getContentResolver();
        try {
            brightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightValue;
    }


    public static void setSysBrightness(Context context, int brightValue) {
        Activity activity = ZipVideoPlayerViewFullScreenUtil.convertActivity(context);
        ContentResolver contentResolver = activity.getContentResolver();
        brightValue = brightValue > 255 ? 255 : brightValue;
        brightValue = brightValue < 50 ? 50 : brightValue;
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightValue);
    }



}
