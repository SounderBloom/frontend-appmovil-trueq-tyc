package com.trueq.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseWrapper<T>(
    val data: T? = null,
    val message: String = "",
    val code: Int = 0
)
