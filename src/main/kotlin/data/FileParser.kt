package data

object FileParser {
    fun getJson() = this::class.java.classLoader
        .getResource("lockcontention/lockcontention-vk-toggles-writer.json")?.readText()
    ?: ""
}