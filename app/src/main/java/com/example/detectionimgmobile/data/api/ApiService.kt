package com.example.detectionimgmobile.data.api

import com.example.detectionimgmobile.data.model.BaseResponse
import com.example.detectionimgmobile.data.model.LoginData
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import com.example.detectionimgmobile.data.model.*

interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("nip") nip: String,
        @Field("password") pass: String,
        @Field("role") role: String
    ): Response<BaseResponse<LoginData>>

    @GET("bm/cmo-list")
    suspend fun getBmCmoList(): Response<BaseResponse<BmCmoListResponse>>

    @GET("bm/cmo/{cmo_id}/customers")
    suspend fun getBmCmoCustomers(
        @retrofit2.http.Path("cmo_id") cmoId: Int
    ): Response<BaseResponse<BmCustomerListResponse>>

    @GET("bm/customer/{customer_id}/fraud-result")
    suspend fun getBmCustomerFraudResult(
        @retrofit2.http.Path("customer_id") customerId: Int
    ): Response<BaseResponse<BmFraudResultResponse>>
}
