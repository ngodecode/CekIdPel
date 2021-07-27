package com.fxlibs.cekidpel

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiTagihan {

    @Headers("content-type: application/x-www-form-urlencoded; charset=UTF-8",
            "cookie: ci_session=bip5q3bur6leusmn2rl8lqe5cu5evf3o; _ga=GA1.2.604533929.1624420672; _gid=GA1.2.1614031339.1624420672; hotelmurah_csrf_cookie_name=5739f00f9517eb39c12732f65c3f3d93",
            "origin: https://www.hotelmurah.com",
            "referer: https://www.hotelmurah.com/pulsa/pln")
    @POST("pulsa/index.php/pln/cari_id_android")
    fun getBillStatus(@Body body:String): Call<String>

    companion object {

        private var mClient : OkHttpClient? = null
        fun get() : ApiTagihan {
            if (mClient == null) {
                mClient = buildClient()
            }
            val retrofit = Retrofit.Builder().baseUrl("https://www.hotelmurah.com")
                .client(mClient)
                .addConverterFactory(ConverterFactory())
                .build()

            return retrofit.create(ApiTagihan ::class.java)
        }

        private fun buildClient() : OkHttpClient {
            val trustAllCerts: Array<TrustManager> = arrayOf (
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> {return arrayOf()}
                }
            )
            return OkHttpClient.Builder().apply {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                sslSocketFactory (
                    SSLContext.getInstance("SSL").apply{init(null, trustAllCerts, SecureRandom())}.socketFactory,
                    trustAllCerts[0] as X509TrustManager
                )
            }.build()
        }
    }

}