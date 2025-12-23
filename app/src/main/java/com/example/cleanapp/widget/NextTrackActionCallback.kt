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
        // Создаем Intent с командой "Следующий трек"
        val intent = Intent(context, MusicWidgetReceiver::class.java).apply {
            action = MusicWidgetActions.ACTION_NEXT_TRACK
        }
        // Отправляем системное широковещательное сообщение,
        // которое будет "услышано" нашим Foreground Service в следующей работе.
        context.sendBroadcast(intent)

        // Примечание: Мы не обновляем состояние виджета здесь,
        // так как не знаем заранее, каким будет следующий трек.
        // Эту логику должен будет взять на себя Foreground Service,
        // который, получив эту команду, обновит виджет с новыми данными.
    }
}

