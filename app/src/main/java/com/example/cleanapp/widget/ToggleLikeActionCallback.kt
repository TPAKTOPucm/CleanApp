package com.example.cleanapp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * кликабельный лайк на виджете
 */
class ToggleLikeActionCallback : ActionCallback {
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Отправляем Broadcast, который передастся в PlaybackService
        // Сервис знает какой кот сейчас играет и переключит его лайк
        val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
            action = MusicWidgetActions.ACTION_TOGGLE_LIKE
        }
        context.sendBroadcast(intent)
    }
}

