В качестве входных данных программа использует json c описание задач, выполняемых на потоках.

В директорию `resources` необходимо положить json в формате:

```json
[
    {
        "name": "pool-thread-1",
        "slices": [
            {
                "name": "Some work",
                "start": 4792506518038,
                "end": 4792506556327
            },
            {
                "name": "Some work",
                "start": 4794320659722,
                "end": 4795719636814
            },
            {
                "name": "Some work",
                "start": 4795719740411,
                "end": 4799420889867
            }
        ],
        "states": [
            {
                "state": "Running",
                "start": 4792506302909,
                "end": 4792506686495
            },
            {
                "state": "S",
                "start": 4792506686495,
                "end": 4794320606499
            },
            {
                "state": "R",
                "start": 4794320606499,
                "end": 4794320625786
            }
        ]
    },
    {
     ...
    }
]
```
**Json файл должен содержать только потоки из одного ThreadPool.**

Который представляет собой массив объектов, где каждый объект это отдельный поток. Объект поток содержит массив из 
объектов Slice и массив из объектов State.

Slice в терминах Perfetto - это какое-то событие в таймлайне потока, у него есть начало и конец.

На самом деле, окончания события может и не быть, если запись трейса прервалась во время выполнения какой-нибудь функции.
Такие слайсы в проекте отфильтровываются методом:
```kotlin
    private fun isCorrectSlice(start: Long, end: Long) = start <= end
```

State описывает состояние потока в конкретный интервал времени, все состояния перечисленны в классе PerfettoThreadState.


Программа находит для каждой задачи (для каждого Slice в терминах Perfetto) время ожидания задачи в очереди пула потоков.

Алгоритм реализован в классе `CreateThreadPoolReportUseCase`.

### Обработка задач
Для удобства все `Slice` можно разделить на два вида: активные задачи и задачи ожидания, разделя их по признаку (`isWaiting`).

В методе `findNearestWaitingSlice` стоит обратить внимание на константу `Constants.MAX_GET_TASK_DURATION_MICRO_SEC` - 
по этой константе определяется, поток ничего не делает потому что запаркован или он не выполняет какую-либо работу так как 
в этот момент достает следующую задачу из очереди.

Эта константа подбиралась опытным путем, после изучения данных семплирующего профайлера,
значительно повысить точно определения момента, когда поток берет следующую задачу можно путем анализа состояния потока.
Если коротко - поток в состоянии Running - значит берет задачу, иначе запаркован.

Для каждого `Slice` в потоке проверяется, что `Slice` не является ожидающей задачей (`isWaiting`).
- Если `Slice` не ожидает:
    - Создается список ожидающих задач (`waitingSlices`).
    - Для каждой задачи ищется ближайшая ожидающая задача (`findNearestWaitingSlice`).
    - Вычисляется время ожидания (`awaitTime`) как разница между началом текущей задачи и окончанием самой поздней ожидающей задачи.
    - Время ожидания добавляется в список (`awaitTimes`) и ассоциируется с потоком (`putAwaitTimeByThread`).
    - Длительность задачи добавляется в список (`durationSlices`), и общее время ожидания увеличивается (`totalAwaitTimeMilliSec`).

- Если задача ожидает, ее длительность добавляется в другой список (`betweenTimes`).

### Вычисление длительности состояний
- Для каждого потока вычисляется длительность его состояний (`calculateStatesDuration`) и ассоциируется с именем потока.
- Аналогично вычисляется длительность состояний для активных задач (`calculateStatesDurationForRunningSlice`) и также ассоциируется с именем потока.

Этот алгоритм позволяет отслеживать время, которое каждая задача проводит в ожидании перед выполнением, что может быть полезно для анализа производительности пула потоков и оптимизации распределения ресурсов.


### Импорт Json из .perfetto-trace файла

`com.example.app` - название процесса

`PrefixForSlice` - если задачи имеют определенный префикс, то можно его указать здесь, чтобы отфильтровать лишние `Slice`.

`poolName` - необходимо, чтобы потоки в приложении из одного пула имели одинаковый префикс, например io-pool-thread.

