package com.wire.android.feature.conversation.list.datasources

import com.wire.android.UnitTest
import com.wire.android.feature.contact.Contact
import com.wire.android.feature.contact.datasources.local.ContactEntity
import com.wire.android.feature.contact.datasources.mapper.ContactMapper
import com.wire.android.feature.conversation.Conversation
import com.wire.android.feature.conversation.data.ConversationMapper
import com.wire.android.feature.conversation.data.local.ConversationEntity
import com.wire.android.feature.conversation.list.datasources.local.ConversationListItemEntity
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import java.io.File

class ConversationListMapperTest : UnitTest() {

    @MockK
    private lateinit var conversationMapper: ConversationMapper

    @MockK
    private lateinit var contactMapper: ContactMapper

    private lateinit var conversationListMapper: ConversationListMapper

    @Before
    fun setUp() {
        conversationListMapper = ConversationListMapper(conversationMapper, contactMapper)
    }

    @Test
    fun `given a ConversationListItemEntity, when fromEntity is called, then delegates to other mappers and combines the results`() {
        val conversationEntity = mockk<ConversationEntity>()
        val conversation = Conversation(id = "id", name = "name")
        every { conversationMapper.fromEntity(conversationEntity) } returns conversation

        val contactEntity1 = mockk<ContactEntity>()
        val contactEntity2 = mockk<ContactEntity>()
        val profilePicture1 = mockk<File>()
        val profilePicture2 = mockk<File>()
        val contactEntities = listOf(contactEntity1, contactEntity2)
        val profilePictures = listOf(profilePicture1, profilePicture2)

        val contact1 = mockk<Contact>()
        val contact2 = mockk<Contact>()
        every { contactMapper.fromContactEntity(contactEntity1, any()) } returns contact1
        every { contactMapper.fromContactEntity(contactEntity2, any()) } returns contact2

        val listItemEntity = ConversationListItemEntity(conversationEntity, contactEntities)

        val result = conversationListMapper.fromEntity(listItemEntity, profilePictures)

        result.id shouldBeEqualTo conversation.id
        result.name shouldBeEqualTo conversation.name
        result.members shouldBeEqualTo listOf(contact1, contact2)
        verify(exactly = 1) { conversationMapper.fromEntity(conversationEntity) }
        verify(exactly = 1) { contactMapper.fromContactEntity(contactEntity1, profilePicture1) }
        verify(exactly = 1) { contactMapper.fromContactEntity(contactEntity2, profilePicture2) }
    }
}