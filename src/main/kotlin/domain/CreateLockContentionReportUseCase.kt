package domain

import domain.model.report.LockContentionByThreadReport

object CreateLockContentionReportUseCase {

    fun create(threadName: String, lockContentions: List<LockContention>): LockContentionByThreadReport {
        val totalLockContentionDuration = lockContentions.sumOf { lockContention ->
            lockContention.duration
        }
        val ownerLockSlices = lockContentions
            .groupBy { lockContention ->
                lockContention.ownerLockThreadName
            }
            .filter { entry ->
                entry.key != null
            }
            .map { lockContentionEntry ->
                Pair(
                    lockContentionEntry.key ?: "",
                    lockContentionEntry.value.mapNotNull { lockContention -> lockContention.ownerLockSlice }
                )
            }
            .toMap()
        return LockContentionByThreadReport(
            threadName = threadName,
            totalLockContentionDuration = totalLockContentionDuration,
            ownerLockSlices = ownerLockSlices
        )
    }

}
