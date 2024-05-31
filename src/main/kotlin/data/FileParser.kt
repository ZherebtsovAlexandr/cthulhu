package data

object FileParser {
    fun getJson() = this::class.java.classLoader
        .getResource("test.json")?.readText()
    ?: ""
}