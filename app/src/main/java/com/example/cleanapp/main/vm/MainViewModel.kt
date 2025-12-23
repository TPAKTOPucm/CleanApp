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

data class PlayerState(
    val currentTrackIndex: Int = -1, // Какой трек сейчас выбран (-1 если никакой)
    val isPlaying: Boolean = false // Играет или на паузе
)

class MainViewModel(
    private val getCatsUseCase: GetCatsUseCase
) : ViewModel() {
    /**
     * StateFlow для состояния UI.
     * 1. Вызывает getElementsUseCase(), который возвращает Flow<List<...>>.
     * 2. С помощью .map преобразуем каждый новый список в соответствующий MainState.
     * Если список пуст (что возможно при первом запуске, пока данные не загрузились), показывает Loading.
     * 3. С помощью .catch ловим любые ошибки во Flow и преобразуем их в MainState.Error.
     * 4. С помощью .stateIn преобразуем "холодный" Flow в "горячий" StateFlow,
     * который кэширует последнее значение и доступен для UI.
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
            _playerState.value = PlayerState(
                currentTrackIndex = it.currentMediaItemIndex,
                isPlaying = it.isPlaying
            )
        }
    }

    fun onPlayPauseClicked(elementId: String) {
        val controller = mediaController ?: return

        // Получаем текущий список из StateFlow
        val currentState = state.value
        if (currentState !is MainState.Content) return

        // Ищем позицию элемента в списке, который пришел из UseCase
        val targetIndex = currentState.list.indexOfFirst { it.id == elementId }

        if (targetIndex == -1) return // Элемент не найден

        // Поскольку у нас в Сервисе всего 2 трека, а список может быть длинным,
        // используем оператор остатка (%), чтобы треки повторялись для списка
        // (0->0, 1->1, 2->0, 3->1...)
        // Если хотите жесткое соответствие, уберите '% 2'
        val trackInPlaylist = targetIndex % 2

        if (trackInPlaylist == controller.currentMediaItemIndex) {
            // Кликнули по тому, что сейчас играет -> Пауза/Плей
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        } else {
            // Кликнули по другому -> Переключить трек
            controller.seekToDefaultPosition(trackInPlaylist)
            controller.playWhenReady = true
            controller.prepare()
            controller.play()
        }
    }

    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Метод, который будет вызывать UI
    fun onElementClick(elementId: String) {
        viewModelScope.launch {
            // Отправляем событие навигации в канал
            _navigationEvent.send(elementId)
        }
    }
}