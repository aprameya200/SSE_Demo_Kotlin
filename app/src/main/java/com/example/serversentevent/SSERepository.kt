package com.example.serversentevent


import android.util.Log
import com.example.ssedemo.SSEEventData
import com.example.ssedemo.STATUS
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources



data class NamesAndAges(val names: List<String>, val ages: List<Int>)

class SSERepository() {

    private val sseClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .build()

    private val sseRequest = Request.Builder()
        .url(EVENTSURL)
        .header("Accept", "application/json")
        .addHeader("Accept", "text/event-stream")
        .build()

    var sseEventsFlow = MutableStateFlow(SSEEventData(STATUS.NONE))
        private set

    private val sseEventSourceListener = object : EventSourceListener() {
        override fun onClosed(eventSource: EventSource) {
            super.onClosed(eventSource)
            Log.d(TAG, "onClosed: $eventSource")
            val event = SSEEventData(STATUS.CLOSED)
            sseEventsFlow.tryEmit(event)
        }

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            super.onEvent(eventSource, id, type, data)
            Log.d(TAG, "onEvent: $data")
            if (data.isNotEmpty()) {
                try {
                    // Parse the JSON data received from the server
                    val namesAndAges = Gson().fromJson(data, NamesAndAges::class.java)

                    // Create an SSEEventData object with the received data
                    val event = SSEEventData(STATUS.SUCCESS, image = namesAndAges)
                    sseEventsFlow.tryEmit(event)
                } catch (e: Exception) {
                    // Handle JSON parsing error
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                }
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            super.onFailure(eventSource, t, response)
            t?.printStackTrace()
            Log.d(TAG, "onFailure: ${t?.message}")
            val event = SSEEventData(STATUS.ERROR)
            sseEventsFlow.tryEmit(event)
        }

        override fun onOpen(eventSource: EventSource, response: Response) {
            super.onOpen(eventSource, response)
            Log.d(TAG, "onOpen: $eventSource")
            val event = SSEEventData(STATUS.OPEN)
            sseEventsFlow.tryEmit(event)
        }
    }

    init {
        initEventSource()
    }

    private fun initEventSource() {
        EventSources.createFactory(sseClient)
            .newEventSource(request = sseRequest, listener = sseEventSourceListener)
    }

    companion object {
        private const val TAG = "SSERepository"
        private const val EVENTSURL = "http://10.0.2.2:3000/sse"
    }

}