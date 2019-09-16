package com.korion.kdyassistant.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.ImageView
import com.korion.kdyassistant.R

class AimPoint(context: Context): ImageView(context){

    companion object {
        private const val TAG = "AimPoint"

        @SuppressLint("ClickableViewAccessibility")
        fun show(windowManager: WindowManager, context: Context): AimPoint {
            val displayMetrics = context.resources.displayMetrics
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            Log.d(TAG, "display: width = $width, height = $height")
            val point = AimPoint(context)

            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            val flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            val params = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                windowType,
                flags ,
                PixelFormat.TRANSLUCENT
            )
            point.setOnTouchListener(object : OnTouchListener{
                private var lastX: Float = 0f
                private var lastY: Float = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when(event.action){
                        MotionEvent.ACTION_DOWN -> {
                            lastX = event.rawX
                            lastY = event.rawY
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val offsetX =  event.rawX - lastX
                            val offsetY =  event.rawY - lastY
                            params.x += offsetX.toInt()
                            params.y += offsetY.toInt()
                            windowManager.updateViewLayout(point, params)
                            lastX = event.rawX
                            lastY = event.rawY
                        }
                    }
                    return false
                }
            })
            windowManager.addView(point, params)
            return point
        }
    }

    init {
       // setBackgroundColor(Color.YELLOW)
        setImageResource(R.drawable.ic_add)
    }

    fun getPoint(): Point {
        val tmp = intArrayOf(0, 0)
        getLocationOnScreen(tmp)
        val x = tmp[0] + width/2
        val y= tmp[1] + height/2
        return Point(x, y)
    }

    fun remove(windowManager: WindowManager){
        windowManager.removeView(this)
    }

    fun setDraggable(windowManager: WindowManager, enable: Boolean){
        val params = layoutParams
        if (params is WindowManager.LayoutParams){
            if (enable){
                params.flags = params.flags and
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()  //取反
            } else {
                params.flags = params.flags or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            windowManager.updateViewLayout(this, params)
        }
    }
}