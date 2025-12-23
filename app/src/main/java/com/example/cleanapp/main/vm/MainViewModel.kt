package com.example.cleanapp.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.domain.entity.ListElementEntity
import com.example.domain.usecase.GetCatsUseCase
import com.example.domain.usecase.ToggleLikeUseCase

// Расширяем состояние, добавляя информацию о плеере
data class PlayerState(
    val selectedCatId: String? = null, // ID выбранного кота
    val isPlaying: Boolean = false // Играет или на паузе
)

// События для Activity (Clean Architecture - ViewModel не зависит от Android-компонентов)
sealed class PlaybackEvent {
    data class SelectCat(val catId: String, val catIndex: Int) : PlaybackEvent()
    data class UpdateLike(val catId: String) : PlaybackEvent()
}

class MainViewModel(
    private val getCatsUseCase: GetCatsUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase
) : ViewModel() {
    /**
     * StateFlow для состояния UI.
     * 1. Вызываем getCatsUseCase(), который возвращает Flow<List<...>>.
     * 2. С помощью .map преобразуем каждый новый список в соответствующий MainState.
     *    Если список пуст (что возможно при первом запуске, пока данные не загрузились), показываем Loading.
     * 3. С помощью .catch ловим любые ошибки во Flow и преобразуем их в MainState.Error.
     * 4. С помощью .stateIn преобразуем "холодный" Flow в "горячий" StateFlow,
     *    который кэширует последнее значение и доступен для UI.
     */
    val state: StateFlow<MainState> = getCatsUseCase.execute(Unit)
        .map<List<ListElementEntity>, MainState> { list ->
            if (list.isEmpty()) {
                MainState.Loading
            } else {
                MainState.Content(list)
            }
        }
        .catch { e ->
            emit(MainState.Error(e.localizedMessage ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainState.Loading
        )

    // 2. Поток плеера (Состояние кнопок)
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private var mediaController: MediaController? = null

    // Подключение контроллера (вызывается из Activity)
    fun setController(controller: MediaController) {
        this.mediaController = controller
        updatePlayerState()
        controller.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                updatePlayerState()
            }
        })
    }

    private fun updatePlayerState() {
        mediaController?.let {
            _playerState.value = _playerState.value.copy(
                isPlaying = it.isPlaying
            )
        }
    }

    fun onPlayPauseClicked(elementId: String) {
        val controller = mediaController ?: return
        // Получаем текущий список из StateFlow
        val currentState = state.value
        if (currentState !is MainState.Content) return

        // Ищем позицию элемента в списке
        val targetIndex = currentState.list.indexOfFirst { it.id == elementId }
        if (targetIndex == -1) return // Элемент не найден

        // Трек выбираем по четности: 0,2,4...→track_1, 1,3,5...→track_2
        val trackInPlaylist = targetIndex % 2

        // Проверяем - это уже выбранный кот?
        val isAlreadySelected = (_playerState.value.selectedCatId == elementId)

        if (isAlreadySelected && controller.isPlaying) {
            controller.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
        } else {
            viewModelScope.launch {
                _playbackEvent.send(PlaybackEvent.SelectCat(elementId, targetIndex))
            }
            _playerState.value = PlayerState(
                selectedCatId = elementId,
                isPlaying = true
            )

            if (trackInPlaylist != controller.currentMediaItemIndex) {
                controller.seekToDefaultPosition(trackInPlaylist)
            }
            controller.playWhenReady = true
            controller.prepare()
            controller.play()
        }
    }

    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    private val _playbackEvent = Channel<PlaybackEvent>()
    val playbackEvent = _playbackEvent.receiveAsFlow()

    // Метод, который будет вызывать UI
    fun onElementClick(elementId: String) {
        viewModelScope.launch {
            // Отправляем событие навигации в канал
            _navigationEvent.send(elementId)
        }
    }

    fun onToggleLike(elementId: String) {
        viewModelScope.launch {
            toggleLikeUseCase.executeSuspend(elementId)
            _playbackEvent.send(PlaybackEvent.UpdateLike(elementId))
        }
    }

    fun updateSelectedCatId(catId: String) {
        _playerState.value = _playerState.value.copy(selectedCatId = catId)
    }
}

