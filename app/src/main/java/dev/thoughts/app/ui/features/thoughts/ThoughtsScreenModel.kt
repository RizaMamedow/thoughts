package dev.thoughts.app.ui.features.thoughts

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.thoughts.app.data.TagParser
import dev.thoughts.app.data.Thought
import dev.thoughts.app.data.ThoughtDao
import dev.thoughts.app.data.ThoughtWithTags
import dev.thoughts.app.data.settings.SettingsRepository
import dev.thoughts.app.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class ThoughtsScreenModel(
    private val dao: ThoughtDao,
    private val settings: SettingsRepository,
) : ScreenModel {

    private val selectedTag = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<ThoughtsUiState> = combine(
        dao.observeAll(),
        dao.observeTags(),
        selectedTag,
        query,
        selectedIds,
    ) { all, tags, tag, q, selected ->
        val zone = ZoneId.systemDefault()
        val filtered = all.filter { item ->
            (tag == null || tag in item.tags) &&
                (q.isBlank() || item.thought.text.contains(q, ignoreCase = true) ||
                    item.tags.any { it.contains(q, ignoreCase = true) })
        }
        ThoughtsUiState(
            days = filtered
                .groupBy { Instant.ofEpochMilli(it.thought.createdAt).atZone(zone).toLocalDate() }
                .map { (date, items) -> DayGroup(date, items) },
            tags = tags,
            selectedTag = tag,
            query = q,
            isEmpty = all.isEmpty(),
            selectedIds = selected,
        )
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(5_000), ThoughtsUiState())

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        screenModelScope.launch { settings.setThemeMode(mode) }
    }

    fun add(raw: String) {
        if (raw.isBlank()) return
        val parsed = TagParser.parse(raw)
        val tags = parsed.tags + listOfNotNull(selectedTag.value)
        screenModelScope.launch {
            dao.insertWithTags(Thought(text = parsed.text, createdAt = System.currentTimeMillis()), tags)
        }
    }

    fun edit(id: Long, raw: String) {
        if (raw.isBlank()) return
        val parsed = TagParser.parse(raw)
        screenModelScope.launch { dao.update(id, parsed.text, parsed.tags) }
    }

    fun delete(item: ThoughtWithTags) {
        screenModelScope.launch { dao.delete(item.thought.id) }
    }

    fun restore(item: ThoughtWithTags) {
        screenModelScope.launch { dao.insertWithTags(item.thought, item.tags) }
    }

    fun selectTag(tag: String?) {
        selectedTag.value = if (selectedTag.value == tag) null else tag
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun toggleSelection(id: Long) {
        selectedIds.update { current -> if (id in current) current - id else current + id }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected(ids: Collection<Long>) {
        screenModelScope.launch { dao.deleteAll(ids.toList()) }
    }

    fun restoreAll(items: Collection<ThoughtWithTags>) {
        screenModelScope.launch { items.forEach { dao.insertWithTags(it.thought, it.tags) } }
    }

    fun addTagToSelected(ids: Collection<Long>, tag: String) {
        val cleanTag = tag.trim().lowercase()
        if (cleanTag.isEmpty()) return
        screenModelScope.launch { dao.addTagToAll(ids.toList(), cleanTag) }
    }

    fun removeTagFromSelected(ids: Collection<Long>, tag: String) {
        val cleanTag = tag.trim().lowercase()
        if (cleanTag.isEmpty()) return
        screenModelScope.launch { dao.removeTagFromAll(ids.toList(), cleanTag) }
    }
}
