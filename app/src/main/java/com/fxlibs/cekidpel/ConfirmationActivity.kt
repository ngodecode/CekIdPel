package com.fxlibs.cekidpel

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fxlibs.cekidpel.databinding.ActivityConfirmationBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class ConfirmationActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    lateinit var idpel:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.mCookie = intent.getStringExtra("cookie") ?: ""
        idpel = intent.getStringExtra("idpel") ?: ""

        intent.getByteArrayExtra("image")?.let {image ->
            binding.imgCapcha.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
        }

        binding.txtLabel.setOnClickListener(object : View.OnClickListener{

            var clicked = 0;
            override fun onClick(v: View?) {
                clicked++
                if (clicked == 30) {
                    this@ConfirmationActivity.getSharedPreferences("SYS", Context.MODE_PRIVATE)
                        .edit().putBoolean("ADS_IGNORE", true).apply()
                    Toast.makeText(this@ConfirmationActivity, "OK", Toast.LENGTH_SHORT).show()
                }
            }

        } )

        binding.edtCapcha.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSubmit.callOnClick()
                true
            }
            false
        }

        val dialog = AlertDialog.Builder(this).setMessage("Memuat data").create()

        binding.btnSubmit.setOnClickListener {
            if (binding.edtCapcha.text.isBlank()) {
                Toast.makeText(this, "Mohon ketik kode terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
            else {
                binding.btnSubmit.isEnabled = false
                dialog.show()
                viewModel.getInfo(binding.edtCapcha.text.toString(), idpel)
            }
        }
        binding.adView.loadAd(AdRequest.Builder().build())

        viewModel.dataInfo.observe(this, Observer {
            when (it.status) {
                MainViewModel.Status.SUCCESS -> {
                    it.data?.let { html ->
                        showAds {
                            startActivity(Intent(this, ResultActivity::class.java).apply {
                                putExtra("data", html)
                            })
                            finish()
                        }
                    }
                }
                MainViewModel.Status.ERROR_NOT_FOUND -> {
                    Toast.makeText(this, "Maaf sitem kami tidak berhasil menemukan informasi", Toast.LENGTH_SHORT).show()
                    finish()
                }
                MainViewModel.Status.ERROR_DATA -> {
                    Toast.makeText(this, "Gagal Memuat Data, Coba lagi nanti", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Gagal Memuat Data, Periksa Koneksi Anda",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.dismiss()
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showAds(onFinish: () -> Unit) {
        if (this.getSharedPreferences("SYS", Context.MODE_PRIVATE)
                .getBoolean("ADS_IGNORE", false)
        ) {
            onFinish()
            return
        }
        InterstitialAd.load(this@ConfirmationActivity,
            resources.getString(R.string.ads_unit_interstitial),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.fullScreenContentCallback = object :
                        FullScreenContentCallback() {
                        /** Called when the ad failed to show full screen content.  */
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            onFinish()
                        }

                        /** Called when ad showed the full screen content.  */
                        override fun onAdShowedFullScreenContent() {
                        }

                        /** Called when full screen content is dismissed.  */
                        override fun onAdDismissedFullScreenContent() {
                            onFinish()
                        }
                    }
                    ad.show(this@ConfirmationActivity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
                    onFinish()
                }
            })
    }

}


