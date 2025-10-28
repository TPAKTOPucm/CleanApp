package com.example.cleanapp.main.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.entity.ListElementEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow<MainState>(MainState.Loading)
    val state = _state.asStateFlow()

    // Создаем канал для отправки "одноразовых" событий
    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val sampleData = listOf(
        ListElementEntity("1", "Continue Cat", "https://http.cat/images/100.jpg", true),
        ListElementEntity("2", "Ok Cat", "https://http.cat/images/200.jpg", true),
        ListElementEntity("3", "Multiple Cat", "https://http.cat/images/300.jpg", false)
    )

    init {
        viewModelScope.launch {
            _state.emit(MainState.Content(sampleData))
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = MainState.Loading
            // 3адержкa сети
            delay(2000)
            _state.value = MainState.Content(sampleData)
        }
    }

    fun onElementClick(elementId: String) {
        viewModelScope.launch {
            _navigationEvent.send(elementId)
        }
    }
}