package com.korion.kdyassistant.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import com.korion.kdyassistant.BuildConfig
import com.korion.kdyassistant.R
import com.korion.kdyassistant.base.ControlService
import com.korion.kdyassistant.utils.showToast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "MainActivity"
        const val SYSTEM_WINDOW_REQUEST_CODE = 1
        const val KEY_TIME = "time"
        const val KEY_INTERVAL = "interval"
    }

    private var mCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        btn_launch.setOnClickListener(this)
        tv_count.setOnClickListener(this)
      //  btn_test.setOnClickListener(this)

        val sp = getPreferences(Context.MODE_PRIVATE)
        val time = sp.getInt(KEY_TIME, 1800)
        val interval = sp.getInt(KEY_INTERVAL, 6000)
        edt_period.setText(time.toString())
        edt_interval.setText(interval.toString())
        tv_version.text = String.format(getString(R.string.version, BuildConfig.VERSION_NAME))

        if (!Settings.canDrawOverlays(this) || !isAccessibilityServiceEnabled()){
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_request)
                .setMessage(R.string.need_permission_desc)
                .setNegativeButton(R.string.cancel){ dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.confirm){ dialog, _ ->
                    checkSystemWindowPermission() && checkAccessibilityService()
                }.show()
        }

        tv_version.setOnLongClickListener {
            enterEngineerMode()
            return@setOnLongClickListener true
        }
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.btn_launch -> {
                if (checkSystemWindowPermission() && checkAccessibilityService()){
                    val period: Int
                    val interval: Int
                    try {
                        period = edt_period.text.toString().toInt() * 1000
                        interval = edt_interval.text.toString().toInt()
                    } catch (e: NumberFormatException){
                        showToast("输入数值有误")
                        return
                    }
                    val sp = getPreferences(Context.MODE_PRIVATE)
                    sp.edit().putInt(KEY_TIME, period/1000)
                        .putInt(KEY_INTERVAL, interval)
                        .apply()
                    //启动服务
                    val intent = Intent(this, ControlService::class.java)
                    intent.putExtra(ControlService.KEY_PERIOD, period)
                    intent.putExtra(ControlService.KEY_INTERVAL, interval)
                    startService(intent)
                    launchReaderActivity()
                    finish()
                } else {
                    showToast("需配置权限")
                }
            }
            R.id.tv_count -> {
                mCount++
                tv_count.text = mCount.toString()
            }
        }
    }

    private fun checkSystemWindowPermission(): Boolean{
        if (!Settings.canDrawOverlays(this)){
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent,
                SYSTEM_WINDOW_REQUEST_CODE
            )
            return false
        }
        return true
    }

    private fun checkAccessibilityService(): Boolean{
        if (!isAccessibilityServiceEnabled()){
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            if (intent.resolveActivity(packageManager) != null){
                startActivity(intent)
            } else {
                showToast("系统不支持辅助服务")
            }
            return false
        }
        return true
    }

    private fun isAccessibilityServiceEnabled(): Boolean{
        val service = baseContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = service.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        list.forEach{
            val serviceInfo = it.resolveInfo.serviceInfo
          //  Log.d(TAG, serviceInfo.packageName)
            if (serviceInfo.packageName == packageName){
                return true
            }
        }
        return false
    }

    private fun launchReaderActivity(){
        val intent = packageManager.getLaunchIntentForPackage("com.yuewen.cooperate.ekreader")
        if (intent != null){
            startActivity(intent)
        }
    }

    private fun enterEngineerMode(){
        val intent = packageManager.getLaunchIntentForPackage("com.sprd.engineermode")
        if (intent != null){
            startActivity(intent)
        } else {
            showToast(R.string.engineer_mode)
        }
    }

}
