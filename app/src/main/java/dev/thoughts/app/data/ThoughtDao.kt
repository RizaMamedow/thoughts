package dev.thoughts.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {
    @Transaction
    @Query("SELECT * FROM thoughts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ThoughtWithTags>>

    @Query("SELECT tag, COUNT(*) AS count FROM thought_tags GROUP BY tag ORDER BY count DESC, tag")
    fun observeTags(): Flow<List<TagCount>>

    @Insert
    suspend fun insert(thought: Thought): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<ThoughtTag>)

    @Query("UPDATE thoughts SET text = :text WHERE id = :id")
    suspend fun updateText(id: Long, text: String)

    @Query("DELETE FROM thought_tags WHERE thoughtId = :thoughtId")
    suspend fun clearTags(thoughtId: Long)

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM thoughts WHERE id IN (:ids)")
    suspend fun deleteAll(ids: List<Long>)

    @Query("DELETE FROM thought_tags WHERE thoughtId IN (:ids) AND tag = :tag")
    suspend fun removeTagFromAll(ids: List<Long>, tag: String)

    @Transaction
    suspend fun insertWithTags(thought: Thought, tags: Collection<String>): Long {
        val id = insert(thought)
        insertTags(tags.map { ThoughtTag(id, it) })
        return id
    }

    @Transaction
    suspend fun update(id: Long, text: String, tags: Collection<String>) {
        updateText(id, text)
        clearTags(id)
        insertTags(tags.map { ThoughtTag(id, it) })
    }

    @Transaction
    suspend fun addTagToAll(ids: List<Long>, tag: String) {
        insertTags(ids.map { ThoughtTag(it, tag) })
    }
}
