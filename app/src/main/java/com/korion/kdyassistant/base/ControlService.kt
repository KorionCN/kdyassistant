package com.korion.kdyassistant.base

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.korion.kdyassistant.ui.ControlView


class ControlService: Service(), ServiceController {

    companion object {
        private const val TAG = "ControlService"
    }

    private lateinit var mControlView: ControlView
    private lateinit var mHandler: Handler

    private val mAutoTask = object : Runnable{
        override fun run() {
            if (mControlView.isRunning()){
                val points = mControlView.getClickPoints()
                points.forEach {
                    KdyAssistantService.performClick(it.x, it.y)
                }
            }
            val interval = mControlView.getInterval()
            mHandler.postDelayed(this, interval)
        }
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
    override fun start() {
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