package data

object FileParser {
    fun getJson() = this::class.java.classLoader
        .getResource("vk-single-threa-io_poo_v2.json")?.readText()
        ?: ""
}