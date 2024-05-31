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