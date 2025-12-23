package com.example.cleanapp.details.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import com.example.cleanapp.workers.KEY_FILTERED_URI
import com.example.cleanapp.workers.KEY_IMAGE_URI
import com.example.domain.usecase.GetCatsByIdUseCase
import java.util.UUID

class DetailsViewModel(
    private val getCatsByIdUseCase: GetCatsByIdUseCase,
    private val workManager: WorkManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Меняем на MutableStateFlow, чтобы иметь возможность обновлять его изнутри
    private val _state = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val elementId = savedStateHandle.get<String>("id")
            if (elementId != null) {
                getCatsByIdUseCase.execute(elementId).collect { element ->
                    if (element != null) {
                        _state.value = DetailsState.Content(element)
                    } else {
                        _state.value = DetailsState.Error("Элемент с ID $elementId не найден")
                    }
                }
            }
        }
    }

    // Метод для запуска фоновой задачи из UI
    fun applyFilter() {
        val currentState = _state.value as? DetailsState.Content ?: return
        val currentImageUri = currentState.element.image

        // 1. Создаем ограничения для задачи
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. Создаем запрос, передавая в него URI картинки и ограничения
        val filterRequest = OneTimeWorkRequestBuilder<com.example.cleanapp.workers.FilterWorker>()
            .setInputData(workDataOf(KEY_IMAGE_URI to currentImageUri))
            .setConstraints(constraints)
            .build()

        // 3. Ставим задачу в очередь
        workManager.enqueue(filterRequest)

        // 4. Сразу же начинаем отслеживать ее результат
        observeWorkResult(filterRequest.id)
    }

    // Метод для отслеживания результата работы Worker'а
    private fun observeWorkResult(workId: UUID) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId)
                .filterNotNull()
                .collect { workInfo ->
                    // Как только работа успешно завершена...
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // ...получаем URI отфильтрованного изображения...
                        val filteredUri = workInfo.outputData.getString(KEY_FILTERED_URI)
                        if (filteredUri != null) {
                            // ...и обновляем наш State новым элементом
                            val oldElement = (_state.value as? DetailsState.Content)?.element
                            if (oldElement != null) {
                                val newElement = oldElement.copy(image = filteredUri)
                                _state.value = DetailsState.Content(newElement)
                            }
                        }
                    }
                }
        }
    }
}