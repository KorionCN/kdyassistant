package com.korion.kdyassistant.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.korion.kdyassistant.R
import com.korion.kdyassistant.base.ServiceController
import kotlin.math.max
import kotlin.math.min


class ControlView(context: Context, interval: Long): FrameLayout(context), View.OnClickListener{

    constructor(context: Context): this(context, 1000)

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
    private val btnGo: ImageView
    private val btnInc: ImageView
    private val btnLow: ImageView
    private val tvInterval: TextView
    private val btnClose: ImageView

    private var mController: ServiceController? = null

    private var hasAttachToWindow = false

    private var running = false

    private var mInterval: Long = interval

    private val mPoints = arrayListOf<AimPoint>()

    init {
        btnGo = root.findViewById(R.id.btn_go)
        btnClose = root.findViewById(R.id.btn_close)
        btnInc = root.findViewById(R.id.btn_inc)
        btnLow = root.findViewById(R.id.btn_low)
        tvInterval = root.findViewById(R.id.tv_interval)

        btnGo.setOnClickListener(this)
        btnInc.setOnClickListener(this)
        btnLow.setOnClickListener(this)
        btnClose.setOnClickListener(this)

        flushIntervalText()
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_go -> {
                Log.d(TAG, "btn_go")
                if (!running){
                    setPointDraggable(false)
                    mController?.start()
                    btnGo.setImageResource(R.drawable.ic_pause)
                } else {
                    setPointDraggable(true)
                    mController?.stop()
                    btnGo.setImageResource(R.drawable.ic_play)
                }
                running = !running
            }
            R.id.btn_close -> {
                mController?.close()
            }
            R.id.btn_inc -> {
                mInterval += 100
                flushIntervalText()
            }
            R.id.btn_low -> {
                mInterval = max(100L, mInterval-100)
                flushIntervalText()
            }
        }
    }


    fun setInterval(interval: Long){
        mInterval = max(100L, interval)
        flushIntervalText()
    }

    private fun flushIntervalText(){
        val value = mInterval*1f/1000
        val res = String.format("%.1fs", value)
        tvInterval.text = res
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
   //     Log.d(TAG, "onAttachedToWindow")
        hasAttachToWindow = true
        addInitPoint()
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
        val windowType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            flags,
            PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        windowManager.addView(this, params)

    }

    fun getInterval(): Long{
        return if (running) mInterval else CHECK_RUNNING_INTERVAL
    }

    fun getClickPoints(): List<Point>{
       /* val displayMetrics = context.resources.displayMetrics
        val w = displayMetrics.widthPixels
        val h = displayMetrics.heightPixels
        return arrayListOf(Point(w/2, h/2))
        */
        return mPoints.map {
            it.getPoint()
        }
    }

    fun isRunning(): Boolean = running

    fun setServiceController(controller: ServiceController) {
        mController = controller
    }

    private fun addInitPoint(){
        val point = AimPoint.show(mWindowManager, context)
        mPoints.add(point)
    }

    private fun setPointDraggable(enable: Boolean){
        mPoints.forEach{
            it.setDraggable(mWindowManager, enable)
        }
    }

}