package com.fabsys.notipe

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANTE: Asegúrate de que termine en "/"
    private const val BASE_URL = "https://ansjfeuicrkpgsrhnaiw.supabase.co/"
    const val SUPABASE_KEY = "sb_publishable_uSiGHsQ3NNcr7QNgRmA1Hg_k_MExAUG"

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}