package com.example.domain.mapper

/**
 * Интерфейс для мапперов, преобразующих один тип данных в другой.
 * @param I Входной тип (Input).
 * @param O Выходной тип (Output).
 */
interface Mapper<I, O> {
    fun map(input: I): O
}

