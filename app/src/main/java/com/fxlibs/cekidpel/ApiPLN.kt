package com.fxlibs.cekidpel

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiPLN {

    @GET("id.co.iconpln.web.PBMohonEntryPoint/SimpleCaptcha.jpg")
    fun getCaptcha(@Query("validasi") validasi:Int): Call<ResponseBody>

    @Headers("Content-Type: text/x-gwt-rpc; charset=UTF-8"
        , "Referer: https://pelanggan.pln.co.id/id.co.iconpln.web.PDMohonEntryPoint/78FCC45720AE0CCB246A48FC48C44B87.cache.html"
        , "Origin: https://pelanggan.pln.co.id"
        , "Host: pelanggan.pln.co.id"
        , "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
    @POST("id.co.iconpln.web.PDMohonEntryPoint/TransService")
    fun getInfo(@Header("Cookie") cookie:String, @Body body:String): Call<String>


    companion object {
        private var mClient : OkHttpClient? = null
        fun get() : ApiPLN {
            if (mClient == null) {
                mClient = buildClient()
            }
            val retrofit = Retrofit.Builder().baseUrl("https://pelanggan.pln.co.id")
                .client(mClient)
                .addConverterFactory(ConverterFactory())
                .build()

            return retrofit.create(ApiPLN ::class.java)
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