package domain.model.report

import domain.model.slice.Slice

data class LockContentionByThreadReport(
    val threadName: String = "",
    val totalLockContentionDuration: Long,
    val ownerLockSlices: Map<String, List<Slice>>,
)