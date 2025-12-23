package com.example.cleanapp.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import com.example.cleanapp.details.vm.DetailsState
import com.example.cleanapp.details.vm.DetailsViewModel

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel = koinViewModel()
) {
    // Подписываемся на изменения state из ViewModel
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {
            is DetailsState.Loading -> CircularProgressIndicator()
            is DetailsState.Error -> Text(text = currentState.message, color = Color.Red)
            is DetailsState.Content -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    AsyncImage(
                        model = currentState.element.image,
                        contentDescription = "Image of ${currentState.element.title}",
                        modifier = Modifier.size(250.dp),
                        placeholder = androidx.compose.ui.res.painterResource(id = com.example.cleanapp.R.drawable.ic_launcher_foreground),
                        error = androidx.compose.ui.res.painterResource(id = com.example.cleanapp.R.drawable.ic_launcher_foreground)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // При нажатии на кнопку вызываем метод из ViewModel
                    Button(onClick = { viewModel.applyFilter() }) {
                        Text("Применить Ч/Б фильтр (нужна зарядка)")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "ID: ${currentState.element.id}")
                    Text(text = "Title: ${currentState.element.title}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    // Preview для тестирования
}

