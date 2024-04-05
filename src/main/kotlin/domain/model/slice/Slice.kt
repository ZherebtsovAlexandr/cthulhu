package domain.model.slice

import kotlinx.serialization.Serializable
import util.nanoToMicros

@Serializable
data class Slice(
    val name: String = "",
    val start: Long = 0L,
    val end: Long = 0L,
    val isWaiting: Boolean = false
) {
    val duration by lazy { end - start }
    override fun toString(): String {
        return "WaitingSlice(start=$start, end=$end, isWaiting=$isWaiting, dur=${duration.nanoToMicros()})"
    }
}