package data

import domain.model.slice.RunningSlice
import domain.model.thread.Thread
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object JsonParser {

    private val parser = Json { ignoreUnknownKeys = true }
    private val mapSerializer = MapSerializer(
        String.serializer(),
        ListSerializer(RunningSlice.serializer())
    )

    fun parseThreads(json: String): List<Thread> {
        return parser.decodeFromString(mapSerializer, json)
            .map { entry ->
                Thread(
                    name = entry.key,
                    slices = entry.value
                )
            }
    }
}