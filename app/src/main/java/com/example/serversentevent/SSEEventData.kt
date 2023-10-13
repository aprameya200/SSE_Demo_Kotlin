package com.example.ssedemo

import com.example.serversentevent.NamesAndAges

data class SSEEventData(
    val status: STATUS? = null,
    val image: NamesAndAges? = null
)

enum class STATUS {
    SUCCESS,
    ERROR,
    NONE,
    CLOSED,
    OPEN
}