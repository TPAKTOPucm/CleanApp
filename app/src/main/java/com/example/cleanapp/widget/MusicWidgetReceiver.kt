package com.example.cleanapp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.cleanapp.service.PlaybackService

class MusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action ?: return

        if (action == MusicWidgetActions.ACTION_PLAY_PAUSE ||
            action == MusicWidgetActions.ACTION_NEXT_TRACK
        ) {
            // Создаем Intent, адресованный нашему сервису
            val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                this.action = action // Передаем action (PLAY_PAUSE или NEXT) дальше
            }
            // Запускаем сервис с этой командой
            // startForegroundService гарантирует доставку, даже если приложение в фоне
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}