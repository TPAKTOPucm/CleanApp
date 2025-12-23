package com.example.cleanapp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState

class PlayPauseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 1. Обновляем состояние виджета в DataStore
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIsPlaying = prefs[MusicWidgetState.isPlayingKey] ?: false
            prefs[MusicWidgetState.isPlayingKey] = !currentIsPlaying
        }
        // Запускаем перерисовку с новым состоянием
        MusicWidget().update(context, glanceId)

        // 2. Отправляем Broadcast для внешних слушателей (например, сервиса)
        val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
            action = MusicWidgetActions.ACTION_PLAY_PAUSE
        }
        context.sendBroadcast(intent)
    }
}