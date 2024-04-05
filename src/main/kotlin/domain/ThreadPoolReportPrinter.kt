package domain

import domain.model.report.ThreadPoolReport
import domain.model.slice.Slice
import util.microToMillis
import util.nanoToMicros

object ThreadPoolReportPrinter {

    fun print(report: ThreadPoolReport) {

        printThreadPoolName(report.threadPoolName)

        printThreadCount(report.threadCount)

        printRunningSliceCount(report.runningSliceCount)

        printPercentilesForRunningSliceDurations(report.runningSliceDurations)

        printPercentilesForRunningSliceBetweenTimes(report.runningSliceBetweenTimes)

        printPercentilesForAwaitTimes(report.awaitTimes)

        printTop("Top 50 long await", 50, report.awaitTimes)
        printTop("Top 50 long durations", 50, report.runningSliceDurations)

        printAverageAwaitTime(report.averageAwaitTime)

        printDurationByThread(report.stateDurationsByThread)

        printDurationBySlice(report.stateDurationsBySlice)
    }

    private fun printDurationByThread(durations: Map<String, Map<String, Long>>) {
        println("Thread states by thread:")
        durations.forEach { threadEntry ->
            var totalDuration = 0L
            threadEntry.value.forEach { stateEntry ->
                totalDuration += stateEntry.value
            }
            println("Thread - ${threadEntry.key}")
            threadEntry.value.forEach { stateEntry ->
                val percent = (stateEntry.value * 100).toDouble() / totalDuration.toDouble()
                println("State ${stateEntry.key} - ${String.format("%.2f", percent)} %")
            }
        }
    }

    private fun printDurationBySlice(durations: Map<String, Map<Slice, Map<String, Long>>>) {
        println("Thread states by slice:")

        durations.forEach { threadEntry ->
            println("Thread - ${threadEntry.key}")

            val slicesRunningStatePercents = mutableListOf<Long>()
            threadEntry.value.forEach { sliceEntry ->
                var totalDurationBySlice = 0L
                var runningDurationBySlice = 0L
                sliceEntry.value.forEach { stateEntry ->
                    totalDurationBySlice += stateEntry.value
                    if (stateEntry.key == PerfettoThreadState.Running.string) {
                        runningDurationBySlice += stateEntry.value
                    }
                }
                val percentOfRunning = runningDurationBySlice * 100 / totalDurationBySlice
                slicesRunningStatePercents.add(percentOfRunning)
            }

            val sortedSlicesRunningStatePercents = sort(slicesRunningStatePercents)
            println("Running percentiles")
            printPercentilesPercents(sortedSlicesRunningStatePercents)
        }

    }


    private fun printThreadPoolName(name: String) {
        println("ThreadPoolName is $name")
    }

    private fun printThreadCount(count: Int) {
        println("Threads in pool: $count")
    }

    private fun printRunningSliceCount(count: Int) {
        println("Total running slices: $count")
    }

    private fun printAverageAwaitTime(averageAwaitTime: Long) {
        println("Average await time: ${averageAwaitTime.nanoToMicros().microToMillis()} ms")
    }

    private fun printPercentilesForRunningSliceDurations(durations: List<Long>) {
        val sortedDurations = sort(durations)
        println("Duration slices percentiles")
        printPercentiles(sortedDurations)
    }

    private fun printPercentilesForRunningSliceBetweenTimes(times: List<Long>) {
        val sortedTimes = sort(times)
        println("Between times percentiles")
        printPercentiles(sortedTimes)
    }

    private fun printPercentilesForAwaitTimes(times: List<Long>) {
        val sortedTimes = sort(times)
        println("Await times percentiles")
        printPercentiles(sortedTimes)
    }

    private fun printTop(text: String, count: Int, list: List<Long>) {
        val sortedList = sort(list).reversed()
        val topList = sortedList
            .take(count)
            .joinToString(", ") {
                "${it.nanoToMicros().microToMillis()}ms"
            }
        println("$text: $topList")
    }

    private fun <T : Comparable<T>> sort(list: List<T>): List<T> {
        val mutableList = list.toMutableList()
        mutableList.sort()
        return mutableList
    }

    private fun printPercentiles(list: List<Long>) {
        val medianIndex = (list.size * 0.5).toInt()
        val percentile75Index = (list.size * 0.75).toInt()
        val percentile90Index = (list.size * 0.90).toInt()
        val percentile99Index = (list.size * 0.99).toInt()
        println("50: ${list[medianIndex].nanoToMicros().microToMillis()} ms")
        println("75: ${list[percentile75Index].nanoToMicros().microToMillis()} ms")
        println("90: ${list[percentile90Index].nanoToMicros().microToMillis()} ms")
        println("99: ${list[percentile99Index].nanoToMicros().microToMillis()} ms")
    }

    private fun printPercentilesPercents(list: List<Long>) {
        val medianIndex = (list.size * 0.5).toInt()
        val percentile75Index = (list.size * 0.75).toInt()
        val percentile90Index = (list.size * 0.90).toInt()
        val percentile99Index = (list.size * 0.99).toInt()
        println("50: ${list[medianIndex]}")
        println("75: ${list[percentile75Index]}")
        println("90: ${list[percentile90Index]}")
        println("99: ${list[percentile99Index]}")
    }
}