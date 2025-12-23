package com.example.cleanapp.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cleanapp.R
import com.example.cleanapp.main.vm.MainState
import com.example.cleanapp.main.vm.PlayerState
import com.example.cleanapp.ui.theme.CleanAppTheme
import com.example.domain.entity.ListElementEntity

// MainScreen теперь "глупый" - не знает о ViewModel
@Composable
fun MainScreen(
    state: MainState,
    playerState: PlayerState,    // Данные из Плеера
    onPlayPauseClick: (String) -> Unit, // Клик по кнопке Play
    onElementClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is MainState.Content -> ContentState(
                list = state.list,
                currentTrackIndex = playerState.currentTrackIndex,
                isPlaying = playerState.isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                onElementClick = onElementClick
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
    currentTrackIndex: Int,
    isPlaying: Boolean,
    onPlayPauseClick: (String) -> Unit,
    onElementClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Используем itemsIndexed, чтобы сопоставить индекс списка с индексом плеера
        itemsIndexed(list) { index, element ->
            // Логика отображения: так как треков всего 2, мы используем остаток от деления.
            // Элементы 0, 2, 4... управляют треком 1 (индекс 0)
            // Элементы 1, 3, 5... управляют треком 2 (индекс 1)
            val isLinkedToCurrentTrack = (index % 2 == currentTrackIndex)
            val showPauseIcon = isLinkedToCurrentTrack && isPlaying

            ElementRow(
                element = element,
                showPauseIcon = showPauseIcon,
                onPlayClick = { onPlayPauseClick(element.id) },
                onItemClick = { onElementClick(element.id) }
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка Play/Pause
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (showPauseIcon) R.drawable.ic_pause else R.drawable.ic_play_arrow
                    ),
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Текст и картинка
            Column {
                AsyncImage(
                    model = element.image,
                    contentDescription = "Element Image ${element.title}",
                    modifier = Modifier.size(64.dp),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = element.title, style = MaterialTheme.typography.titleMedium)
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ContentState(
                list = sampleDataForPreview,
                currentTrackIndex = 0,
                isPlaying = true,
                onPlayPauseClick = {},
                onElementClick = {}
            )
        }
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