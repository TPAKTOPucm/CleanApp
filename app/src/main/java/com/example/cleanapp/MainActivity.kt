package com.example.cleanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cleanapp.details.DetailsScreen
import com.example.cleanapp.details.DetailsScreenRoute
import com.example.cleanapp.main.MainScreen
import com.example.cleanapp.main.MainScreenRoute
import com.example.cleanapp.main.vm.MainViewModel
import com.example.cleanapp.ui.theme.CleanAppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
// ИЗМЕНЕНИЕ: Настраиваем NavHost с двумя экранами
                    NavHost(navController = navController,
                        startDestination = MainScreenRoute
                    ) {
                        composable<MainScreenRoute> {
                            val viewModel: MainViewModel =
                                koinViewModel()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.navigationEvent.collect {
                                        elementId ->

                                    navController.navigate(DetailsScreenRoute(id = elementId.toString()))
                                }
                            }

                            MainScreen(
                                state = state,
                                onElementClick = { elementId ->  viewModel.onElementClick(elementId.toString())
                                }
                            )
                        }
                        composable<DetailsScreenRoute> {
                            DetailsScreen() // Вызываем экран деталей
                        }
                    }
                }
            }
        }
    }
}
