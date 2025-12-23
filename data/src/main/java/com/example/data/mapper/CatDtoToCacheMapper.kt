package com.example.data.mapper

import com.example.data.database.CatCacheEntity
import com.example.data.network.response.CatImageDto
import com.example.domain.mapper.Mapper

class CatDtoToCacheMapper : Mapper<CatImageDto, CatCacheEntity> {
    override fun map(input: CatImageDto): CatCacheEntity {
        return CatCacheEntity(id = input.id, url = input.url)
    }
}

