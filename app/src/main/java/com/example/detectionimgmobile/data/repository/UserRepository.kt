package com.example.detectionimgmobile.data.repository

import com.example.detectionimgmobile.data.api.RetrofitClient
import com.example.detectionimgmobile.data.model.BaseResponse
import com.example.detectionimgmobile.data.model.LoginData
import retrofit2.Response

class UserRepository {
    private val apiService = RetrofitClient.instance

    suspend fun login(nip: String, pass: String, role: String): Response<BaseResponse<LoginData>> {
        return apiService.login(nip, pass, role)
    }
}
