package com.example.cleanapp.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cleanapp.main.vm.MainState
import com.example.cleanapp.main.vm.MainViewModel
import com.example.cleanapp.ui.theme.CleanAppTheme
import com.example.cleanapp.ui.view.Like
import com.example.domain.entity.ListElementEntity
import org.koin.androidx.compose.koinViewModel
import kotlin.collections.listOf

@Composable
fun MainScreen(state: MainState, onElementClick: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is MainState.Content -> ContentState(
                list = state.list,
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

// ИЗМЕНЕНИЕ: ContentState получает лямбду onElementClick
@Composable
fun ContentState(
    list: List<ListElementEntity>,
    onElementClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { element ->
            ElementRow(
                element = element,
                // Передаем modifier с обработчиком клика в ElementRow
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onElementClick(element.id) }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ElementRow(element: ListElementEntity, modifier: Modifier =
    Modifier) {
    Row(
        modifier = modifier, // Применяем переданный modifier здесь
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = element.image,
            contentDescription = "Element Image",
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = element.title)
        }
    }
}
// --- ИНСТРУМЕНТЫ ДЛЯ ПРЕВЬЮ ---
private val sampleDataForPreview = listOf(
    ListElementEntity("1", "Continue Cat", "https://http.cat/images/100.jpg", true),
    ListElementEntity("2", "Ok Cat", "https://http.cat/images/200.jpg", true),
    ListElementEntity("3", "Multiple Cat", "https://http.cat/images/300.jpg", false)
)

@Preview(showBackground = true)
@Composable
fun ContentStatePreview() {
    val sampleData = listOf(
        ListElementEntity("1", "Continue Cat", "https://http.cat/images/100.jpg", true),
        ListElementEntity("2", "Ok Cat", "https://http.cat/images/200.jpg", true),
        ListElementEntity("3", "Multiple Cat", "https://http.cat/images/300.jpg", false)
    )
    ContentState(list = sampleData, onElementClick = {})
}
@Preview(name = "Loading State", showBackground = true)
@Composable
fun LoadingStatePreview() {
    CleanAppTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment =
                Alignment.Center) {
// И его тоже вызываем напрямую
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment =
                Alignment.Center) {
                ErrorState(message = "Something went wrong!")
            }
        }
    }
}