package data

object FileParser {
    fun getJson() = this::class.java.classLoader
        .getResource("scenario/vk-network-imag-search_100_3g.json")?.readText()
    ?: ""
}