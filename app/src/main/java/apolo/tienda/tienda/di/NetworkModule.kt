package apolo.tienda.tienda.di

import apolo.tienda.tienda.data.remote.api.ActualizacionApi
import apolo.tienda.tienda.data.remote.api.AuthApi
import apolo.tienda.tienda.data.remote.api.InventoryApi
import apolo.tienda.tienda.data.remote.interceptor.AuthInterceptor
import apolo.tienda.tienda.data.remote.interceptor.TokenInterceptor
import apolo.tienda.tienda.utils.PreferencesHelper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    fun provideOkHttpClient(preferencesHelper: PreferencesHelper): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(preferencesHelper))
            .addInterceptor(AuthInterceptor(preferencesHelper)) // ya no necesita context
            .build()

    fun provideRetrofit(baseUrl: String, preferencesHelper: PreferencesHelper): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(provideOkHttpClient(preferencesHelper))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    fun provideInventoryApi(retrofit: Retrofit): InventoryApi =
        retrofit.create(InventoryApi::class.java)

    fun provideActualizacionApi(retrofit: Retrofit): ActualizacionApi =
        retrofit.create(ActualizacionApi::class.java)
}
