package com.msub.selfcheck

import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timer

class MyService : Service() {

    var start = false
    var links = mutableListOf<String>()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        start = true
        timer(period = 4000) {
            if (start) {
                (getSystemService(POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My:notify")
                }.acquire(4000)
                val formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm;ss"))
                val time = applicationContext.getSharedPreferences("info", Activity.MODE_PRIVATE).getString("time", "")?.split(":")?.map {
                    if(it.length==1) "0"+it else it
                }?.joinToString(separator=":")
                val cancle = applicationContext.getSharedPreferences("info", Activity.MODE_PRIVATE).getBoolean("cancle", false)
                val week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (time == formatted.split(";")[0] && formatted.split(";")[1] in listOf("00", "01", "02", "03")) {
                    if (!(cancle && week in listOf(1, 7))) {
                        var list = applicationContext.getSharedPreferences("info", Activity.MODE_PRIVATE).getString("users", "")?.split("\n")
                        links = mutableListOf<String>()
                        list = list?.subList(0, list.size - 1)
                        for (i in list!!) {
                            start(i)
                        }
                    } else {
                        val notificon = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        var notifi = NotificationCompat.Builder(applicationContext)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channel = NotificationChannel("1", "자가진단 알림", NotificationManager.IMPORTANCE_HIGH)
                            notificon.createNotificationChannel(channel)
                            notifi = NotificationCompat.Builder(applicationContext, "1")
                        }
                        notifi
                                .setContentTitle("자가진단 미완료")
                                .setContentText("토/일요일 자가진단 하지 않기 설정으로 인해 자가진단을 완료하지 않았습니다.")
                                .setStyle(NotificationCompat.BigTextStyle().bigText("토/일요일 자가진단 하지 않기 설정으로 인해 자가진단을 완료하지 않았습니다."))
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setAutoCancel(true)
                        notificon.notify(0, notifi.build())
                    }
                }
            }
        }
        initializeNotification()
        return START_NOT_STICKY
    }

    fun initializeNotification() {
        val builder = NotificationCompat.Builder(this, "0")
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("동작중...")
                .setContentTitle("자동 자가진단")
                .setOngoing(true)
                .setWhen(0)
                .setShowWhen(false)
        builder.setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel("0", "동작중 알림", NotificationManager.IMPORTANCE_NONE))
        startForeground(1, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        super.onDestroy()
        start = false
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun start(uri: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = async(Dispatchers.Default) {
                try {
                    listOf(Jsoup.connect(uri).ignoreContentType(true).get().text(), uri)
                } catch(e:Exception) {
                    listOf("jsoup error!", "error")
                }
            }.await()
            if(result[0] !="jsoup error!") {
                if (JSONObject(result[0]).getInt("status") == 0) links.add(result[1].split("=")[4].split("&")[0] + "(" + result[1].split("birth=")[1].split("&")[0] + ")-" + result[1].split("scname=")[1].split("&")[0])
                newnotifi()
            } else {
                val notificon = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                var notifi = NotificationCompat.Builder(applicationContext)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("1", "자가진단 알림", NotificationManager.IMPORTANCE_HIGH)
                    notificon.createNotificationChannel(channel)
                    notifi = NotificationCompat.Builder(applicationContext, "1")
                }
                notifi
                        .setContentTitle("자가진단 미완료")
                        .setContentText("네트워크 연결을 확인해주세요.")
                        .setStyle(NotificationCompat.BigTextStyle().bigText("네트워크 연결을 확인해주세요."))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                notificon.notify(0, notifi.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun newnotifi() {
        val notificon = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var notifi = NotificationCompat.Builder(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("1", "자가진단 알림", NotificationManager.IMPORTANCE_HIGH)
            notificon.createNotificationChannel(channel)
            notifi = NotificationCompat.Builder(this, "1")
        }
        notifi
                .setContentTitle("자가진단 완료")
                .setContentText(java.lang.String.join("\n", links))
                .setStyle(NotificationCompat.BigTextStyle().bigText(java.lang.String.join("\n", links)))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
        notificon.notify(0, notifi.build())
    }
}