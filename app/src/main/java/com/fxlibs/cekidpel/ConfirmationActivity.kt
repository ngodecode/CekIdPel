package com.fxlibs.cekidpel

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.fxlibs.cekidpel.databinding.ActivityConfirmationBinding

class ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getByteArrayExtra("image")?.let {image ->
            binding.imgCapcha.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
        }

        binding.btnSubmit.setOnClickListener {
            if (binding.edtCapcha.text.isBlank()) {
                Toast.makeText(this, "Mohon ketik kode terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
            else {
                setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("captcha", binding.edtCapcha.text.toString())
                    }
                )
                finish()
            }
        }

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

}


