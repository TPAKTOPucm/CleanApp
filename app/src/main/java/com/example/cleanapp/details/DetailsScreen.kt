package com.example.cleanapp.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cleanapp.details.vm.DetailsState
import com.example.cleanapp.details.vm.DetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailsScreen(
// Теперь навигация не нужна, vm получаем из Koin
    viewModel: DetailsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {
            is DetailsState.Content -> {
                Column {
                    Text(text = "ID: ${currentState.element.id}")
                    Text(text = "Title: ${currentState.element.title}")
                    AsyncImage(
                        modifier = Modifier.size(250.dp),
                        model = currentState.element.image,
                        contentScale = ContentScale.Crop,
                        contentDescription = null
                    )
                }
            }
            is DetailsState.Error -> {
                Text(text = currentState.message)
            }
            DetailsState.Loading -> {
                CircularProgressIndicator()
            }
        }
    }
}