package domain.model.slice

import util.nanoToMicros

data class WaitingSlice(
    override val start: Long,
    override val end: Long
) : Slice() {
    override fun toString(): String {
        return "WaitingSlice(start=$start, end=$end, dur=${dur.nanoToMicros()})"
    }
}
