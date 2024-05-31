import data.FileParser
import data.JsonParser
import domain.*
import domain.model.thread.Thread

@Suppress("UNCHECKED_CAST")
fun main(args: Array<String>) {
    val json = FileParser.getJson()
    val parsedThreads = JsonParser.parseThreads(json)
    //threadPoolReport(parsedThreads)
    lockContentionReport(parsedThreads)
}

private fun threadPoolReport(parsedThreads: List<Thread>) {
    val threads = mutableListOf<Thread>()

    parsedThreads.forEach { parsedThread ->
        val prepared = FillWaitingSlicesUseCase.fill(parsedThread.slices)
        val thread = parsedThread.copy(slices = prepared)
        threads.add(thread)
    }

    val threadPoolReport = CreateThreadPoolReportUseCase.create(threads)
    ThreadPoolReportPrinter.print(threadPoolReport)
}

private fun lockContentionReport(parsedThreads: List<Thread>) {
    val targetThread = parsedThreads.first()
    val lockContentions = GetLockContentionsUseCase.get(targetThread, parsedThreads)
    val lockContentionReport = CreateLockContentionReportUseCase.create(targetThread.name, lockContentions)
    LockContentionReportPrinter.print(lockContentionReport)
}