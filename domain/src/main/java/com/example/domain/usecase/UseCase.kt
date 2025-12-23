package com.example.domain.usecase

/**
 * Базовый интерфейс для всех UseCase'ов в приложении.
 * @param In Тип входных данных.
 * @param Out Тип возвращаемого результата.
 */
interface UseCase<In, Out> {
    fun execute(data: In): Out
}