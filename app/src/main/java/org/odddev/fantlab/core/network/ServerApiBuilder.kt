package org.odddev.fantlab.core.network

import com.facebook.stetho.okhttp3.StethoInterceptor

import org.odddev.fantlab.BuildConfig

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

/**
 * @author kenrube
 * *
 * @since 15.09.16
 */

internal object ServerApiBuilder {

	private val HTTP_LOG_TAG = "OkHttp"

	fun createApi(): IServerApi {
		val builder = OkHttpClient.Builder()

		builder.addInterceptor(HeaderInterceptor())
		builder.addInterceptor(StethoInterceptor())

		val loggingInterceptor = HttpLoggingInterceptor { message -> Timber.tag(HTTP_LOG_TAG).d(message) }
		loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
		builder.addInterceptor(loggingInterceptor)

		builder.followRedirects(false)

		val httpClient = builder.build()

		val retrofitBuilder = Retrofit.Builder()
				.baseUrl(BuildConfig.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.client(httpClient)

		return retrofitBuilder.build().create(IServerApi::class.java)
	}
}
