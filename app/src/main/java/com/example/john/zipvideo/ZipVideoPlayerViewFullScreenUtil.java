package com.example.john.zipvideo;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Created by john on 19/12/2017.
 */

public class ZipVideoPlayerViewFullScreenUtil {


    private static Activity convertActivity(Context context){
        if(context == null){
            return null;
        }else if(context instanceof Activity){
            return (Activity) context;
        }else if(context instanceof ContextWrapper){
            return convertActivity(((ContextWrapper)context).getBaseContext());
        }
        return null;
    }


    public static void startFullScreen(Context context, ZipVideoPlayerView.ZipVideoControllerListener zipVideoControllerListener){
        Activity activity = convertActivity(context);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ViewGroup contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        View old = contentView.findViewById(R.id.zip_video_player_view_fullscreen_id);
        if (old != null) {
            contentView.removeView(old);
        }
        ZipVideoPlayerView zipVideoPlayerView = new ZipVideoPlayerView(context);
        zipVideoPlayerView.initView(zipVideoControllerListener);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(zipVideoPlayerView, lp);
    }

}
