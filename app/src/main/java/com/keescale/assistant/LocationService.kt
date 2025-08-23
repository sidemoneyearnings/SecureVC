package com.keescale.assistant

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 60000 // 1 min

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForeground(1, createNotification())
        handler.post(runnable)
    }

    private val runnable = object : Runnable {
        override fun run() {
            sendLocation()
            handler.postDelayed(this, interval)
        }
    }

    private fun sendLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val json = JSONObject()
                json.put("lat", it.latitude)
                json.put("lng", it.longitude)

                val body = RequestBody.create(json.toString(), "application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url("https://keescale.com/android/locationapi.php")
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) { response.close() }
                })
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "assistant_channel"
        val channel = NotificationChannel(channelId, "Assistant Service", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Assistant Service Running")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}