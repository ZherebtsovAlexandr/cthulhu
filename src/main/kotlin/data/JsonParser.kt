package data

import domain.model.thread.Thread
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object JsonParser {

    private val parser = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(
        Thread.serializer()
    )

    fun parseThreads(json: String): List<Thread> {
        return parser.decodeFromString(listSerializer, json)
    }
}