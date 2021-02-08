package com.wire.android.feature.conversation.data.local

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversationList: List<ConversationEntity>)

    @Query("DELETE FROM conversation WHERE id = :id")
    fun deleteConversationById(id: String)

    @Query("SELECT * FROM conversation")
    suspend fun conversations(): List<ConversationEntity>

    @Query("SELECT * FROM conversation")
    fun conversationsInBatch(): DataSource.Factory<Int, ConversationEntity>
}