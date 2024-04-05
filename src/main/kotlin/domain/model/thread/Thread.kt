package domain.model.thread

import domain.model.slice.Slice
import kotlinx.serialization.Serializable

@Serializable
data class Thread(
    val name: String,
    val slices: List<Slice>,
    val states: List<ThreadState>
)