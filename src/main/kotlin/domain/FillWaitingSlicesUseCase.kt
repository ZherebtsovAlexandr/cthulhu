package domain

import domain.model.slice.Slice

object FillWaitingSlicesUseCase {
    fun fill(runningSlices: List<Slice>): List<Slice> {
        val slices = mutableListOf<Slice>()
        var previousRunningSlice: Slice? = null
        runningSlices
            .filter { runningSlice -> isCorrectSlice(runningSlice.start, runningSlice.end) }
            .forEach { currentRunningSlice ->
                previousRunningSlice?.let { previousRunningSlice ->
                    val waitingSlice = createWaitingSlice(previousRunningSlice, currentRunningSlice)
                    slices.add(waitingSlice)
                }
                slices.add(currentRunningSlice)
                previousRunningSlice = currentRunningSlice
            }
        return slices
    }

    private fun isCorrectSlice(start: Long, end: Long) = start <= end

    private fun createWaitingSlice(previous: Slice, current: Slice) = Slice(
        start = previous.end,
        end = current.start,
        isWaiting = true
    )
}