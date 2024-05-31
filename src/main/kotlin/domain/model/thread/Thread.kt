package domain.model.thread

import domain.model.slice.Slice
import kotlinx.serialization.Serializable

@Serializable
data class Thread(
    val id: Int,
    val name: String,
    val slices: List<Slice> = listOf(),
    val states: List<ThreadState> = listOf()
)