package domain

import domain.model.slice.Slice
import util.nanoToMicros

data class LockContention(
    val name: String,
    val start: Long,
    val end: Long,
    val sliceWhenLockContentionOccurred: Slice? = null,
    val ownerLockThreadId: Int? = null,
    val ownerLockThreadName: String? = null,
    val ownerLockSlice: Slice? = null
) {
    val duration by lazy { end - start }
    override fun toString(): String {
        return "LockContention(dur=${duration.nanoToMicros()})"
    }
}