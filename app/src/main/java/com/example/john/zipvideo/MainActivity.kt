package com.example.john.zipvideo

import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.SurfaceHolder.Callback
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.*
import android.widget.Button
import kotlinx.android.synthetic.main.layout_floating_window.view.*
import android.view.WindowManager
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.Toast
import android.content.Intent
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val ALERT_WINDOW_PERMISSION_CODE = 100

    var mMediaPlayer: MediaPlayer? = null
    var mVideoView: TextureView? = null

    var mSystemPlayer: SystemPlayer? = null

    var uri: Uri? = null
    var uri2: Uri = Uri.parse("http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4")
    var uri3: Uri = Uri.parse("http://baobab.wandoujia.com/api/v1/playUrl?vid=2614&editionType=normal")

    var mUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mMediaPlayer = MediaPlayer()

        uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.gao_bai_qi_qiu)
        mUri = uri

        mSystemPlayer = SystemPlayer(this)
        mZipVideoPlayerView.initView(mSystemPlayer)
        mZipVideoPlayerView.setUri(mUri)
        radio_button_group.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when(checkedId){
                    R.id.radio_button_1 ->mUri = uri
                    R.id.radio_button_2 ->mUri = uri2
                    R.id.radio_button_3 ->mUri = uri3
                }
                mZipVideoPlayerView.setUri(mUri)
            }

        })

        mVideoView = findViewById(R.id.video_view)
        mVideoView?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if(mVideoView?.width!! > 0){
                    mVideoView?.viewTreeObserver?.removeOnPreDrawListener(this)
                    val lp: ViewGroup.LayoutParams? = mVideoView?.layoutParams
                    lp?.height = 9 * mVideoView?.width!! /16
                    mVideoView?.layoutParams = lp
                }
                return true
            }
        })
        mVideoView?.layoutParams?.height = mVideoView?.layoutParams?.width!!
        mVideoView?.surfaceTextureListener = mSurfaceTextureListener

        mVideoView?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                mMediaPlayer?.reset()
                mMediaPlayer?.setDataSource(this@MainActivity, mUri)
                mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mMediaPlayer?.prepareAsync()
                mMediaPlayer?.setOnPreparedListener {
                    mMediaPlayer?.isLooping = true
                    mMediaPlayer?.start()
                }
            }

        })

        val button: Button = findViewById(R.id.show_floating_window)
        button.setOnClickListener(View.OnClickListener {
            if(checkPermission()){
                showFloatingWindow()
            }
        })
    }

    private val mSurfaceTextureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            mMediaPlayer?.setSurface(Surface(surface))
        }

    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                Toast.makeText(this, "当前无权限使用悬浮窗，请授权！", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + packageName))
                startActivityForResult(intent, ALERT_WINDOW_PERMISSION_CODE)
                return false
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === ALERT_WINDOW_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this@MainActivity, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    var wm: WindowManager? = null
    var wmParams: LayoutParams? = null
    var floatingWindow: View? = null
    private fun showFloatingWindow(): Unit {

        if(wm == null){
            wm = this.applicationContext.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
            wmParams = LayoutParams()

            //8.0 26 不支持
            wmParams?.type = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) WindowManager.LayoutParams.TYPE_TOAST else WindowManager.LayoutParams.TYPE_PHONE
            wmParams?.format = PixelFormat.TRANSPARENT
            wmParams?.flags = (FLAG_NOT_TOUCH_MODAL
                    or FLAG_NOT_FOCUSABLE
                    or FLAG_LAYOUT_NO_LIMITS)
            wmParams?.gravity = Gravity.LEFT or Gravity.TOP
            val display: Display? = wm?.defaultDisplay
            val width = display?.width
            val height = display?.height
            wmParams?.width = width!! / 2
            wmParams?.height = 9 * wmParams?.width!! / 16
        }

        floatingWindow = LayoutInflater.from(this).inflate(R.layout.layout_floating_window, null, false)
        floatingWindow?.video_surface_view?.holder?.addCallback(mCallback)
        floatingWindow?.setOnTouchListener(FloatingListener())
        wm?.addView(floatingWindow, wmParams)
    }

    private val mCallback = object : Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            mMediaPlayer?.setDisplay(holder)
        }
    }


    var mTouchStartX: Int? = 0
    var mTouchStartY: Int? = 0
    var mTouchCurrentX: Int? = 0
    var mTouchCurrentY: Int? = 0
    private inner class FloatingListener : OnTouchListener {

        override fun onTouch(arg0: View, event: MotionEvent): Boolean {

            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchStartX = event.rawX.toInt()
                    mTouchStartY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    mTouchCurrentX = event.rawX.toInt()
                    mTouchCurrentY = event.rawY.toInt()
                    wmParams!!.x += mTouchCurrentX!! - mTouchStartX!!
                    wmParams!!.y += mTouchCurrentY!! - mTouchStartY!!
                    wm?.updateViewLayout(floatingWindow, wmParams)

                    mTouchStartX = mTouchCurrentX
                    mTouchStartY = mTouchCurrentY
                }
            }
            return false
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }

}
