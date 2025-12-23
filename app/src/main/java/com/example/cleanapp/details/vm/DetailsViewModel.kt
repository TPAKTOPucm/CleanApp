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

    fun applyFilter() {
        val currentState = _state.value as? DetailsState.Content ?: return
        val currentImageUri = currentState.element.image

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val filterRequest = OneTimeWorkRequestBuilder<com.example.cleanapp.workers.FilterWorker>()
            .setInputData(workDataOf(KEY_IMAGE_URI to currentImageUri))
            .setConstraints(constraints)
            .build()

        workManager.enqueue(filterRequest)

        observeWorkResult(filterRequest.id)
    }

    private fun observeWorkResult(workId: UUID) {
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId)
                .filterNotNull()
                .collect { workInfo ->
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val filteredUri = workInfo.outputData.getString(KEY_FILTERED_URI)
                        if (filteredUri != null) {
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