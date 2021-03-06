package com.msub.selfcheck.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.msub.selfcheck.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup

class PushFragment : Fragment() {

    var isconnect=true
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val networkCallBack=object:ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isconnect=true
        }
        override fun onLost(network: Network) {
            isconnect=false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.push, container, false)
        val local=root.findViewById<Spinner>(R.id.local)
        val sctype=root.findViewById<Spinner>(R.id.sctype)
        val scname=root.findViewById<EditText>(R.id.scname)
        val name=root.findViewById<EditText>(R.id.name)
        val birth=root.findViewById<EditText>(R.id.birth)
        val pass=root.findViewById<EditText>(R.id.pass)
        val btn=root.findViewById<Button>(R.id.pushbtn)
        btn.setOnClickListener {
            start(local.selectedItem.toString(), sctype.selectedItem.toString(), scname.text.toString(), name.text.toString(), birth.text.toString(), pass.text.toString())
        }
        return root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val connectivityManager = activity?.getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallBack)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStop() {
        super.onStop()
        val connectivityManager = activity?.getSystemService(ConnectivityManager::class.java)
        connectivityManager?.unregisterNetworkCallback(networkCallBack)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun start(local:String, sctype:String, scname:String, name:String, birth:String, pass:String) {
        if(!isconnect) {
            AlertDialog.Builder(activity)
                    .setTitle("??????")
                    .setMessage("??????????????? ??????????????????.")
                    .setPositiveButton(
                            "??????",
                            DialogInterface.OnClickListener { dialogInterface, id -> {} })
                    .create()
                    .show()
        } else {
            val progressdialog=ProgressDialog(activity).apply {
                setMessage("?????? ????????? ?????? ???????????? ??????????????????...")
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(false)
                show()
            }
            val gubun=mapOf(
                    "?????????" to "1",
                    "????????????" to "2",
                    "?????????" to "3",
                    "????????????" to "4",
                    "????????????" to "5"
            )
            val region=mapOf(
                    "??????" to "01",
                    "??????" to "02",
                    "??????" to "03",
                    "??????" to "04",
                    "??????" to "05",
                    "??????" to "06",
                    "??????" to "07",
                    "??????" to "08",
                    "??????" to "10",
                    "??????" to "11",
                    "??????" to "12",
                    "??????" to "13",
                    "??????" to "14",
                    "??????" to "15",
                    "??????" to "16",
                    "??????" to "17",
                    "??????" to "18"
            )
            val user=getData()
            CoroutineScope(Dispatchers.Main).launch {
                val result=async(Dispatchers.Default) {
                    var schoolName="?????? ?????? ??????."
                    if(scname!="") {
                        try {
                            val school = JSONObject(Jsoup.connect("https://hcs.eduro.go.kr/v2/searchSchool?lctnScCode=${region[local]}&schulCrseScCode=${gubun[sctype]}&orgName=$scname&loginType=school")
                                    .ignoreContentType(true)
                                    .get()
                                    .text()).getJSONArray("schulList")
                            schoolName = if (school.length() == 0) "?????? ?????? ??????." else school.getJSONObject(0).getString("kraOrgNm")
                        } catch(e:Exception) {
                            schoolName="?????? ?????? ?????? ??????."
                        }
                    }
                    if("https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass" in user) arrayOf("{'status':-1, 'message':'????????? ?????? ?????? ?????????.'}", "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                    else if(schoolName=="?????? ?????? ?????? ??????.") arrayOf("{'status':-1, 'message':'??????????????? ??????????????? ??????????????????.'}", "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                    else arrayOf(Jsoup.connect("https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass").ignoreContentType(true).get().text(), "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                }.await()

                val uri=result[1]
                val json=JSONObject(result[0])
                progressdialog.dismiss()
                if(json.getInt("status")!=0) {
                    AlertDialog.Builder(activity)
                            .setTitle("??????")
                            .setMessage(json.getString("message"))
                            .setPositiveButton("??????", DialogInterface.OnClickListener { dialogInterface, id -> {}})
                            .create()
                            .show()
                } else {
                        activity?.getSharedPreferences(
                                "info",
                                Activity.MODE_PRIVATE
                        )!!.edit().run {
                            putString("users", "$user$uri\n")
                            commit()
                        }
                        AlertDialog.Builder(activity)
                                .setTitle("??????")
                                .setMessage("????????? ?????? ???????????????.")
                                .setPositiveButton(
                                        "??????",
                                        DialogInterface.OnClickListener { dialogInterface, id -> {} })
                                .create()
                                .show()
                }
            }
        }
    }

    fun getData():String {
        return activity?.getSharedPreferences("info", Activity.MODE_PRIVATE)?.getString("users", "") ?: ""
    }
}