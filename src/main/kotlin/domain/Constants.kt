package domain

import java.util.concurrent.TimeUnit

object Constants {
    val MAX_GET_TASK_DURATION_MICRO_SEC = TimeUnit.MICROSECONDS.toNanos(150L)
}