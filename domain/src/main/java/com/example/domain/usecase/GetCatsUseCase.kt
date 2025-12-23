package com.example.domain.usecase

import kotlinx.coroutines.flow.Flow
import com.example.domain.entity.ListElementEntity
import com.example.domain.repository.ListRepository

/**
 * Use Case для получения списка котиков.
 * Принимает Unit (т.к. входных параметров нет), возвращает Flow со списком ListElementEntity.
 */
class GetCatsUseCase(
    private val listRepository: ListRepository
) : UseCase<Unit, Flow<List<ListElementEntity>>> {
    
    override fun execute(data: Unit): Flow<List<ListElementEntity>> {
        // UseCase просто делегирует вызов репозиторию.
        // Здесь может быть дополнительная бизнес-логика (сортировка, фильтрация),
        // но НЕ маппинг.
        return listRepository.getElements()
    }
}

