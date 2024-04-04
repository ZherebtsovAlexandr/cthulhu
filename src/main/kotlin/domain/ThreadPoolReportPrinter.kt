package domain

import domain.model.report.ThreadPoolReport
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
        println("Average await time: $averageAwaitTime ms")
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

}