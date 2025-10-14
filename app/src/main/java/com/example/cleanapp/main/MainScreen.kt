package com.example.cleanapp.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        when (val currentState = state) {
            is MainState.Content -> ContentState(list = currentState.list)
            is MainState.Error -> ErrorState(message = currentState.message)
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
@Composable
fun ContentState(list: List<ListElementEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) { element ->
            ElementRow(element = element)
        }
    }
}

@Composable
fun ElementRow(element: ListElementEntity) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier.size(100.dp),
            model = element.image,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = element.title,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Like(isLiked = element.like)
    }
}

private val sampleDataForPreview = listOf(
    ListElementEntity("1", "Continue Cat", "https://http.cat/images/100.jpg", true),
    ListElementEntity("2", "Ok Cat", "https://http.cat/images/200.jpg", true),
    ListElementEntity("3", "Multiple Cat", "https://http.cat/images/300.jpg", false)
)
@Preview(name = "Content State", showBackground = true)
@Composable
fun ContentStatePreview() {
    CleanAppTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            ContentState(list = sampleDataForPreview)
        }
    }
}
@Preview(name = "Loading State", showBackground = true)
@Composable
fun LoadingStatePreview() {
    CleanAppTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment =
                Alignment.Center) {
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