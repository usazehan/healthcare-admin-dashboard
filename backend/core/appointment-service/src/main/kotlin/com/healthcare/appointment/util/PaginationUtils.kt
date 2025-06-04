package com.healthcare.appointment.util

const val DEFAULT_PAGE_SIZE = 25

object PaginationUtils {
    fun encodePageToken(pageIndex: Int): String = pageIndex.toString()
    fun decodePageToken(token: String): Int = token.toIntOrNull() ?: 0
}