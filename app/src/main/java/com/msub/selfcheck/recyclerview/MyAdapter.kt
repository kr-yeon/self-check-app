package com.msub.selfcheck.recyclerview

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.msub.selfcheck.R

class MyAdapter(val datas:List<String>, val activity: Activity?, val recyclerv:RecyclerView):RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val lists=datas.map {
            val info=it.split("=").subList(1, it.split("=").size).map {
                it.split("&")[0]
            }
            "${info[3]}(${info[4]})-${info[0]}\n학교:${info[2]} 비밀번호:${info[5]}"
        }
        holder.info.text=lists[position]
        holder.delbtn.setOnClickListener {
            val sp=activity?.getSharedPreferences("info", Activity.MODE_PRIVATE) as SharedPreferences
            val editor=sp.edit() as SharedPreferences.Editor
            getData()
            editor.putString("users", getData().replace(datas[position]+"\n", ""))
            editor.commit()
            AlertDialog.Builder(activity)
                .setTitle("완료")
                .setMessage("삭제가 완료 되었습니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, id -> {}})
                .create()
                .show()
            val datas=getData()
            if(datas!="") {
                recyclerv.adapter=MyAdapter(datas.split("\n").subList(0, datas.split("\n").size-1), activity, recyclerv)
            } else {
                recyclerv.adapter=MyAdapter(listOf(), activity, recyclerv)
            }
            recyclerv.layoutManager= LinearLayoutManager(activity)
        }
    }

    fun getData():String {
        val sp=activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)
        if(sp != null) {
            return sp.getString("users", "").toString()
        }
        return ""
    }

}