```kotlin
   import androidx.benchmark.perfetto.*;

   @OptIn(ExperimentalPerfettoTraceProcessorApi::class, ExperimentalPerfettoCaptureApi::class)
    private fun saveAsJson(filesDir: String, traceFile: String, poolName: String, jsonFileName: String) {
        PerfettoTraceProcessor.runServer {
            loadTrace(PerfettoTrace("$filesDir/${traceFile}")) {

                val threadStateRows = query(
                    "SELECT " +
                        "    thread.tid as thread_tid, " +
                        "    thread_state.id as id, " +
                        "    thread.id as thread_id, " +
                        "    thread.name as thread_name, " +
                        "    thread_state.state as state, " +
                        "    thread_state.ts as start, " +
                        "    thread_state.dur as duration " +
                        "FROM thread_state " +
                        "    INNER JOIN thread on thread_state.utid = thread.id" +
                        "    INNER JOIN process USING(upid)" +
                        "WHERE" +
                        "    thread_name LIKE \"$poolName%\"" +
                        "    AND process.name LIKE \"com.example.app%\"" +
                        "ORDER BY start  "
                )

                val sliceRows = query(
                    "SELECT "
                        + "thread.tid as thread_id, "
                        + "thread.name as thread_name, "
                        + "slice.name as slice_name, "
                        + "slice.ts, "
                        + "slice.dur "
                        + "FROM slice "
                        + "INNER JOIN thread_track on slice.track_id = thread_track.id "
                        + "INNER JOIN thread USING(utid) "
                        + "INNER JOIN process USING(upid) "
                        + "WHERE "
                        + "thread.name LIKE  \"$poolName%\" AND "
                        + "slice.name LIKE \"PrefixForTask%\" AND "
                        + "process.name LIKE \"com.example.app%\"".trimMargin()
                )


                val threadStatesRowsGroupedByThread = threadStateRows
                    .groupBy { row ->
                        row.string("thread_name") + row.long("thread_tid")
                    }

                val threadStatesGroupedByThread = mutableMapOf<String, List<ThreadState>>()

                threadStatesRowsGroupedByThread.forEach { entry ->
                    val threadName = entry.key
                    val threadStates = entry.value.map { row ->
                        val start = row.long("start")
                        val dur = row.long("duration")
                        val state = row.string("state")
                        val threadState = ThreadState(
                            start = start,
                            end = start + dur,
                            state = state
                        )
                        threadState
                    }
                    threadStatesGroupedByThread[threadName] = threadStates
                }


                val slicesRowsGroupedByThread = sliceRows.groupBy { row ->
                    row.string("thread_name") + row.long("thread_id")
                }

                val threads = slicesRowsGroupedByThread.map { entry ->
                    val threadName = entry.key
                    val slices = entry.value.map { row ->
                        val start = row.long("ts")
                        val dur = row.long("dur")
                        val name = row.string("slice_name")
                        val runningSlice = Slice(
                            start = start,
                            end = start + dur,
                            name = name,
                            isWaiting = false
                        )
                        runningSlice
                    }
                    PerfettoThread(
                        id = 0, //just hardcode, not important stuff but this issue
                        name = threadName,
                        slices = slices,
                        states = threadStatesGroupedByThread[threadName] ?: listOf()
                    )
                }


                val json = Json.encodeToString(threads)
                val dir = File(filesDir, "traces")
                if (!dir.exists()) {
                    dir.mkdir()
                }
                try {
                    val jsonFile = File(dir, "$poolName-${jsonFileName}.json")
                    val writer = FileWriter(jsonFile)
                    writer.append(json)
                    writer.flush()
                    writer.close()
                    println("Saved json file here $filesDir/traces/$poolName-${jsonFileName}.json")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
```

Вспомогательные классы модели

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class Thread(
    val id: Int,
    val name: String,
    val slices: List<Slice> = listOf(),
    val states: List<ThreadState> = listOf()
)

@Serializable
data class Slice(
  val name: String = "",
  val isWaiting: Boolean = false,
  val start: Long = 0L,
  val end: Long = 0L,
)

@Serializable
data class ThreadState(
  val state: String,
  val start: Long,
  val end: Long
)
```

