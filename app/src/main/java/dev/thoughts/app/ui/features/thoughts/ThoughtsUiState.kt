package dev.thoughts.app.ui.features.thoughts

import dev.thoughts.app.data.TagCount
import dev.thoughts.app.data.ThoughtWithTags
import java.time.LocalDate

data class DayGroup(
    val date: LocalDate,
    val thoughts: List<ThoughtWithTags>,
)

data class ThoughtsUiState(
    val days: List<DayGroup> = emptyList(),
    val tags: List<TagCount> = emptyList(),
    val selectedTag: String? = null,
    val query: String = "",
    val isEmpty: Boolean = true,
    val selectedIds: Set<Long> = emptySet(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
