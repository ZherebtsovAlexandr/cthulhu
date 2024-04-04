package domain.model.thread

import domain.model.slice.Slice

data class Thread(
    val name: String,
    val slices: List<Slice>
)