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
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIsPlaying = prefs[MusicWidgetState.isPlayingKey] ?: false
            prefs[MusicWidgetState.isPlayingKey] = !currentIsPlaying
        }
        MusicWidget().update(context, glanceId)

        val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
            action = MusicWidgetActions.ACTION_PLAY_PAUSE
        }
        context.sendBroadcast(intent)
    }
}

