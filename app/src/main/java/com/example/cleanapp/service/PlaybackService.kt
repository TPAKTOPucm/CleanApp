package com.example.cleanapp.service

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import com.example.cleanapp.R
import com.example.cleanapp.widget.MusicWidget
import com.example.cleanapp.widget.MusicWidgetActions
import com.example.cleanapp.widget.MusicWidgetState
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository
import com.example.domain.usecase.ToggleLikeUseCase
import java.io.ByteArrayOutputStream

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private val listRepository: ListRepository by inject()
    private val imageLoader: ImageLoader by inject()
    private val toggleLikeUseCase: ToggleLikeUseCase by inject()

    private var catsList: List<ListElementEntity> = emptyList()

    override fun onCreate() {
        super.onCreate()
        // 1. Инициализируем ExoPlayer
        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL

        // 2. Добавляем слушатель событий плеера
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidgetState(isPlaying = isPlaying)
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // При автоматическом переключении трека (трек закончился)
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    if (catsList.isNotEmpty()) {
                        val nextCat = findNextCatInList()
                        if (nextCat != null) {
                            val nextIndex = catsList.indexOf(nextCat)
                            currentSelectedCatId = nextCat.id
                            currentSelectedCatIndex = nextIndex
                            
                            // Обновляем MediaMetadata
                            CoroutineScope(Dispatchers.Main).launch {
                                updateMediaMetadataForSelectedCat(nextCat.id)
                            }
                            // Обновляем виджет
                            updateWidgetWithCurrentCat()
                        }
                    }
                }
            }
        })

        // Загружаем котиков и создаем треки с обложками
        CoroutineScope(Dispatchers.Main).launch {
            try {
                catsList = listRepository.getElements().first()
                
                if (catsList.size >= 2) {
                    val track_1 = createMediaItemWithArtwork(catsList[0], R.raw.track_1)
                    val track_2 = createMediaItemWithArtwork(catsList[1], R.raw.track_2)
                    
                    player.setMediaItems(listOf(track_1, track_2))
                    player.prepare()

                    currentSelectedCatId = catsList[0].id
                    currentSelectedCatIndex = 0
                    updateWidgetWithCurrentCat()
                } else {
                    val track_1 = MediaItem.fromUri("android.resource://$packageName/${R.raw.track_1}")
                    val track_2 = MediaItem.fromUri("android.resource://$packageName/${R.raw.track_2}")
                    player.setMediaItems(listOf(track_1, track_2))
                    player.prepare()
                }
            } catch (e: Exception) {
                val track_1 = MediaItem.fromUri("android.resource://$packageName/${R.raw.track_1}")
                val track_2 = MediaItem.fromUri("android.resource://$packageName/${R.raw.track_2}")
                player.setMediaItems(listOf(track_1, track_2))
                player.prepare()
            }
        }

        // Создаем MediaSession с переопределенными Next/Prev
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                override fun onPlayerCommandRequest(
                    session: MediaSession,
                    controller: androidx.media3.session.MediaSession.ControllerInfo,
                    playerCommand: Int
                ): Int {
                    when (playerCommand) {
                        Player.COMMAND_SEEK_TO_NEXT -> {
                            if (catsList.isNotEmpty()) {
                                val nextCat = findNextCatInList()
                                if (nextCat != null) {
                                    val nextIndex = catsList.indexOf(nextCat)
                                    currentSelectedCatId = nextCat.id
                                    currentSelectedCatIndex = nextIndex

                                    val targetTrack = nextIndex % 2
                                    if (player.currentMediaItemIndex != targetTrack) {
                                        player.seekToDefaultPosition(targetTrack)
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {
                                        updateMediaMetadataForSelectedCat(nextCat.id)
                                    }
                                    updateWidgetWithCurrentCat()
                                }
                            }
                            return Player.COMMAND_INVALID
                        }
                        Player.COMMAND_SEEK_TO_PREVIOUS -> {
                            // Переопределяем Prev - листаем ВСЕ элементы подряд назад
                            if (catsList.isNotEmpty()) {
                                val prevCat = findPreviousCatInList()
                                if (prevCat != null) {
                                    val prevIndex = catsList.indexOf(prevCat)
                                    currentSelectedCatId = prevCat.id
                                    currentSelectedCatIndex = prevIndex

                                    val targetTrack = prevIndex % 2
                                    if (player.currentMediaItemIndex != targetTrack) {
                                        player.seekToDefaultPosition(targetTrack)
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {
                                        updateMediaMetadataForSelectedCat(prevCat.id)
                                    }
                                    updateWidgetWithCurrentCat()
                                }
                            }
                            return Player.COMMAND_INVALID
                        }
                    }
                    return super.onPlayerCommandRequest(session, controller, playerCommand)
                }
            })
            .build()
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

    // Создание MediaItem с обложкой котика
    private suspend fun createMediaItemWithArtwork(
        cat: ListElementEntity,
        audioResId: Int
    ): MediaItem = withContext(Dispatchers.IO) {
        // Загружаем Bitmap из URL через Coil
        val bitmap = loadBitmapFromUrl(cat.image)
        
        // Создаем MediaMetadata с обложкой
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(cat.title)
            .setArtist("Meowify")
            .setArtworkUri(android.net.Uri.parse(cat.image))

        bitmap?.let {
            metadataBuilder.setArtworkData(
                bitmapToByteArray(it),
                MediaMetadata.PICTURE_TYPE_FRONT_COVER
            )
        }
        
        MediaItem.Builder()
            .setUri("android.resource://$packageName/$audioResId")
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }
    
    // Загрузка Bitmap через Coil
    private suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false) // Нужен software bitmap для MediaMetadata
                .build()
            
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else null
        } catch (e: Exception) {
            Log.e("PlaybackService", "Failed to load bitmap", e)
            null
        }
    }
    
    // Конвертация Bitmap в ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    
    // Кэшируем ID и индекс текущего выбранного кота
    private var currentSelectedCatId: String? = null
    private var currentSelectedCatIndex: Int = -1
    
    // Находим следующего кота в списке (циклически, все подряд)
    private fun findNextCatInList(): ListElementEntity? {
        if (catsList.isEmpty()) return null

        if (currentSelectedCatIndex >= 0) {
            val nextIndex = (currentSelectedCatIndex + 1) % catsList.size
            return catsList[nextIndex]
        }

        return catsList.first()
    }
    
    // TНаходим предыдущего кота в списке (циклически, все подряд)
    private fun findPreviousCatInList(): ListElementEntity? {
        if (catsList.isEmpty()) return null
        
        // Если есть выбранный кот, находим предыдущего
        if (currentSelectedCatIndex >= 0) {
            val prevIndex = if (currentSelectedCatIndex > 0) {
                currentSelectedCatIndex - 1
            } else {
                catsList.size - 1 // Циклически к последнему
            }
            return catsList[prevIndex]
        }
        
        // Иначе возвращаем последнего
        return catsList.last()
    }
    
    // Обновление виджета с данными выбранного кота
    private fun updateWidgetWithCurrentCat() {
        if (catsList.isEmpty()) return
        
        // Берем кота по ID (если выбран) или по текущему треку
        val currentCat = if (currentSelectedCatId != null) {
            catsList.find { it.id == currentSelectedCatId }
        } else {
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < catsList.size) catsList[currentIndex] else null
        } ?: return
        
        // Отправляем Broadcast для синхронизации MainActivity
        val intent = Intent("com.example.cleanapp.CAT_SELECTED").apply {
            setPackage(packageName) // Explicit intent для безопасности
            putExtra("CAT_ID", currentCat.id)
            putExtra("CAT_INDEX", catsList.indexOf(currentCat))
        }
        sendBroadcast(intent)
        
        CoroutineScope(Dispatchers.IO).launch {
            // Сохраняем картинку котика в файл для виджета
            val bitmap = loadBitmapFromUrl(currentCat.image)
            val imagePath = bitmap?.let { saveBitmapToCache(it, currentCat.id) }
            
            updateWidgetState(
                trackTitle = currentCat.title,
                imageUrl = imagePath ?: "",
                isLiked = currentCat.like,
                catId = currentCat.id
            )
        }
    }
    
    // Сохранение Bitmap в кэш для виджета
    private fun saveBitmapToCache(bitmap: Bitmap, catId: String): String {
        val cacheDir = applicationContext.cacheDir
        val imageFile = java.io.File(cacheDir, "widget_cat_$catId.png")
        
        java.io.FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return imageFile.absolutePath
    }
    
    // Обновление MediaMetadata уведомления для выбранного кота
    private suspend fun updateMediaMetadataForSelectedCat(catId: String) = withContext(Dispatchers.IO) {
        val cat = catsList.find { it.id == catId } ?: return@withContext
        
        // Загружаем Bitmap
        val bitmap = loadBitmapFromUrl(cat.image)
        
        // Создаем новый MediaMetadata
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(cat.title)
            .setArtist("Meowify")
            .setArtworkUri(android.net.Uri.parse(cat.image))
        
        bitmap?.let {
            metadataBuilder.setArtworkData(
                bitmapToByteArray(it),
                MediaMetadata.PICTURE_TYPE_FRONT_COVER
            )
        }
        
        // Обновляем метаданные текущего трека
        withContext(Dispatchers.Main) {
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < player.mediaItemCount) {
                val currentItem = player.getMediaItemAt(currentIndex)
                val newItem = currentItem.buildUpon()
                    .setMediaMetadata(metadataBuilder.build())
                    .build()
                
                // Заменяем текущий MediaItem с сохранением позиции
                val wasPlaying = player.isPlaying
                val position = player.currentPosition
                
                player.replaceMediaItem(currentIndex, newItem)
                player.seekTo(currentIndex, position)
                player.playWhenReady = wasPlaying
            }
        }
    }

    // Обновление DataStore виджета
    private fun updateWidgetState(
        isPlaying: Boolean? = null,
        trackTitle: String? = null,
        imageUrl: String? = null,
        isLiked: Boolean? = null,
        catId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val context = applicationContext
            val manager = GlanceAppWidgetManager(context)
            val widgetIds = manager.getGlanceIds(MusicWidget::class.java)
            if (widgetIds.isEmpty()) return@launch

            // Обновляем состояние каждого активного виджета
            widgetIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    isPlaying?.let { prefs[MusicWidgetState.isPlayingKey] = it }
                    trackTitle?.let { prefs[MusicWidgetState.trackTitleKey] = it }
                    // Сохраняем данные текущего кота
                    imageUrl?.let { prefs[MusicWidgetState.imageUrlKey] = it }
                    isLiked?.let { prefs[MusicWidgetState.isLikedKey] = it }
                    catId?.let { prefs[MusicWidgetState.catIdKey] = it }
                }
                MusicWidget().update(context, glanceId)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Обрабатываем наши кастомные команды от Виджета
        when (intent?.action) {
            MusicWidgetActions.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            MusicWidgetActions.ACTION_NEXT_TRACK -> {
                // Next листает ВСЕ элементы подряд
                if (catsList.isNotEmpty()) {
                    val nextCat = findNextCatInList()
                    if (nextCat != null) {
                        val nextIndex = catsList.indexOf(nextCat)
                        currentSelectedCatId = nextCat.id
                        currentSelectedCatIndex = nextIndex
                        
                        // Переключаем трек если нужно (по четности индекса)
                        val targetTrack = nextIndex % 2
                        if (player.currentMediaItemIndex != targetTrack) {
                            player.seekToDefaultPosition(targetTrack)
                        }
                        
                        // Обновляем MediaMetadata
                        CoroutineScope(Dispatchers.Main).launch {
                            updateMediaMetadataForSelectedCat(nextCat.id)
                        }
                        // Обновляем виджет
                        updateWidgetWithCurrentCat()
                    }
                }
            }
            // Переключаем лайк выбранного кота
            MusicWidgetActions.ACTION_TOGGLE_LIKE -> {
                val catId = currentSelectedCatId
                if (catId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // Переключаем лайк в БД
                        toggleLikeUseCase.executeSuspend(catId)
                        // Перезагружаем список котиков с обновленными лайками
                        catsList = listRepository.getElements().first()
                        // Обновляем виджет с новым статусом лайка
                        updateWidgetWithCurrentCat()
                    }
                }
            }
            // Обновление выбранного кота (от MainActivity)
            "ACTION_SELECT_CAT" -> {
                val catId = intent.getStringExtra("CAT_ID")
                val catIndex = intent.getIntExtra("CAT_INDEX", -1)
                if (catId != null) {
                    currentSelectedCatId = catId
                    currentSelectedCatIndex = catIndex // Сохраняем индекс в списке
                    // Обновляем виджет с новым котом
                    updateWidgetWithCurrentCat()
                    // Обновляем MediaMetadata уведомления с новым котом
                    CoroutineScope(Dispatchers.Main).launch {
                        updateMediaMetadataForSelectedCat(catId)
                    }
                }
            }
            // Обновление лайка (от MainActivity)
            "ACTION_UPDATE_LIKE" -> {
                val catId = intent.getStringExtra("CAT_ID")
                if (catId != null) {
                    // Перезагружаем список котов из БД (с обновленными лайками)
                    CoroutineScope(Dispatchers.IO).launch {
                        catsList = listRepository.getElements().first()
                        // Если это выбранный кот, обновляем виджет
                        if (currentSelectedCatId == catId) {
                            updateWidgetWithCurrentCat()
                        }
                    }
                }
            }
        }

        // ВАЖНО: Обязательно вызываем super, чтобы Media3 мог обработать
        // свои стандартные кнопки уведомлений и жизненный цикл сервиса
        return super.onStartCommand(intent, flags, startId)
    }
}

