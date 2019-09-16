package com.korion.kdyassistant.base

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.korion.kdyassistant.R
import com.korion.kdyassistant.utils.showToast


class KdyAssistantService: AccessibilityService(){

    private var ready = false

    companion object {
        private const val TAG = "KdyAssistantService"
        private var assistantService: KdyAssistantService? = null

        fun performClick(x: Int, y: Int){
            assistantService?.performClickInternal(x, y)
        }
    }

    override fun onInterrupt() {
        //ignore
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //ignore
    }

    override fun onServiceConnected() {
        ready = true
        assistantService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        assistantService = null
    }

    private fun performClickInternal(x: Int, y: Int): Boolean{
        if (!ready){
            showToast(R.string.accessibility_service_close)
            return false
        }
        Log.d(TAG, "performClickInternal: $x, $y")
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 100L))
            .build()
        return dispatchGesture(gestureDescription, null, null)

    }


}