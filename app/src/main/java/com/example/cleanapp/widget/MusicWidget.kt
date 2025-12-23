package com.example.cleanapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.cleanapp.R

class MusicWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val trackTitle = prefs[MusicWidgetState.trackTitleKey] ?: "Выберите трек"
                val isPlaying = prefs[MusicWidgetState.isPlayingKey] ?: false
                val isLiked = prefs[MusicWidgetState.isLikedKey] ?: false
                val imagePath = prefs[MusicWidgetState.imageUrlKey]

                MusicWidgetContent(
                    trackTitle = trackTitle,
                    isPlaying = isPlaying,
                    isLiked = isLiked,
                    imagePath = imagePath
                )
            }
        }
    }

    @Composable
    private fun MusicWidgetContent(
        trackTitle: String,
        isPlaying: Boolean,
        isLiked: Boolean,
        imagePath: String?
    ) {
        val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val likeIcon = if (isLiked) R.drawable.ic_favorite else R.drawable.ic_favorite_border

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.White)
                .cornerRadius(16.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Картинка котика в круге!
            if (imagePath != null && imagePath.isNotEmpty()) {
                val imageFile = java.io.File(imagePath)
                if (imageFile.exists()) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(imagePath)
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Cat Album Art",
                        modifier = GlanceModifier
                            .size(64.dp)
                            .cornerRadius(32.dp),
                        contentScale = androidx.glance.layout.ContentScale.Crop
                    )
                } else {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Album Art",
                        modifier = GlanceModifier
                            .size(64.dp)
                            .cornerRadius(32.dp)
                    )
                }
            } else {
                Image(
                    provider = ImageProvider(R.drawable.ic_launcher_foreground),
                    contentDescription = "Album Art",
                    modifier = GlanceModifier
                        .size(64.dp)
                        .cornerRadius(32.dp)
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = trackTitle,
                    style = TextStyle(color = ColorProvider(Color.Black))
                )
                Spacer(GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.End,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(playPauseIcon),
                        contentDescription = "Play/Pause",
                        modifier = GlanceModifier
                            .size(36.dp)
                            .clickable(onClick = actionRunCallback<PlayPauseActionCallback>()),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.Black))
                    )
                    Image(
                        provider = ImageProvider(R.drawable.ic_skip_next),
                        contentDescription = "Next",
                        modifier = GlanceModifier
                            .size(36.dp)
                            .clickable(onClick = actionRunCallback<NextTrackActionCallback>()),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.Black))
                    )
                    Image(
                        provider = ImageProvider(likeIcon),
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        modifier = GlanceModifier
                            .size(36.dp)
                            .clickable(onClick = actionRunCallback<ToggleLikeActionCallback>()),
                        colorFilter = androidx.glance.ColorFilter.tint(
                            ColorProvider(if (isLiked) Color.Red else Color.Gray)
                        )
                    )
                }
            }
        }
    }
}

