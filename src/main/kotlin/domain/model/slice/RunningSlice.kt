package domain.model.slice

import kotlinx.serialization.Serializable
import util.nanoToMicros

@Serializable
data class RunningSlice(
    override val start: Long,
    override val end: Long
) : Slice() {
    override fun toString(): String {
        return "RunningSlice(start=$start, end=$end, dur=${dur.nanoToMicros()})"
    }
}