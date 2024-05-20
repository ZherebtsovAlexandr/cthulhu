package domain.model.report

import domain.model.slice.Slice

data class ThreadPoolReport(
    val threadPoolName: String = "",
    val threadCount: Int = 0,
    val averageAwaitTime: Long = 0,
    val totalAwaitTimeMilliSec: Long = 0L,
    var runningSliceCount: Int = 0,
    val awaitTimes: List<Long> = mutableListOf(),
    val awaitTimesByThreads: MutableMap<String, MutableList<Long>> = mutableMapOf(),
    val runningSliceDurations: List<Long> = mutableListOf(),
    val runningSliceBetweenTimes: List<Long> = mutableListOf(),
    val stateDurationsByThread: Map<String, Map<String, Long>> = mutableMapOf(),
    val stateDurationsBySlice: Map<String, Map<Slice, Map<String, Long>>> = mutableMapOf()
)