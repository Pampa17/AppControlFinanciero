package com.jpdev.appcontrolfinanciero.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jpdev.appcontrolfinanciero.R

private const val CHANNEL_ID = "budget_alerts"

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return // channels don't exist before API 26
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alertas de presupuesto",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    /** Fires only when the caller has just crossed into CRITICO — never from a reactive observer. */
    @SuppressLint("MissingPermission") // permission checked manually right below
    fun fireCriticalAlert(context: Context, monthLabel: String, balance: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return // in-app Crítico banner still covers the visual-alert requirement
        }
        val title = if (monthLabel.isBlank()) "Saldo crítico" else "Saldo crítico · $monthLabel"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("Tu saldo disponible es de $$balance. Estás por agotar tu presupuesto del mes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, notification)
    }
}
