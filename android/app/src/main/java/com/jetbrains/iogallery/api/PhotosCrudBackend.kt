package com.jetbrains.iogallery.api

import androidx.lifecycle.LiveData
import com.jetbrains.iogallery.model.ApiPhotos
import com.jetbrains.iogallery.model.ApiPhotos.ApiEmbedded.ApiPhoto
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotosCrudBackend {

    @GET("photos")
    fun photosList(): LiveData<Result<ApiPhotos>>

    @GET("photos/{id}")
    fun photo(@Path("id") id: String): LiveData<Result<ApiPhoto>>

    @Multipart
    @POST("upload")
    fun uploadPhoto(@Part photo: MultipartBody.Part): LiveData<Result<Unit>>

    @DELETE("image/{id}")
    fun deleteImage(@Path("id") id: String): LiveData<Result<Unit>>
}
