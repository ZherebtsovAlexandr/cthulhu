package domain.model.report

data class ThreadPoolReport(
    val threadPoolName: String = "",
    val threadCount: Int = 0,
    val averageAwaitTime: Long = 0,
    val totalAwaitTimeMilliSec: Long = 0L,
    var runningSliceCount: Int = 0,
    val awaitTimes: List<Long> = mutableListOf(),
    val runningSliceDurations: List<Long> = mutableListOf(),
    val runningSliceBetweenTimes: List<Long> = mutableListOf()
)