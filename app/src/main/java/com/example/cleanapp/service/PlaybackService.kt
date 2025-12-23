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
        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidgetState(isPlaying = isPlaying)
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    if (catsList.isNotEmpty()) {
                        val nextCat = findNextCatInList()
                        if (nextCat != null) {
                            val nextIndex = catsList.indexOf(nextCat)
                            currentSelectedCatId = nextCat.id
                            currentSelectedCatIndex = nextIndex

                            CoroutineScope(Dispatchers.Main).launch {
                                updateMediaMetadataForSelectedCat(nextCat.id)
                            }
                            updateWidgetWithCurrentCat()
                        }
                    }
                }
            }
        })

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

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private suspend fun createMediaItemWithArtwork(
        cat: ListElementEntity,
        audioResId: Int
    ): MediaItem = withContext(Dispatchers.IO) {
        val bitmap = loadBitmapFromUrl(cat.image)

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

    private suspend fun loadBitmapFromUrl(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false)
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

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private var currentSelectedCatId: String? = null
    private var currentSelectedCatIndex: Int = -1

    private fun findNextCatInList(): ListElementEntity? {
        if (catsList.isEmpty()) return null

        if (currentSelectedCatIndex >= 0) {
            val nextIndex = (currentSelectedCatIndex + 1) % catsList.size
            return catsList[nextIndex]
        }

        return catsList.first()
    }

    private fun findPreviousCatInList(): ListElementEntity? {
        if (catsList.isEmpty()) return null

        if (currentSelectedCatIndex >= 0) {
            val prevIndex = if (currentSelectedCatIndex > 0) {
                currentSelectedCatIndex - 1
            } else {
                catsList.size - 1
            }
            return catsList[prevIndex]
        }

        return catsList.last()
    }

    private fun updateWidgetWithCurrentCat() {
        if (catsList.isEmpty()) return

        val currentCat = if (currentSelectedCatId != null) {
            catsList.find { it.id == currentSelectedCatId }
        } else {
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < catsList.size) catsList[currentIndex] else null
        } ?: return

        val intent = Intent("com.example.cleanapp.CAT_SELECTED").apply {
            setPackage(packageName)
            putExtra("CAT_ID", currentCat.id)
            putExtra("CAT_INDEX", catsList.indexOf(currentCat))
        }
        sendBroadcast(intent)
        
        CoroutineScope(Dispatchers.IO).launch {
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

    private fun saveBitmapToCache(bitmap: Bitmap, catId: String): String {
        val cacheDir = applicationContext.cacheDir
        val imageFile = java.io.File(cacheDir, "widget_cat_$catId.png")
        
        java.io.FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return imageFile.absolutePath
    }

    private suspend fun updateMediaMetadataForSelectedCat(catId: String) = withContext(Dispatchers.IO) {
        val cat = catsList.find { it.id == catId } ?: return@withContext

        val bitmap = loadBitmapFromUrl(cat.image)

        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(cat.title)
            .setArtworkUri(android.net.Uri.parse(cat.image))
        
        bitmap?.let {
            metadataBuilder.setArtworkData(
                bitmapToByteArray(it),
                MediaMetadata.PICTURE_TYPE_FRONT_COVER
            )
        }

        withContext(Dispatchers.Main) {
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < player.mediaItemCount) {
                val currentItem = player.getMediaItemAt(currentIndex)
                val newItem = currentItem.buildUpon()
                    .setMediaMetadata(metadataBuilder.build())
                    .build()

                val wasPlaying = player.isPlaying
                val position = player.currentPosition
                
                player.replaceMediaItem(currentIndex, newItem)
                player.seekTo(currentIndex, position)
                player.playWhenReady = wasPlaying
            }
        }
    }

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

            widgetIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    isPlaying?.let { prefs[MusicWidgetState.isPlayingKey] = it }
                    trackTitle?.let { prefs[MusicWidgetState.trackTitleKey] = it }
                    imageUrl?.let { prefs[MusicWidgetState.imageUrlKey] = it }
                    isLiked?.let { prefs[MusicWidgetState.isLikedKey] = it }
                    catId?.let { prefs[MusicWidgetState.catIdKey] = it }
                }
                MusicWidget().update(context, glanceId)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            MusicWidgetActions.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            MusicWidgetActions.ACTION_NEXT_TRACK -> {
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
            }
            MusicWidgetActions.ACTION_TOGGLE_LIKE -> {
                val catId = currentSelectedCatId
                if (catId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        toggleLikeUseCase.executeSuspend(catId)
                        catsList = listRepository.getElements().first()
                        updateWidgetWithCurrentCat()
                    }
                }
            }
            "ACTION_SELECT_CAT" -> {
                val catId = intent.getStringExtra("CAT_ID")
                val catIndex = intent.getIntExtra("CAT_INDEX", -1)
                if (catId != null) {
                    currentSelectedCatId = catId
                    currentSelectedCatIndex = catIndex
                    updateWidgetWithCurrentCat()
                    CoroutineScope(Dispatchers.Main).launch {
                        updateMediaMetadataForSelectedCat(catId)
                    }
                }
            }
            "ACTION_UPDATE_LIKE" -> {
                val catId = intent.getStringExtra("CAT_ID")
                if (catId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        catsList = listRepository.getElements().first()
                        if (currentSelectedCatId == catId) {
                            updateWidgetWithCurrentCat()
                        }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}

