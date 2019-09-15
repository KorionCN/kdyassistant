package com.korion.kdyassistant.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.korion.kdyassistant.R

class AimPoint(context: Context): ImageView(context){

    companion object {
        private const val TAG = "AimPoint"

        fun show(windowManager: WindowManager, context: Context): AimPoint {
            val displayMetrics = context.resources.displayMetrics
            val w = displayMetrics.widthPixels
            val h = displayMetrics.heightPixels
            Log.d(TAG, "display: width = $w, height = $h")
            val point = AimPoint(context)
            val params = WindowManager.LayoutParams()
            params.apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
            windowManager.addView(point, params)
            return point
        }
    }

    init {
        setImageResource(R.drawable.ic_add)
    }

    fun getPoint(): Point {
        val tmp = intArrayOf(0, 0)
        getLocationOnScreen(tmp)
        return Point(tmp[0], tmp[1])
    }

    private var lastX: Float = 0f
    private var lastY: Float = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX =  event.rawX - lastX
                val offsetY =  event.rawY - lastY
                x += offsetX
                y += offsetY
                return true
            }
        }
        return true
    }



  /*private val DRAG_DISTANCE_THRESHOLD = 10
    private fun isDragging(event: MotionEvent): Boolean =
        ((Math.pow((event.rawX - lastX).toDouble(), 2.0)
                + Math.pow((event.rawY - lastY).toDouble(), 2.0))
                > DRAG_DISTANCE_THRESHOLD * DRAG_DISTANCE_THRESHOLD)*/

    fun remove(windowManager: WindowManager){
        windowManager.removeView(this)
    }
}