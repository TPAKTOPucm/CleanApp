package com.example.cleanapp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class NextTrackActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
            action = MusicWidgetActions.ACTION_NEXT_TRACK
        }
        context.sendBroadcast(intent)

    }
}

