package com.example.data.mapper

import com.example.data.network.response.CatImageDto
import com.example.domain.entity.ListElementEntity

fun CatImageDto.toDomain(): ListElementEntity {
    return ListElementEntity(
        id = this.id,
        title = "Cat #$id",
        image = this.url,
        like = false
    )
}