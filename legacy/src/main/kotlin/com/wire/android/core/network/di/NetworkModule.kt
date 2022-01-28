@file:Suppress("MatchingDeclarationName")

package com.wire.android.core.network.di

import android.content.Context
import android.net.ConnectivityManager
import com.wire.android.BuildConfig
import com.wire.android.core.network.either.EitherResponseAdapterFactory
import com.wire.android.core.network.NetworkConfig
import com.wire.android.core.network.HttpRequestParams
import com.wire.android.core.network.NetworkClient
import com.wire.android.core.network.NetworkHandler
import com.wire.android.core.network.RetrofitClient
import com.wire.android.core.network.UserAgentInterceptor
import com.wire.android.core.network.auth.accesstoken.AccessTokenAuthenticator
import com.wire.android.core.network.auth.accesstoken.AccessTokenInterceptor
import com.wire.android.core.network.auth.accesstoken.AuthenticationManager
import com.wire.android.core.network.di.NetworkDependencyProvider.createHttpClientWithAuth
import com.wire.android.core.network.di.NetworkDependencyProvider.createHttpClientWithoutAuth
import com.wire.android.core.network.di.NetworkDependencyProvider.retrofit
import com.wire.android.shared.session.datasources.remote.SessionApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory


object NetworkDependencyProvider {

    fun retrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ProtoConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(EitherResponseAdapterFactory())
            .build()

    fun createHttpClientWithAuth(httpsRequestParams: HttpRequestParams,
                                 accessTokenInterceptor: AccessTokenInterceptor,
                                 accessTokenAuthenticator: AccessTokenAuthenticator,
                                 userAgentInterceptor: UserAgentInterceptor): OkHttpClient =
        defaultHttpClient(httpsRequestParams, userAgentInterceptor)
            .addInterceptor(accessTokenInterceptor)
            .authenticator(accessTokenAuthenticator)
            .build()

    fun createHttpClientWithoutAuth(httpsRequestParams: HttpRequestParams,
                                    userAgentInterceptor: UserAgentInterceptor): OkHttpClient =
        defaultHttpClient(httpsRequestParams, userAgentInterceptor).build()

    private fun defaultHttpClient(httpParams: HttpRequestParams,
                                  userAgentInterceptor: UserAgentInterceptor): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectionSpecs(httpParams.connectionSpecs)
            .addInterceptor(userAgentInterceptor)
            .addLoggingInterceptor()

    private fun OkHttpClient.Builder.addLoggingInterceptor() = this.apply {
        if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().setLevel(Level.BODY))
    }
}

val networkModule: Module = module {
    single { NetworkHandler(androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager) }
    single<NetworkClient> { RetrofitClient(get()) }
    single { retrofit(get(), get<NetworkConfig>().baseUrl) }
    single { createHttpClientWithAuth(get(), get(), get(), get()) }
    single { HttpRequestParams() }
    single { AccessTokenAuthenticator(get(), get()) }
    single { AccessTokenInterceptor(get()) }
    single { UserAgentInterceptor() }
    factory { AuthenticationManager() }

    val networkClientForNoAuth = "NETWORK_CLIENT_NO_AUTH_REQUEST"
    single<NetworkClient>(named(networkClientForNoAuth)) {
        RetrofitClient(retrofit(createHttpClientWithoutAuth(get(), get()), get<NetworkConfig>().baseUrl))
    }
    single { get<NetworkClient>(named(networkClientForNoAuth)).create(SessionApi::class.java) }
    single { NetworkConfig() }
}