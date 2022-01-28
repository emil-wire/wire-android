package com.wire.android.feature.conversation.content

import java.time.OffsetDateTime

data class EncryptedMessageEnvelope(
    val id: String,
    val conversationId: String,
    // TODO use Qualified ID
    val senderUserId: String,
    val clientId: String?,
    val content: String,
    val time: OffsetDateTime
)