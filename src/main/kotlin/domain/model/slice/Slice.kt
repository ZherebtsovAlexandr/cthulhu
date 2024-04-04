package domain.model.slice

open class Slice {
    val dur by lazy { end - start }
    open val start: Long = 0L
    open val end: Long = 0L
}
