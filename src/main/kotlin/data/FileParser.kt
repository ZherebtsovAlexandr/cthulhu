package data

object FileParser {
    fun getJson() = this::class.java.classLoader
        .getResource("vk-network-imag-unboun.json")?.readText()
        ?: ""
}