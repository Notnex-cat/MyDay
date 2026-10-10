# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

MyDay — Android-приложение (Kotlin, Jetpack Compose, один модуль `:app`): дневник дня с оценкой, заметкой, отзывом нейросети и расписанием, которое нейросеть генерирует по описанию.

## Режим работы

Проект учебный: владелец учится на нём Android-разработке, а большая часть кода написана ИИ.

- В разговорах о коде объяснять существующий код и проверять понимание, а не переписывать его. Если просят фичу — сначала предложить написать самому и прийти на ревью.
- Просьбы «сделай» про настройку, сборку и Firebase выполнять как обычно.
- Вопросы на проверку задавать по одному, начиная с того, что видно прямо в коде. После ответа явно говорить, что верно, что неточно и как правильно.

## Команды

```
./gradlew assembleDebug                 # сборка
./gradlew installDebug                  # сборка и установка на подключённое устройство
./gradlew lintDebug                     # lint
./gradlew testDebugUnitTest             # unit-тесты
./gradlew testDebugUnitTest --tests "com.notnex.myday.ExampleUnitTest"   # один тест
./gradlew connectedDebugAndroidTest     # инструментальные тесты, нужно устройство
./gradlew signingReport                 # отпечатки ключей подписи для Firebase
```

- `java` в `PATH` нет. Gradle берёт JDK из `JAVA_HOME=/opt/android-studio/jbr`; если переменная пуста, подставлять её перед командой.
- Тестов по сути нет: в `test/` и `androidTest/` лежат только шаблонные примеры.

## Файлы вне git

Без них проект не собирается или не работает, в репозитории их нет:

- `app/google-services.json` — конфигурация Firebase, без неё падает плагин google-services.
- `local.properties` — `sdk.dir` и `nn_key` (ключ Mistral API, попадает в `BuildConfig.NN_API_KEY`). Без `nn_key` сборка проходит, но запросы к нейросети возвращают 401.

## Архитектура

Слои сверху вниз: экран Compose → ViewModel → `MyDayRepository` → Room и Firestore.

### Данные и синхронизация

- **Room — источник правды.** Экраны читают только `Flow` из Room. Firestore получает копии изменений и присылает чужие.
- **Запись** идёт через `MyDayRepository`: обёртка `write { }` (общий `Mutex` + `NonCancellable`) пишет в Room, затем `pushInBackground` отправляет копию в облако в `@ApplicationScope`. Экран часто закрывается сразу после нажатия «Сохранить», поэтому запись не должна зависеть от `viewModelScope`.
- **Обратный поток** делает `SyncManager`. Он стартует в `MyDayApp.onCreate` и привязан к аккаунту, а не к экрану: при входе слушает снапшоты Firestore и сливает их в Room, при выходе или смене аккаунта очищает локальную базу.
- **Конфликты** решаются по `lastUpdated`: побеждает более поздняя запись. Первую выгрузку локальных данных `SyncManager` делает только после первого серверного снапшота, не из кэша.
- **Структура Firestore** известна только `CloudDataSource`: `users/{uid}/days/{yyyy-MM-dd}` и `users/{uid}/schedule/{id}`.
- **Имена полей DTO** в `data/remote/FirebaseDTO.kt` закрыты keep-правилом в `proguard-rules.pro` и совпадают с полями документов. Переименование ломает уже сохранённые данные.

### Особенности модели

- У `ScheduleEntity` внешний ключ на `MyDayEntity`. Перед вставкой пункта `ensureDay` создаёт день-заглушку с `lastUpdated = 0`; такая строка в облако не выгружается и настоящий день не затирает.
- Id пункта расписания детерминированный, `YYYYMMDD_HHmm`: повторная генерация на то же время обновляет пункт, а не создаёт дубль.
- База собирается с `fallbackToDestructiveMigration()`: при смене схемы локальные данные стираются и у вошедшего пользователя возвращаются из облака. При правке сущности поднимать `version` в `MyDayDataBase`; схемы экспортируются в `app/schemas`.
- Новое поле проходит через четыре места: сущность, DTO, `Mappers.kt`, версия базы.

### ViewModel и навигация

- Один `NavHost` в `MainActivity`, маршруты — `@Serializable`-классы в `ui/navigation/Routes.kt`.
- `hiltViewModel()` даёт каждому экрану свой экземпляр `MyDayViewModel`. Общего состояния между экранами через него нет: дата и id передаются аргументами маршрута, общие данные живут в репозитории-синглтоне.
- Заметка сохраняется с задержкой 1,5 с после последнего символа, остаток дописывается в `onCleared` через `saveNoteInBackground`.

### Нейросеть и вход

- `MistralRepository` ходит в `api.mistral.ai` через OkHttp. Расписание — обычный запрос, ответ разбирается как JSON-массив; отзыв на день — SSE-стрим, текст копится в `NNViewModel`.
- `AuthRepository` — Firebase Auth (почта и Google One Tap). Его `authState` — единственный источник сведений о текущем пользователе; на нём держится `SyncManager`.
- У варианта `release` нет signing config. Отпечатки debug-ключа в Firebase уже добавлены; для release-ключа их придётся добавить отдельно.

## Стиль

- Комментарии в коде на русском и объясняют причину, а не действие.
- Строки интерфейса добавлять сразу в `res/values/strings.xml` и `res/values-ru/strings.xml`.
- В `catch (e: Exception)` вокруг suspend-кода `CancellationException` ловится отдельно и пробрасывается дальше.
