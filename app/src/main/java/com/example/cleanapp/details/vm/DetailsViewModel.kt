package com.example.cleanapp.details.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.cleanapp.details.DetailsScreenRoute
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    savedStateHandle: SavedStateHandle,
    val repository: ListRepository
) : ViewModel() {
    private val _state =
        MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state: StateFlow<DetailsState> get() = _state
    // Временно создадим здесь тот же самый список для симуляции
    private val sampleData = listOf(
        ListElementEntity("1", "Continue Cat", "https://http.cat/images/100.jpg", true),
        ListElementEntity("2", "Ok Cat", "https://http.cat/images/200.jpg", true),
        ListElementEntity("3", "Multiple Cat", "https://http.cat/images/300.jpg", false)
    )
    init {
        viewModelScope.launch {
            val routeInfo =
                savedStateHandle.toRoute<DetailsScreenRoute>()
            val elementId = routeInfo.id
            val element = repository.getElementById(elementId)
            if (element.isSuccess) {
                _state.emit(DetailsState.Content(element.getOrThrow()))
            } else {
                _state.emit(DetailsState.Error("Элемент с ID $elementId не найден"))
            }
        }
    }
}