package com.example.cleanapp

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.koin.androidx.compose.koinViewModel
import com.example.cleanapp.details.DetailsScreen
import com.example.cleanapp.details.DetailsScreenRoute
import com.example.cleanapp.main.MainScreen
import com.example.cleanapp.main.MainScreenRoute
import com.example.cleanapp.main.vm.MainViewModel
import com.example.cleanapp.ui.theme.CleanAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = MainScreenRoute
                    ) {
                        composable<MainScreenRoute> {
                            val viewModel: MainViewModel = koinViewModel()
                            // 1. Сохраняем ссылку на VM для слушателя
                            mainViewModel = viewModel
                            // 2. ИСПРАВЛЕНИЕ: Если контроллер УЖЕ готов (соединение было быстрым),
                            // сразу отдаем его во ViewModel
                            controller?.let { viewModel.setController(it) }

                            val mainState by viewModel.state.collectAsState()
                            val playerState by viewModel.playerState.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.navigationEvent.collect { elementId ->
                                    navController.navigate(DetailsScreenRoute(id = elementId))
                                }
                            }

                            MainScreen(
                                state = mainState,
                                playerState = playerState,
                                onPlayPauseClick = { elementId ->
                                    viewModel.onPlayPauseClicked(elementId)
                                },
                                onElementClick = { elementId ->
                                    viewModel.onElementClick(elementId)
                                }
                            )
                        }

                        composable<DetailsScreenRoute> {
                            DetailsScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, com.example.cleanapp.service.PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                // 3. Получаем контроллер
                val ctrl = controllerFuture.get()
                controller = ctrl
                // 4. ИСПРАВЛЕНИЕ: Отдаем его во ViewModel (если VM уже создана)
                mainViewModel?.setController(ctrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
        controller = null
    }
}