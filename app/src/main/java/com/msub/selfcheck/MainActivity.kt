package com.msub.selfcheck

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<BottomNavigationView>(R.id.nav_view).setupWithNavController(findNavController(R.id.nav_host_fragment))
        requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if(!((getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName))) {
            AlertDialog.Builder(this).apply {
                setMessage("정상적인 앱 사용을 위해 배터리 사용량 최적화 목록에서 제외 합니다.")
                setCancelable(false)
                setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                    startActivity(Intent().apply {
                        setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        setData(Uri.parse("package:"+packageName))
                    }
                    )
                })
            }.show()
        }
    }
}