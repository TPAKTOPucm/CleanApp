package com.example.cleanapp.main.vm

import com.example.domain.entity.ListElementEntity

sealed class MainState {
    data object Loading : MainState()
    data class Content(val list: List<ListElementEntity>) : MainState()
    data class Error(val message: String) : MainState()
}