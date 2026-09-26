# Thoughts

Android-приложение для быстрых заметок с тегами: пишешь мысль, добавляешь `#тег` прямо в тексте — и потом находишь её по дню, тегу или поиску.

## Возможности

- Быстрый ввод мыслей с автоматическим извлечением `#тегов` из текста
- Лента, сгруппированная по дням (сегодня / вчера / дата)
- Поиск по тексту и тегам, фильтр по тегу через чипы
- Свайп для удаления одной мысли с отменой через снекбар
- Режим множественного выбора (долгий тап): удаление, копирование текста в буфер, массовое добавление/снятие тега у выбранных
- Drawer с настройками: переключение темы (Система / Светлая / Тёмная) и языка интерфейса (RU / EN)
- Диалог «О проекте» с версией приложения

## Стек

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Voyager** — навигация (`Screen`) и `ScreenModel` вместо `ViewModel`
- **Koin** — dependency injection
- **Room** — локальное хранилище мыслей и тегов
- **DataStore Preferences** — хранение настроек (тема)
- **AndroidX AppCompat** — per-app language через `AppCompatDelegate`

## Структура проекта

```
app/src/main/java/dev/thoughts/app/
├── MainActivity.kt                 # AppCompatActivity, точка входа, применяет тему
├── ThoughtsApplication.kt          # инициализация Koin
├── data/
│   ├── AppDatabase.kt              # Room-база
│   ├── ThoughtDao.kt               # запросы к мыслям и тегам
│   ├── Entities.kt                 # Thought, ThoughtTag, ThoughtWithTags, TagCount
│   ├── TagParser.kt                # разбор #тегов из текста
│   └── settings/
│       ├── SettingsRepository.kt   # DataStore-хранилище настроек
│       └── ThemeMode.kt            # SYSTEM / LIGHT / DARK
├── di/
│   └── AppModule.kt                # Koin-модуль (база, DAO, репозитории, ScreenModel)
└── ui/
    ├── theme/Theme.kt              # ThoughtsTheme(themeMode) — Material 3 тема
    └── features/thoughts/
        ├── ThoughtsScreen.kt       # Voyager Screen — компоновка экрана
        ├── ThoughtsScreenModel.kt  # Voyager ScreenModel — состояние и бизнес-логика
        ├── ThoughtsUiState.kt      # состояния экрана (data class'ы)
        └── components/             # переиспользуемые UI-компоненты
```

Каждый экран построен как пара **Screen + ScreenModel**: `Screen` отвечает только за компоновку и получает свою модель через `koinScreenModel<...>()`, вся логика и состояние — в `ScreenModel`, зависимости приходят через Koin (`di/AppModule.kt`).

## Локализация

- `res/values/strings.xml` — английский (язык по умолчанию)
- `res/values-ru/strings.xml` — русский перевод
- Название приложения («Thoughts») не переводится и одинаково на всех языках
- Язык интерфейса переключается вручную из drawer (работает через `AppCompatDelegate.setApplicationLocales`, поэтому `MainActivity` наследуется от `AppCompatActivity`)

## Сборка и запуск

Требования: JDK 17, Android SDK (compileSdk 36), устройство/эмулятор с API 26+.

```bash
./gradlew :app:assembleDebug
```

Или откройте проект в Android Studio и запустите конфигурацию `app`.
