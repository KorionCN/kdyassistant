package com.korion.kdyassistant.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import com.korion.kdyassistant.R
import com.korion.kdyassistant.base.ServiceController


class ControlView(context: Context, attrs: AttributeSet?, defaultStyle: Int):
    FrameLayout(context, attrs, defaultStyle), View.OnClickListener{

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null)

    companion object {
        private const val TAG = "ControlView"
        private const val CHECK_RUNNING_INTERVAL = 1000L

        fun show(context: Context): ControlView{
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val controlView = ControlView(context)
            controlView.showInternal(windowManager)
            return controlView
        }
    }

    private lateinit var mWindowManager: WindowManager
    private val root = LayoutInflater.from(context).inflate(R.layout.control_view, this, true)
    private var btnGo: Button
    private var btnClose: Button

    private var mController: ServiceController? = null

    private var hasAttachToWindow = false

    private var running = false

    private var mInterval: Long = 1000

    private val mPoints = arrayListOf<AimPoint>()

    init {
        btnGo = root.findViewById(R.id.btn_go)
        btnGo.setOnClickListener(this)
        btnClose = root.findViewById(R.id.btn_close)
        btnClose.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_go -> {
                Log.d(TAG, "btn_go")
                if (!running){
                    mController?.start()
                    btnGo.text = context.getString(R.string.pause)
                } else {
                    mController?.stop()
                    btnGo.text = context.getString(R.string.go)
                }
                running = !running
            }
            R.id.btn_close -> {
                mController?.close()
            }
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
   //     Log.d(TAG, "onAttachedToWindow")
        hasAttachToWindow = true
    }

    fun destroy(){
        if (hasAttachToWindow){
            mPoints.forEach{
                it.remove(mWindowManager)
            }
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(this)
        }
    }

    private fun showInternal(windowManager: WindowManager){
        mWindowManager = windowManager
        val overlayParam =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayParam,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        windowManager.addView(this, params)

    }

    fun getInterval(): Long{
        return if (running) mInterval else CHECK_RUNNING_INTERVAL
    }

    fun getClickPoints(): List<Point>{
        val displayMetrics = context.resources.displayMetrics
        val w = displayMetrics.widthPixels
        val h = displayMetrics.heightPixels
        return arrayListOf(Point(w/2, h/2))

        /*return mPoints.map {
            it.getPoint()
        }*/
    }

    fun isRunning(): Boolean = running

    fun setServiceController(controller: ServiceController) {
        mController = controller
    }

    private fun addInitPoint(){
        val point = AimPoint.show(mWindowManager, context)
        mPoints.add(point)
    }

}