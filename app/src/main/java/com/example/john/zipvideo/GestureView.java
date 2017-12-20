package com.example.john.zipvideo;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by john on 20/12/2017.
 */

public class GestureView extends FrameLayout {

    private GestureDetectorCompat mGestureDetectorCompat;
    private GestureViewOnGestureListener mGestureViewOnGestureListener;
    private Callback mCallback;

    public GestureView(@NonNull Context context) {
        this(context, null);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @CallSuper
    protected void init() {
        mGestureViewOnGestureListener = new GestureViewOnGestureListener();
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), mGestureViewOnGestureListener);
        mGestureDetectorCompat.setOnDoubleTapListener(mGestureViewOnGestureListener);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
    }

    class GestureViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if(mCallback != null){
                mCallback.onDown(e);
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(mCallback != null){
                mCallback.onSingleTapUp(getWidth(), getHeight(), e.getX(), e.getY());
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(mCallback != null){
                mCallback.onDoubleTap(getWidth(), getHeight(), e.getX(), e.getY());
            }
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(mCallback != null){
                mCallback.onLongPress(getWidth(), getHeight(), e.getX(), e.getY());
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent startPosition, MotionEvent currentPosition, float distanceX, float distanceY) {
            if(mCallback != null){
                mCallback.onScroll(getWidth(), getHeight(), startPosition, currentPosition, distanceX, distanceY);
            }
            return super.onScroll(startPosition, currentPosition, distanceX, distanceY);
        }
    }

    interface Callback{
        void onDown(MotionEvent e);
        void onSingleTapUp(int viewWidth, int viewHeight, float eventX, float eventY);
        void onDoubleTap(int viewWidth, int viewHeight, float eventX, float eventY);
        void onLongPress(int viewWidth, int viewHeight, float eventX, float eventY);
        void onScroll(int viewWidth, int viewHeight, MotionEvent startPosition, MotionEvent currentPosition, float distanceX, float distanceY);
    }

    public static class SimpleCallback implements Callback{

        @Override
        public void onDown(MotionEvent e) {

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

        }
    }

}
