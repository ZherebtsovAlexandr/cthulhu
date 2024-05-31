package domain

import domain.model.report.LockContentionByThreadReport
import domain.model.report.ThreadPoolReport
import domain.model.slice.Slice
import util.microToMillis
import util.nanoToMicros

object LockContentionReportPrinter {

    fun print(report: LockContentionByThreadReport) {
        printLockContentionByThreadReport(report)
    }

    private fun printLockContentionByThreadReport(report: LockContentionByThreadReport) {
        println("Thread name: ${report.threadName}")
        println("Total LockContention duration: ${report.totalLockContentionDuration.nanoToMicros().microToMillis()} ms")
        println("Owner lock slices:")
        report.ownerLockSlices.forEach { sliceEntry ->
            println("   Owner lock thread ${sliceEntry.key}")
            sliceEntry.value.forEach { slice ->
                println("       Slice ${slice.name} (${slice.duration.nanoToMicros().microToMillis()})")
            }
        }
    }
}