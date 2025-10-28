package com.example.cleanapp.details.vm

import com.example.domain.entity.ListElementEntity

sealed class DetailsState {
    data object Loading : DetailsState()
    data class Error(val message: String) : DetailsState()
    data class Content(val element: ListElementEntity) : DetailsState()
}