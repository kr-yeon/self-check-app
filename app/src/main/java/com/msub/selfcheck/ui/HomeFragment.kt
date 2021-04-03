package com.msub.selfcheck.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.POWER_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msub.selfcheck.MyService
import com.msub.selfcheck.R
import com.msub.selfcheck.recyclerview.MyAdapter

class HomeFragment : Fragment() {
    @SuppressLint("BatteryLife")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.home, container, false)
        val recyclerv=root.findViewById<RecyclerView>(R.id.recyclerv)
        val startbtn=root.findViewById<Button>(R.id.startbtn)
        val stopbtn=root.findViewById<Button>(R.id.quitbtn)
        startbtn.setOnClickListener {
            if(!((activity?.getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(activity?.packageName))) {
                AlertDialog.Builder(activity).apply {
                    setMessage("정상적인 앱 사용을 위해 배터리 사용량 최적화 목록에서 제외 합니다.")
                    setCancelable(false)
                    setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                        startActivity(Intent().apply {
                            setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            setData(Uri.parse("package:"+activity?.packageName))
                        }
                        )
                    })
                }.show()
            } else {
                if(gettime()=="no time") {
                    AlertDialog.Builder(activity)
                            .setTitle("설정")
                            .setMessage("시간 탭에서 시간을 먼저 설정해주세요.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, id -> {}})
                            .create()
                            .show()
                } else {
                    activity?.startService(Intent(activity, MyService::class.java))
                    AlertDialog.Builder(activity)
                            .setTitle("완료")
                            .setMessage("시작되었습니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, id -> {}})
                            .create()
                            .show()
                }
            }
        }
        stopbtn.setOnClickListener {
            activity?.stopService(Intent(activity, MyService::class.java))
            AlertDialog.Builder(activity)
                    .setTitle("완료")
                    .setMessage("종료되었습니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, id -> {}})
                    .create()
                    .show()
        }
        val datas=getData()
        if(datas!="") recyclerv.adapter=MyAdapter(datas.split("\n").subList(0, datas.split("\n").size-1), activity, recyclerv)
        else recyclerv.adapter=MyAdapter(listOf(), activity, recyclerv)
        recyclerv.layoutManager=LinearLayoutManager(activity)
        return root
    }

    fun getData():String {
        return activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)?.getString("users", "") ?: ""
    }

    fun gettime():String {
        return activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)?.getString("time", "no time") ?: "no time"
    }
}