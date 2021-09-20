package com.wire.android.feature.messaging.datasource.remote

import com.wire.android.core.network.ApiService
import com.wire.android.core.network.NetworkHandler
import com.wire.android.feature.messaging.ChatMessageEnvelope
import com.wire.android.feature.messaging.datasource.remote.api.MessageApi
import com.wire.android.feature.messaging.datasource.remote.mapper.OtrNewMessageMapper

class MessageRemoteDataSource(
    override val networkHandler: NetworkHandler,
    private val otrNewMessageMapper: OtrNewMessageMapper,
    private val messageApi: MessageApi
) : ApiService() {

    suspend fun sendMessage(conversationId: String, chatMessageEnvelope: ChatMessageEnvelope) = request {
        val otrMessage = otrNewMessageMapper.fromMessageEnvelope(chatMessageEnvelope)
        messageApi.sendMessage(conversationId, otrMessage)
    }
}