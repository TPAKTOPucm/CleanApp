package com.example.cleanapp.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.example.cleanapp.R
import com.example.cleanapp.widget.MusicWidget
import com.example.cleanapp.widget.MusicWidgetActions
import com.example.cleanapp.widget.MusicWidgetState

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        // 1. Инициализируем ExoPlayer
        player = ExoPlayer.Builder(this).build()
        // Включаем бесконечный повтор всего списка
        player.repeatMode = Player.REPEAT_MODE_ALL

        // 2. Добавляем слушатель событий плеера для обновления виджета
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Когда меняется статус (играет/пауза) -> обновляем виджет
                updateWidgetState(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Когда меняется трек -> обновляем заголовок в виджете
                // В реальном приложении берем данные из mediaItem.mediaMetadata
                val trackName = "Track " + (player.currentMediaItemIndex + 1)
                updateWidgetState(trackTitle = trackName)
            }
        })

        // 3. Загружаем треки в плейлист
        val track1 =
            MediaItem.fromUri("android.resource://$packageName/${R.raw.track_1}")
        val track2 =
            MediaItem.fromUri("android.resource://$packageName/${R.raw.track_2}")
        player.setMediaItems(listOf(track1, track2))
        player.prepare()

        // 4. Создаем MediaSession. Это "сердце" интеграции с Android.
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // Метод обязателен для MediaSessionService. Возвращает сессию контроллерам.
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Освобождаем ресурсы при убийстве сервиса
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    // Вспомогательный метод для обновления DataStore виджета (из Лаб. 9)
    private fun updateWidgetState(isPlaying: Boolean? = null, trackTitle: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val context = applicationContext
            val manager = GlanceAppWidgetManager(context)
            val widgetIds =
                manager.getGlanceIds(MusicWidget::class.java)

            if (widgetIds.isEmpty()) return@launch

            // Обновляем состояние каждого активного виджета
            widgetIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    isPlaying?.let {
                        prefs[MusicWidgetState.isPlayingKey] = it
                    }
                    trackTitle?.let {
                        prefs[MusicWidgetState.trackTitleKey] = it
                    }
                }
                MusicWidget().update(context, glanceId)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Обрабатываем наши кастомные команды от Виджета
        when(intent?.action) {
            MusicWidgetActions.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            MusicWidgetActions.ACTION_NEXT_TRACK -> {
                player.seekToNext()
            }
        }

        // ВАЖНО: Обязательно вызываем super, чтобы Media3 мог обработать
        // свои стандартные кнопки уведомлений и жизненный цикл сервиса
        return super.onStartCommand(intent, flags, startId)
    }
}