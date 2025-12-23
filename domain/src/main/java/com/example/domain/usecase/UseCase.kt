package com.example.domain.usecase

interface UseCase<In, Out> {
    fun execute(data: In): Out
}

