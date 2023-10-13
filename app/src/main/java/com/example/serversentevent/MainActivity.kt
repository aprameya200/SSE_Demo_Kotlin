package com.example.serversentevent

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.serversentevent.databinding.ActivityMainBinding
import com.example.serversentevent.lifecycle.MutableLiveData.SSEViewModel
import com.example.ssedemo.STATUS
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var viewModel: SSEViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SSEViewModel::class.java]

        val glide = Glide.with(this)

        viewModel?.sseEvents?.observe(this) {
            it?.let { event ->
                when(event.status) {

                    STATUS.OPEN -> {
                        binding.image.isVisible = false
                        binding.text.isVisible = true
                        binding.text.text = "Session opened"
                    }

                    STATUS.SUCCESS -> {
                        if (!event.image.equals(null)){
                            binding.image.isVisible = true
                            binding.text.isVisible = false
                            glide
                                .load(event.image)
                                .into(binding.image)
                        } else {
                            binding.image.isVisible = false
                            binding.text.isVisible = true
                            binding.text.text = "No image received"
                            Log.d("DATA",event.toString())
                        }
                    }

                    STATUS.ERROR -> {
                        binding.image.isVisible = false
                        binding.text.isVisible = true
                        binding.text.text = "Session Error"
                    }

                    STATUS.CLOSED -> {
                        binding.image.isVisible = false
                        binding.text.isVisible = true
                        binding.text.text = "Session closed"
                    }

                    else -> {
                        // STATUS.NONE
                        binding.text.isVisible = false
                        binding.image.isVisible = false
                    }
                }
            }
        }

        viewModel?.getSSEEvents()

    }
}