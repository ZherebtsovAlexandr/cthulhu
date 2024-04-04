package util

import java.util.concurrent.TimeUnit

fun Long.nanoToMicros() = TimeUnit.NANOSECONDS.toMicros(this)
fun Long.microToMillis() = TimeUnit.MICROSECONDS.toMillis(this)
