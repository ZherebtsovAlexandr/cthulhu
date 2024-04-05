import data.FileParser
import data.JsonParser
import domain.CreateThreadPoolReportUseCase
import domain.FillWaitingSlicesUseCase
import domain.ThreadPoolReportPrinter
import domain.model.thread.Thread

@Suppress("UNCHECKED_CAST")
fun main(args: Array<String>) {
    val json = FileParser.getJson()
    val parsedThreads = JsonParser.parseThreads(json)
    val threads = mutableListOf<Thread>()
    parsedThreads.forEach { parsedThread ->
        val prepared = FillWaitingSlicesUseCase.fill(parsedThread.slices)
        val thread = parsedThread.copy(slices = prepared)
        threads.add(thread)
    }

    val threadPoolReport = CreateThreadPoolReportUseCase.create(threads)
    ThreadPoolReportPrinter.print(threadPoolReport)
}