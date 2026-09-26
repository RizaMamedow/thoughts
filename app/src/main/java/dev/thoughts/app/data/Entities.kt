package dev.thoughts.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "thoughts")
data class Thought(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long,
)

@Entity(
    tableName = "thought_tags",
    primaryKeys = ["thoughtId", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = Thought::class,
            parentColumns = ["id"],
            childColumns = ["thoughtId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tag")],
)
data class ThoughtTag(
    val thoughtId: Long,
    val tag: String,
)

data class ThoughtWithTags(
    @Embedded val thought: Thought,
    @Relation(parentColumn = "id", entityColumn = "thoughtId")
    val tagRows: List<ThoughtTag>,
) {
    val tags: List<String> get() = tagRows.map { it.tag }.sorted()
}

data class TagCount(
    val tag: String,
    val count: Int,
)
