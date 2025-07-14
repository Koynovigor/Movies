package com.l3on1kl.movies.util

import android.content.Context
import com.l3on1kl.movies.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {
    fun map(throwable: Throwable, context: Context): String = when (throwable) {
        is UnknownHostException -> context.getString(R.string.error_no_internet)

        is SocketTimeoutException -> context.getString(R.string.error_timeout)

        is IOException -> context.getString(R.string.error_network)

        else -> throwable.localizedMessage ?: context.getString(R.string.unexpected_error)
    }
}