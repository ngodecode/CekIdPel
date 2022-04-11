package com.fxlibs.cekidpel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fxlibs.cekidpel.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    lateinit var binding:ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra("data")?.let { html ->
            binding.webView.loadData(html.replace("\n", "<br>"),
                "text/html",
                "UTF-8")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}