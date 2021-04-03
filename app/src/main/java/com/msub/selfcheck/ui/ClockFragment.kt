package com.msub.selfcheck.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.msub.selfcheck.R

class ClockFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.clock, container, false)
        val mytp=root.findViewById<TimePicker>(R.id.mytp)
        root.findViewById<Button>(R.id.mybtn).run {
            setOnClickListener {
                activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)!!.edit().run{
                    putString("time", "${mytp.hour}:${mytp.minute}")
                    commit()
                }
                AlertDialog.Builder(activity)
                        .setTitle("설정 완료")
                        .setMessage("시간 설정이 완료 되었습니다.\n설정된 시각 : ${if(mytp.hour < 12) "오전 "+mytp.hour.toString() else "오후 "+if(mytp.hour==12) "12" else (mytp.hour-12).toString()}시 ${mytp.minute}분")
                        .setPositiveButton("확인", DialogInterface.OnClickListener {dialogInterface, id -> {}})
                        .create()
                        .show()
            }
        }
        root.findViewById<Switch>(R.id.myswitch).run {
            isChecked=onStartFrg()
            setOnClickListener {
                if(isChecked) {
                    activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)!!.edit().run{
                        putBoolean("cancle", true)
                        commit()
                    }
                    AlertDialog.Builder(activity)
                            .setTitle("설정 완료")
                            .setMessage("이제 토/일요일에는 자가진단이 실시되지 않습니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener {dialogInterface, id -> {}})
                            .create()
                            .show()
                } else {
                    activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)!!.edit().run{
                        putBoolean("cancle", false)
                        commit()
                    }
                    AlertDialog.Builder(activity)
                            .setTitle("설정 완료")
                            .setMessage("이제 토/일요일에도 자가진단이 실시됩니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener {dialogInterface, id -> {}})
                            .create()
                            .show()
                }
            }
        }
        val time=onStartFragment()
        if(time!="no time") time.run {
            mytp.hour=split(":")[0].toInt()
            mytp.minute=split(":")[1].toInt()
        }
        return root
    }

    fun onStartFragment():String {
        return activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)?.getString("time", "no time") ?: "no time"
    }

    fun onStartFrg():Boolean {
        return activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)?.getBoolean("cancle", false) ?: false
    }
}