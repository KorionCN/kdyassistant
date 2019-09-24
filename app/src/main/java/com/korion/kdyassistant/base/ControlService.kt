package com.korion.kdyassistant.base

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.korion.kdyassistant.ui.ControlView
import com.korion.kdyassistant.utils.showToast
import kotlin.concurrent.timer


class ControlService: Service(), ServiceController {

    companion object {
        private const val TAG = "ControlService"
        const val KEY_PERIOD = "period"
        const val KEY_INTERVAL = "interval"
    }

    private lateinit var mControlView: ControlView
    private lateinit var mHandler: Handler
    private var period: Long = 0
    private var interval: Long = 0

    private val mAutoTask = object : Runnable{
        override fun run() {
            if (mControlView.isRunning()){
                val points = mControlView.getClickPoints()
                points.forEach {
                    KdyAssistantService.performClick(it.x, it.y)
                }
            }
            val interval = mControlView.getInterval()
            if (period > 0){
                val time = System.currentTimeMillis()
                if (time - mStartTime < period){
                    mHandler.postDelayed(this, interval)
                } else {
                    stop()
                }
            } else {
                mHandler.postDelayed(this, interval)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {
            period = getIntExtra(KEY_PERIOD, 0).toLong()
            interval = getIntExtra(KEY_INTERVAL, 0).toLong()
        }
        showToast("时长:${period/1000}s, 间隔:${interval/1000}s")
        mControlView.setInterval(interval)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        mControlView = ControlView.show(baseContext)
        mControlView.setServiceController(this)
        mHandler = Handler(Looper.myLooper())
    }


    override fun onDestroy() {
        mControlView.destroy()
        super.onDestroy()
    }

    /**
     * ServiceController
     * ***************************************************/
    private var mStartTime: Long = 0
    override fun start() {
        mStartTime = System.currentTimeMillis()
        mHandler.postDelayed(mAutoTask, 1000L)
    }

    override fun stop() {
        mHandler.removeCallbacks(mAutoTask)
    }

    override fun close() {
        mHandler.removeCallbacks(mAutoTask)
        stopSelf()
    }

}