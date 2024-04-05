package domain.model.thread

import kotlinx.serialization.Serializable

@Serializable
data class ThreadState(
    val start: Long,
    val end: Long,
    val state: String
) {
    val duration by lazy { end - start }
    override fun toString(): String {
        return "ThreadState(start=$start, end=$end, state=$state)"
    }
}