package domain

import domain.model.slice.Slice
import domain.model.thread.Thread
import java.lang.NumberFormatException

object GetLockContentionsUseCase {

    fun get(targetThread: Thread, otherThreads: List<Thread>): List<LockContention> {
        var ownerLockSlice: Slice? = null
        var ownerLockThreadName: String? = null
        val lockContentionSlices = targetThread
            .slices
            .filter { slice -> slice.name.lowercase().contains("lock contention") }

        val lockContentions = mutableListOf<LockContention>()
        lockContentionSlices.forEach { lockContentionSlice: Slice ->
            val tid = getOwnerTid(lockContentionSlice.name)
            tid?.let { tid ->
                val ownerLockContentionThread = otherThreads.firstOrNull() { thread ->
                    thread.id == tid
                }
                ownerLockThreadName = ownerLockContentionThread?.name
                ownerLockSlice = ownerLockContentionThread?.slices?.firstOrNull() { ownerSlice ->
                    val duration = findIntersectDuration(
                        lockContentionSlice.start,
                        lockContentionSlice.end,
                        ownerSlice.start,
                        ownerSlice.end
                    )
                    duration > 0
                }
            }
            lockContentions.add(
                LockContention(
                    name = lockContentionSlice.name,
                    start = lockContentionSlice.start,
                    end = lockContentionSlice.end,
                    ownerLockThreadId = tid,
                    ownerLockThreadName = "$ownerLockThreadName-$tid",
                    ownerLockSlice = ownerLockSlice
                )
            )
        }
        return lockContentions
    }

    private fun getOwnerTid(name: String): Int? {
        val regex = """owner tid: (\d+)""".toRegex()
        val matchResult = regex.find(name)
        val ownerTid = if (matchResult != null) {
            try {
                matchResult.groupValues[1].toInt()
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
        return ownerTid
    }

    private fun findIntersectDuration(
        start1: Long,
        end1: Long,
        start2: Long,
        end2: Long
    ): Long {
        val intersectStart = maxOf(start1, start2)
        val intersectEnd = minOf(end1, end2)
        return if (intersectEnd > intersectStart) {
            intersectEnd - intersectStart
        } else {
            0L
        }
    }
}