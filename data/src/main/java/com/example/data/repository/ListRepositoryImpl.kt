package com.example.data.repository

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import com.example.data.database.CatCacheEntity
import com.example.data.database.CatDao
import com.example.data.network.ApiService
import com.example.data.network.response.CatImageDto
import com.example.domain.entity.ListElementEntity
import com.example.domain.mapper.Mapper
import com.example.domain.repository.ListRepository

class ListRepositoryImpl(
    private val apiService: ApiService,
    private val catDao: CatDao,
    private val dtoToCacheMapper: Mapper<CatImageDto, CatCacheEntity>,
    private val cacheToDomainMapper: Mapper<CatCacheEntity, ListElementEntity>,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context
) : ListRepository {
    
    override fun getElements(): Flow<List<ListElementEntity>> {
        return catDao.getCatsFlow() // 1. Берем постоянный поток данных из БД
            .map { catsFromCache -> // 2. Мапим каждую новую порцию данных в доменную модель
                catsFromCache.map { cacheToDomainMapper.map(it) }
            }
            .onStart { // 3. При первой подписке на этот Flow, выполняем этот блок
                try {
                    val currentCats = catDao.getCatsSync()
                    
                    if (currentCats.isEmpty()) {
                        Log.d("ListRepositoryImpl", "Database is empty, loading from network")
                        val catsFromApi = apiService.getCatImages()
                        val cacheEntities = catsFromApi.map { dtoToCacheMapper.map(it) }
                        catDao.insertCats(cacheEntities)
                        precacheImages(cacheEntities)
                    } else {

                        Log.d("ListRepositoryImpl", "Using cached data (${currentCats.size} items)")
                    }
                } catch (e: Exception) {
                    Log.e("ListRepositoryImpl", "Network update failed", e)
                }
            }
    }

    override fun getElement(id: String): Flow<ListElementEntity?> {
        // Для одного элемента обновление из сети не делаем (для простоты),
        // просто берем актуальные данные из кэша.
        return catDao.getCatById(id).map { catFromCache ->
            catFromCache?.let { cacheToDomainMapper.map(it) }
        }
    }

    override suspend fun toggleLike(elementId: String) {
        val currentEntity = catDao.getCatById(elementId)
            .map { it }
            .first()
        
        currentEntity?.let { entity ->
            val newLikeStatus = !entity.isLiked
            catDao.updateLikeStatus(elementId, newLikeStatus)
        }
    }

    private fun precacheImages(cats: List<CatCacheEntity>) {
        cats.forEach { cat ->
            val request = ImageRequest.Builder(applicationContext)
                .data(cat.url)
                .build()
            imageLoader.enqueue(request)
        }
    }
}

