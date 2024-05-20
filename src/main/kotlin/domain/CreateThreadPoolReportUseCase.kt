package domain

import domain.model.report.ThreadPoolReport
import domain.model.slice.Slice
import domain.model.thread.Thread
import domain.model.thread.ThreadState

object CreateThreadPoolReportUseCase {

    fun create(threads: List<Thread>): ThreadPoolReport {
        var totalAwaitTimeMilliSec = 0L
        var runningSliceCount = 0
        val awaitTimes = mutableListOf<Long>()
        val awaitTimesByThreads = mutableMapOf<String, MutableList<Long>>()
        val durationSlices = mutableListOf<Long>()
        val betweenTimes = mutableListOf<Long>()
        val stateDurationsByThread = mutableMapOf<String, Map<String, Long>>()
        val stateDurationsBySlice = mutableMapOf<String, Map<Slice, Map<String, Long>>>()
        threads.forEach { thread ->
            if (thread.slices.isEmpty()) {
                return@forEach
            }
            runningSliceCount += thread.slices.count { slice -> !slice.isWaiting }
            val firstSlice = thread.slices.first()
            thread.slices.forEach { slice ->
                when (slice.isWaiting) {
                    false -> {
                        val waitingSlices = mutableListOf<Slice>()
                        threads.forEach { thread ->
                            val waitingSlice = findNearestWaitingSlice(thread.slices, slice)
                            waitingSlice?.let {
                                waitingSlices.add(waitingSlice)
                            }
                        }
                        val endWaitingSlice = waitingSlices.maxOfOrNull { waitingSlice ->
                            waitingSlice.end
                        }
                        val awaitTime = endWaitingSlice?.let { endWaitingSlice ->
                            slice.start - endWaitingSlice
                        } ?: run {
                            if (slice != firstSlice) {
                                slice.start - firstSlice.start
                            } else {
                                0L
                            }
                        }
                        awaitTimes.add(awaitTime)
                        putAwaitTimeByThread(thread.name, awaitTimesByThreads, awaitTime)
                        durationSlices.add(slice.duration)
                        totalAwaitTimeMilliSec += awaitTime
                    }

                    true -> {
                        betweenTimes.add(slice.duration)
                    }
                }
            }
            val stateDurationsForThread = calculateStatesDuration(thread.states)
            val stateDurationsForSlice = calculateStatesDurationForRunningSlice(thread)
            stateDurationsByThread[thread.name] = stateDurationsForThread
            stateDurationsBySlice[thread.name] = stateDurationsForSlice
        }


        return ThreadPoolReport(
            threadCount = threads.size,
            averageAwaitTime = (totalAwaitTimeMilliSec / runningSliceCount),
            totalAwaitTimeMilliSec = totalAwaitTimeMilliSec,
            runningSliceCount = runningSliceCount,
            awaitTimes = awaitTimes,
            awaitTimesByThreads = awaitTimesByThreads,
            runningSliceDurations = durationSlices,
            runningSliceBetweenTimes = betweenTimes,
            stateDurationsBySlice = stateDurationsBySlice,
            stateDurationsByThread = stateDurationsByThread
        )
    }


    private fun putAwaitTimeByThread(
        name: String, awaitTimesByThreads: MutableMap<String, MutableList<Long>>, time: Long
    ) {
        if (awaitTimesByThreads.contains(name)) {
            awaitTimesByThreads[name]?.add(time)
        } else {
            val times = mutableListOf(time)
            awaitTimesByThreads[name] = times
        }
    }

    private fun findNearestWaitingSlice(
        slices: List<Slice>, runningSlice: Slice
    ): Slice? {
        return slices.lastOrNull { slice ->
            slice.isWaiting && slice.end <= runningSlice.start && slice.duration > Constants.MAX_GET_TASK_DURATION_MICRO_SEC
        }
    }

    private fun calculateStatesDuration(threadStates: List<ThreadState>): Map<String, Long> {
        val stateDurations = mutableMapOf<String, Long>()
        threadStates.forEach { threadState ->
            val key = threadState.state
            if (stateDurations.contains(key)) {
                var duration = stateDurations[key] ?: 0
                duration += threadState.duration
                stateDurations[key] = duration
            } else {
                stateDurations[key] = threadState.duration
            }
        }
        return stateDurations
    }

    private fun calculateStatesDurationForRunningSlice(thread: Thread): Map<Slice, Map<String, Long>> {
        val stateDurationsBySlice = mutableMapOf<Slice, Map<String, Long>>()
        thread.slices.filter { slice -> !slice.isWaiting }.forEach { slice ->
            val stateDurations = mutableMapOf<String, Long>()
            val threadStates = getThreadStatesForSlice(slice, thread.states)
            threadStates.forEach { threadState ->
                var threadStateDuration = 0L
                if (threadState.start <= slice.start && threadState.end < slice.end && threadState.end > slice.start) {
                    threadStateDuration = threadState.end - slice.start
                }
                if (threadState.start > slice.start && threadState.end < slice.end) {
                    threadStateDuration = threadState.duration
                }
                if (threadState.start > slice.start && threadState.end >= slice.end && threadState.start < slice.end) {
                    threadStateDuration = slice.end - threadState.start
                }
                if (threadState.start < slice.start && threadState.end > slice.end) {
                    threadStateDuration = slice.duration
                }
                if (threadState.start == slice.start && threadState.end == slice.end) {
                    threadStateDuration = threadState.duration
                }

                val key = threadState.state
                if (stateDurations.contains(key)) {
                    var duration = stateDurations[key] ?: 0
                    duration += threadStateDuration
                    stateDurations[key] = duration
                } else {
                    stateDurations[key] = threadStateDuration
                }
            }

            stateDurationsBySlice[slice] = stateDurations
        }
        return stateDurationsBySlice
    }

    private fun getThreadStatesForSlice(slice: Slice, threadStates: List<ThreadState>): List<ThreadState> {
        return threadStates
    }

}