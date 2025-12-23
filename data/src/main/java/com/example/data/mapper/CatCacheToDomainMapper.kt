package com.example.data.mapper

import com.example.data.database.CatCacheEntity
import com.example.domain.entity.ListElementEntity
import com.example.domain.mapper.Mapper

/**
 * Маппер для преобразования модели кэша CatCacheEntity в доменную модель ListElementEntity.
 */
class CatCacheToDomainMapper : Mapper<CatCacheEntity, ListElementEntity> {
    override fun map(input: CatCacheEntity): ListElementEntity {
        return ListElementEntity(
            id = input.id,
            title = "Cat #${input.id}",
            image = input.url,
            like = input.isLiked
        )
    }
}