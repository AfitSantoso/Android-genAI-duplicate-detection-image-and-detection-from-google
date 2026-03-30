package com.example.detectionimgmobile.data.api

import com.example.detectionimgmobile.data.model.BaseResponse
import com.example.detectionimgmobile.data.model.LoginData
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("nip") nip: String,
        @Field("password") pass: String,
        @Field("role") role: String
    ): Response<BaseResponse<LoginData>>
}
