package com.example.cleanapp.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cleanapp.main.vm.MainState
import com.example.cleanapp.ui.theme.CleanAppTheme
import com.example.domain.entity.ListElementEntity

// MainScreen теперь "глупый" - не знает о ViewModel
@Composable
fun MainScreen(
    state: MainState,
    playerState: com.example.cleanapp.main.vm.PlayerState, // Данные из Плеера
    onPlayPauseClick: (String) -> Unit, // Клик по кнопке Play
    onElementClick: (String) -> Unit,
    onToggleLike: (String) -> Unit // Клик по кнопке Лайк
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is MainState.Content -> ContentState(
                list = state.list,
                selectedCatId = playerState.selectedCatId,
                isPlaying = playerState.isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                onElementClick = onElementClick,
                onToggleLike = onToggleLike
            )
            is MainState.Error -> ErrorState(message = state.message)
            MainState.Loading -> LoadingState()
        }
    }
}

@Composable
fun LoadingState() {
    CircularProgressIndicator()
}

@Composable
fun ErrorState(message: String) {
    Text(text = message, color = Color.Red)
}

// ContentState получает лямбду onElementClick
@Composable
fun ContentState(
    list: List<ListElementEntity>,
    selectedCatId: String?,
    isPlaying: Boolean,
    onPlayPauseClick: (String) -> Unit,
    onElementClick: (String) -> Unit,
    onToggleLike: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        itemsIndexed(list) { index, element ->
            val isSelected = (element.id == selectedCatId)
            val showPauseIcon = isSelected && isPlaying

            ElementRow(
                element = element,
                showPauseIcon = showPauseIcon,
                onPlayClick = { onPlayPauseClick(element.id) },
                onItemClick = { onElementClick(element.id) },
                onToggleLike = { onToggleLike(element.id) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ElementRow(
    element: ListElementEntity,
    showPauseIcon: Boolean,
    onPlayClick: () -> Unit,
    onItemClick: () -> Unit,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp),
        onClick = onItemClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Фиксированная высота для красивого отображения
        ) {
            // Изображение котика на весь фон
            AsyncImage(
                model = element.image,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(
                    id = com.example.cleanapp.R.drawable.ic_launcher_foreground
                ),
                error = androidx.compose.ui.res.painterResource(
                    id = com.example.cleanapp.R.drawable.ic_launcher_foreground
                )
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f), // Темнее
                                Color.Black.copy(alpha = 0.7f)  // Темнее
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = if (showPauseIcon) com.example.cleanapp.R.drawable.ic_pause
                            else com.example.cleanapp.R.drawable.ic_play_arrow
                        ),
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = element.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                androidx.compose.material3.IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(48.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (element.like)
                            androidx.compose.material.icons.Icons.Default.Favorite
                        else
                            androidx.compose.material.icons.Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (element.like)
                            Color.Red
                        else
                           Color.White
                    )
                }
            }
        }
    }
}

// --- ИНСТРУМЕНТЫ ДЛЯ ПРЕВЬЮ ---
private val sampleDataForPreview = listOf(
    ListElementEntity("1", "Cool Cat", "https://cataas.com/cat/says/hello", true),
    ListElementEntity("2", "Serious Cat", "https://cataas.com/cat", false),
    ListElementEntity("3", "Cute Cat", "https://cataas.com/cat/cute", true)
)

@Preview(showBackground = true)
@Composable
fun ContentStatePreview() {
    CleanAppTheme {
        ContentState(
            list = sampleDataForPreview,
            selectedCatId = "1",
            isPlaying = true,
            onPlayPauseClick = {},
            onElementClick = {},
            onToggleLike = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun LoadingStatePreview() {
    CleanAppTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        }
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
fun ErrorStatePreview() {
    CleanAppTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorState(message = "Something went wrong!")
            }
        }
    }
}
