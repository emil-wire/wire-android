package com.wire.android.feature.conversation.data

import androidx.paging.DataSource
import androidx.paging.PagedList
import com.wire.android.core.exception.Failure
import com.wire.android.core.functional.Either
import com.wire.android.core.functional.suspending
import com.wire.android.feature.conversation.Conversation
import com.wire.android.feature.conversation.data.local.ConversationLocalDataSource
import com.wire.android.feature.conversation.data.remote.ConversationsRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ConversationDataSource(
    private val conversationMapper: ConversationMapper,
    private val conversationRemoteDataSource: ConversationsRemoteDataSource,
    private val conversationLocalDataSource: ConversationLocalDataSource
) : ConversationsRepository {

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun conversationsByBatch(
        pagingDelegate: ConversationsPagingDelegate
    ): Flow<Either<Failure, PagedList<Conversation>>> {
        val failureFlow = ConflatedBroadcastChannel<Failure>()

        val pagingFlow = pagingDelegate.conversationList(conversationsDataFactory()) { lastConvId, size ->
            suspending {
                fetchConversations(lastConvId, size).onFailure {
                    failureFlow.send(it)
                }
            }
        }

        return merge(pagingFlow.map { Either.Right(it) }, failureFlow.asFlow().map { Either.Left(it) })
    }

    private fun conversationsDataFactory(): DataSource.Factory<Int, Conversation> =
        conversationLocalDataSource.conversationsDataFactory().map {
            conversationMapper.fromEntity(it)
        }

    private suspend fun fetchConversations(start: String?, size: Int): Either<Failure, Unit> = suspending {
        conversationRemoteDataSource.conversationsByBatch(start, size).flatMap { response ->
            val conversationEntities = conversationMapper.fromConversationResponseToEntityList(response)
            conversationLocalDataSource.saveConversations(conversationEntities).flatMap {
                val conversationMemberEntities = conversationMapper.fromConversationResponseToConversationMembers(response)
                conversationLocalDataSource.saveMemberIdsForConversations(conversationMemberEntities)
            }
        }
    }

    override suspend fun conversationMemberIds(conversation: Conversation): Either<Failure, List<String>> =
        conversationLocalDataSource.conversationMemberIds(conversation.id)
}