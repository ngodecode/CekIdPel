package com.fxlibs.cekidpel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class MainViewModel : ViewModel() {

    data class Result<T>(val status: Status, val data:T? = null)
    enum class Status{SUCCESS, ERROR_CONNECTION, ERROR_DATA, ERROR_NOT_FOUND}

    val dataCaptcha = MutableLiveData<Result<ByteArray>>()
    val dataInfo    = MutableLiveData<Result<String>>()

    var mCookie = ""
    fun getCaptcha() {
        ApiPLN.get().getCaptcha(5).enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {

                try {
                    mCookie = ""
                    var cookies = response?.headers()?.toMultimap()?.get("Set-Cookie")
                    if (cookies != null && cookies.isNotEmpty()) {
                        cookies.forEach {
                            var cook = it.substring(0, it.indexOf(";"))
                            mCookie += ";$cook "
                        }
                    }
                    val image   = response?.body()?.byteStream()?.readBytes()
                    if (mCookie != null && image != null) {
                        dataCaptcha.postValue(Result(Status.SUCCESS, image))
                    }
                    else {
                        dataCaptcha.postValue(Result(Status.ERROR_CONNECTION))
                    }
                } catch (e:Exception) {
                    dataCaptcha.postValue(Result(Status.ERROR_DATA))
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                dataCaptcha.postValue(Result(Status.ERROR_CONNECTION))
            }

        })

    }

    fun getInfo(capcha:String, idPel:String, by:String = "nometer") {
        val body = "5|0|8|https://pelanggan.pln.co.id/id.co.iconpln.web.PDMohonEntryPoint/|AB6BB8F2A9B546B9DE2B259C9242D117|id.co.iconpln.web.client.service.TransService|getDataPelangganBykriteria|java.lang.String/2004016611|$by|$idPel|$capcha|1|2|3|4|3|5|5|5|6|7|8|"
        val cookie = "_ga=GA1.3.1439579538.1597333509; _gid=GA1.3.1006617863.1597333509$mCookie"
        ApiPLN.get().getInfo(cookie, body)
            .enqueue(object : Callback<String>{

                val knownKeys = HashSet<String>().apply {
                    addAll("nama,idpel,nometer_kwh,daya,nama_kec,nama_kel,nama_prov,nama_kab,tarif,kd_kel,namapnj".split(","))
                }

                fun getValue(list:ArrayList<String>, key:String) : String? {
                    return list.indexOf(key).let {
                        if (it > -1 && it < list.size) {
                            list[it + 1]
                        }
                        else {
                            null
                        }
                    }?.let {
                        if (knownKeys.contains(it)) {
                            null
                        }
                        else {
                            it
                        }
                    }
                }


                override fun onResponse(call: Call<String>?, response: Response<String>?) {
                    try {
                        var response = response?.body()
                            response = response?.substring(4)

                        val idx = response?.indexOf("[", 1)

                        var data = JSONArray("[" + response?.substring(idx!!)).getJSONArray(0)

                        //nama, nometer_kwh, idpel, daya, tarif bersubsidi

                        val list = ArrayList<String>().apply {
                            Array(data.length()) {
                                data.getString(it)
                            }.let {
                                addAll(it)
                            }
                        }


                        val nama  = getValue(list, "nama")
                        val idpel = getValue(list, "idpel")
                        val daya  = getValue(list, "daya")
                        val nometer_kwh = getValue(list, "nometer_kwh")
                        val nama_kec    = getValue(list, "nama_kec") ?: ""
                        val nama_kel    = getValue(list, "nama_kel") ?: ""
                        val nama_prov   = getValue(list, "nama_prov")?: ""
                        val nama_kab    = getValue(list, "nama_kab")?: ""
                        val namapnj     = getValue(list, "namapnj")?: ""
                        val tarif       = getValue(list, "tarif")
                        val subsidi      = tarif?.let {
                            when {
                                it.endsWith("M") -> {
                                    "/ TIDAK BERSUBSIDI"
                                }
                                it.endsWith("T") -> {
                                    "/ BERSUBSIDI"
                                }
                                else -> {
                                    ""
                                }
                            }
                        } ?: ""

                        if (by == "nometer" && nama == null) {
                            getInfo(capcha, idPel, "idpel")
                        }
                        else {
                            var html ="<table>"
                                html +="<tr><td>NAMA</td><td> : $nama </td></tr>" +
                                    "<tr><td>ID PELANGGAN </td><td> : $idpel</td></tr>" +
                                    "<tr><td>NOMOR KWH </td><td> : $nometer_kwh </td></tr>" +
                                    "<tr><td>TARIF </td><td> : $tarif $subsidi </td></tr>" +
                                    "<tr><td>DAYA </td><td> : $daya </td></tr>" +
                                    "<tr><td>LOKASI </td><td> : $namapnj $nama_kec $nama_kel $nama_prov $nama_kab </td></tr>"

                            html += "</table>\n"
                            getBill(idPel, html)
                        }

                    } catch (e:Exception) {
                        Log.e(javaClass.name, e.message, e)
                        dataInfo.postValue(Result(Status.ERROR_CONNECTION))
                    }
                }

                override fun onFailure(call: Call<String>?, t: Throwable?) {
                    Log.e(javaClass.name, t?.message, t)
                    dataInfo.postValue(Result(Status.ERROR_CONNECTION))
                }

            })

    }

    private fun getBill(idPel: String, info:String) {

        val link = "\n\n<a href='https://play.google.com/store/apps/details?id=com.ftools.ceksubsidi'> Klik disini untuk Cek Subsidi/Stimulus >> </a>"
        val body = "id=$idPel&jenis=1&kode=TU&hm_csrf_hash_name=5739f00f9517eb39c12732f65c3f3d93"
        ApiTagihan.get().getBillStatus(body).enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                try {
                    var infoBill = ""
                    val json    = JSONObject(response?.body())
                    val status  = json.getString("status")
                    val data    = json.getString("data")
                    if (status == "2") {
                        val bill = JSONObject(data).getJSONObject("info")
                        infoBill += "TAGIHAN BULAN INI SENILAI Rp. " + DecimalFormat("#,###").format(bill.getString("amount").toDouble()).replace(",", ".")
                    }
                    else if (status == "0") {
                        infoBill += data
                    }
                    dataInfo.postValue(Result(Status.SUCCESS, info + "\n" + infoBill + link))
                } catch (e:Exception) {
                    Log.e(javaClass.name, e.message, e)
                    dataInfo.postValue(Result(Status.SUCCESS, info + link))
                }

            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                dataInfo.postValue(Result(Status.SUCCESS, info + link))
            }

        })
    }

}