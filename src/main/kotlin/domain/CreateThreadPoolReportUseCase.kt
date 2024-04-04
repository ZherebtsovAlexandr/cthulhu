package domain

import domain.model.report.ThreadPoolReport
import domain.model.slice.RunningSlice
import domain.model.slice.Slice
import domain.model.slice.WaitingSlice
import domain.model.thread.Thread
import util.microToMillis
import util.nanoToMicros

object CreateThreadPoolReportUseCase {

    fun create(threads: List<Thread>): ThreadPoolReport {
        var totalAwaitTimeMilliSec = 0L
        var runningSliceCount = 0
        val awaitTimes = mutableListOf<Long>()
        val durationSlices = mutableListOf<Long>()
        val betweenTimes = mutableListOf<Long>()
        threads.forEach { thread ->
            if (thread.slices.isEmpty()) {
                return@forEach
            }
            runningSliceCount += thread.slices.count { slice -> slice is RunningSlice }
            val firstSlice = thread.slices.first()
            thread.slices.forEach { slice ->
                when (slice) {
                    is RunningSlice -> {
                        val waitingSlices = mutableListOf<WaitingSlice>()
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
                        durationSlices.add(slice.dur)
                        totalAwaitTimeMilliSec += awaitTime
                    }

                    is WaitingSlice -> {
                        betweenTimes.add(slice.dur)
                    }
                }
            }
        }

        return ThreadPoolReport(
            threadCount = threads.size,
            averageAwaitTime = (totalAwaitTimeMilliSec / runningSliceCount).nanoToMicros().microToMillis(),
            totalAwaitTimeMilliSec = totalAwaitTimeMilliSec,
            runningSliceCount = runningSliceCount,
            awaitTimes = awaitTimes,
            runningSliceDurations = durationSlices,
            runningSliceBetweenTimes = betweenTimes
        )

    }

    private fun findNearestWaitingSlice(
        slices: List<Slice>,
        runningSlice: RunningSlice
    ): WaitingSlice? {
        return slices.lastOrNull { slice ->
            slice is WaitingSlice
                    && slice.end <= runningSlice.start
                    && slice.dur > Constants.MAX_GET_TASK_DURATION_MICRO_SEC
        } as WaitingSlice?
    }

}