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
                    .setTitle("실패")
                    .setMessage("네트워크에 연결해주세요.")
                    .setPositiveButton(
                            "확인",
                            DialogInterface.OnClickListener { dialogInterface, id -> {} })
                    .create()
                    .show()
        } else {
            val progressdialog=ProgressDialog(activity).apply {
                setMessage("정보 확인을 위해 자가진단 진행중입니다...")
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setCancelable(false)
                show()
            }
            val gubun=mapOf(
                    "유치원" to "1",
                    "초등학교" to "2",
                    "중학교" to "3",
                    "고등학교" to "4",
                    "특수학교" to "5"
            )
            val region=mapOf(
                    "서울" to "01",
                    "부산" to "02",
                    "대구" to "03",
                    "인천" to "04",
                    "광주" to "05",
                    "대전" to "06",
                    "울산" to "07",
                    "세종" to "08",
                    "경기" to "10",
                    "강원" to "11",
                    "충북" to "12",
                    "충남" to "13",
                    "전북" to "14",
                    "전남" to "15",
                    "경북" to "16",
                    "경남" to "17",
                    "제주" to "18"
            )
            val user=getData()
            CoroutineScope(Dispatchers.Main).launch {
                val result=async(Dispatchers.Default) {
                    var schoolName="학교 정보 없음."
                    if(scname!="") {
                        try {
                            val school = JSONObject(Jsoup.connect("https://hcs.eduro.go.kr/v2/searchSchool?lctnScCode=${region[local]}&schulCrseScCode=${gubun[sctype]}&orgName=$scname&loginType=school")
                                    .ignoreContentType(true)
                                    .get()
                                    .text()).getJSONArray("schulList")
                            schoolName = if (school.length() == 0) "학교 정보 없음." else school.getJSONObject(0).getString("kraOrgNm")
                        } catch(e:Exception) {
                            schoolName="서버 점검 확인 요망."
                        }
                    }
                    if("https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass" in user) arrayOf("{'status':-1, 'message':'유저가 이미 존재 합니다.'}", "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                    else if(schoolName=="서버 점검 확인 요망.") arrayOf("{'status':-1, 'message':'자가진단이 점검중인지 확인해주세요.'}", "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                    else arrayOf(Jsoup.connect("https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass").ignoreContentType(true).get().text(), "https://api.self-check.msub.kr/?local=$local&sctype=$sctype&scname=$schoolName&name=$name&birth=$birth&pass=$pass")
                }.await()

                val uri=result[1]
                val json=JSONObject(result[0])
                progressdialog.dismiss()
                if(json.getInt("status")!=0) {
                    AlertDialog.Builder(activity)
                            .setTitle("오류")
                            .setMessage(json.getString("message"))
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, id -> {}})
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
                                .setTitle("완료")
                                .setMessage("추가가 완료 되었습니다.")
                                .setPositiveButton(
                                        "확인",
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