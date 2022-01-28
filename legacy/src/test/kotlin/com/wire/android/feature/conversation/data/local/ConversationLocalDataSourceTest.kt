package com.wire.android.feature.conversation.data.local

import android.database.sqlite.SQLiteException
import com.wire.android.UnitTest
import com.wire.android.feature.contact.datasources.local.ContactWithClients
import com.wire.android.feature.conversation.ConversationID
import com.wire.android.feature.conversation.members.datasources.local.ConversationMemberEntity
import com.wire.android.feature.conversation.members.datasources.local.ConversationMembersDao
import com.wire.android.framework.functional.shouldFail
import com.wire.android.framework.functional.shouldSucceed
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import java.sql.SQLException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

class ConversationLocalDataSourceTest : UnitTest() {

    @MockK
    private lateinit var conversationDao: ConversationDao

    @MockK
    private lateinit var conversationMembersDao: ConversationMembersDao

    @MockK
    private lateinit var conversationCache: ConversationCache

    private lateinit var conversationLocalDataSource: ConversationLocalDataSource

    @Before
    fun setUp() {
        conversationLocalDataSource = ConversationLocalDataSource(conversationDao, conversationMembersDao, conversationCache)
    }

    @Test
    fun `given saveConversations is called, when dao insertion is successful, then returns success`() {
        val conversationEntities = mockk<List<ConversationEntity>>()
        coEvery { conversationDao.insertAll(conversationEntities) } returns Unit

        val result = runBlocking { conversationLocalDataSource.saveConversations(conversationEntities) }

        result shouldSucceed { it shouldBe Unit }
        coVerify { conversationDao.insertAll(conversationEntities) }
    }

    @Test
    fun `given saveConversations is called, when dao insertion fails, then propagates failure`() {
        val conversationEntities = mockk<List<ConversationEntity>>()
        coEvery { conversationDao.insertAll(conversationEntities) } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.saveConversations(conversationEntities) }

        result shouldFail { }
        coVerify { conversationDao.insertAll(conversationEntities) }
    }

    @Test
    fun `given updateConversations is called, when dao update is successful, then returns success`() {
        val conversationEntities = mockk<List<ConversationEntity>>()
        coEvery { conversationDao.updateConversations(conversationEntities) } returns Unit

        val result = runBlocking { conversationLocalDataSource.updateConversations(conversationEntities) }

        result shouldSucceed { it shouldBe Unit }
        coVerify { conversationDao.updateConversations(conversationEntities) }
    }

    @Test
    fun `given updateConversations is called, when dao update fails, then propagates failure`() {
        val conversationEntities = mockk<List<ConversationEntity>>()
        coEvery { conversationDao.updateConversations(conversationEntities) } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.updateConversations(conversationEntities) }

        result shouldFail { }
        coVerify { conversationDao.updateConversations(conversationEntities) }
    }

    @Test
    fun `given saveMemberIdsForConversations is called, when dao operation is successful, then returns success`() {
        val conversationMemberEntities = mockk<List<ConversationMemberEntity>>()
        coEvery { conversationMembersDao.insertAll(conversationMemberEntities) } returns Unit

        val result = runBlocking { conversationLocalDataSource.saveMemberIdsForConversations(conversationMemberEntities) }

        result shouldSucceed { it shouldBe Unit }
        coVerify { conversationMembersDao.insertAll(conversationMemberEntities) }
    }

    @Test
    fun `given saveMemberIdsForConversations is called, when dao operation fails, then propagates failure`() {
        val conversationMemberEntities = mockk<List<ConversationMemberEntity>>()
        coEvery { conversationMembersDao.insertAll(conversationMemberEntities) } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.saveMemberIdsForConversations(conversationMemberEntities) }

        result shouldFail { }
        coVerify { conversationMembersDao.insertAll(conversationMemberEntities) }
    }

    @Test
    fun `given conversationMemberIds is called, when dao operation returns member ids, then propagates member ids`() {
        val conversationId = "conv-id-1"
        val memberIds = listOf("member-id-1", "member-id-2")
        coEvery { conversationMembersDao.conversationMembers(conversationId) } returns memberIds

        val result = runBlocking { conversationLocalDataSource.conversationMemberIds(conversationId) }

        result shouldSucceed { it shouldBe memberIds }
        coVerify { conversationMembersDao.conversationMembers(conversationId) }
    }

    @Test
    fun `given conversationMemberIds is called, when dao operation fails, then propagates failure`() {
        val conversationId = "conv-id-1"
        coEvery { conversationMembersDao.conversationMembers(conversationId) } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.conversationMemberIds(conversationId) }

        result shouldFail { }
        coVerify { conversationMembersDao.conversationMembers(conversationId) }
    }

    @Test
    fun `given allConversationMemberIds is called, when dao operation returns member ids, then propagates member ids`() {
        val memberIds = mockk<List<String>>()
        coEvery { conversationMembersDao.allConversationMemberIds() } returns memberIds

        val result = runBlocking { conversationLocalDataSource.allConversationMemberIds() }

        result shouldSucceed { it shouldBe memberIds }
        coVerify { conversationMembersDao.allConversationMemberIds() }
    }

    @Test
    fun `given allConversationMemberIds is called, when dao operation fails, then propagates failure`() {
        coEvery { conversationMembersDao.allConversationMemberIds() } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.allConversationMemberIds() }

        result shouldFail { }
        coVerify { conversationMembersDao.allConversationMemberIds() }
    }

    @Test
    fun `given numberOfConversations is called, when dao operation returns the count, then propagates the count`() {
        val count = 218
        coEvery { conversationDao.count() } returns count

        val result = runBlocking { conversationLocalDataSource.numberOfConversations() }

        result shouldSucceed { it shouldBeEqualTo count }
        coVerify { conversationDao.count() }
    }

    @Test
    fun `given numberOfConversations is called, when dao operation fails, then propagates failure`() {
        coEvery { conversationDao.count() } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.numberOfConversations() }

        result shouldFail { }
        coVerify { conversationDao.count() }
    }

    @Test
    fun `given conversationCache returns the conversationId, when getting current conversationId, then propagates the conversationId`() {
        every { conversationCache.currentOpenedConversationId() } returns TEST_CONVERSATION_ID

        val result = runBlocking { conversationLocalDataSource.currentOpenedConversationId() }

        result shouldSucceed { it shouldBeEqualTo TEST_CONVERSATION_ID }
        verify { conversationCache.currentOpenedConversationId() }
    }

    @Test
    fun `given conversationCache update is successful, when updating current conversationId, then returns success`() {
        every { conversationCache.updateConversationId(TEST_CONVERSATION_ID) } returns Unit

        val result = runBlocking { conversationLocalDataSource.updateCurrentConversationId(TEST_CONVERSATION_ID) }

        result shouldSucceed { it shouldBe Unit }
        verify { conversationCache.updateConversationId(TEST_CONVERSATION_ID) }
    }

    @Test
    fun `given conversation dao fails, when getting conversation name by id, then returns failure`() {
        coEvery { conversationDao.conversationNameById(any()) } throws SQLException()

        val result = runBlocking { conversationLocalDataSource.conversationNameById(CONVERSATION_ID_VALUE) }

        result shouldFail {}
    }

    @Test
    fun `given conversation dao succeed, when getting conversation name by id, then returns conversation name`() {
        coEvery { conversationDao.conversationNameById(any()) } returns CONVERSATION_NAME

        val result = runBlocking { conversationLocalDataSource.conversationNameById(CONVERSATION_ID_VALUE) }

        result shouldSucceed {
            it shouldBeEqualTo CONVERSATION_NAME
        }
    }

    @Test
    fun `given a conversation id, when getting detailed members of a conversation, then pass the correct ID to the DAO`() {
        coEvery { conversationMembersDao.detailedConversationMembers(any()) } returns mockk()

        runBlockingTest { conversationLocalDataSource.detailedMembersOfConversation(CONVERSATION_ID_VALUE) }

        coVerify(exactly = 1) { conversationMembersDao.detailedConversationMembers(CONVERSATION_ID_VALUE) }
    }

    @Test
    fun `given conversationMembersDao succeeds, when getting detailed members of a conversation, then forward the result`() {
        val result = mockk<List<ContactWithClients>>()
        coEvery { conversationMembersDao.detailedConversationMembers(any()) } returns result

        runBlockingTest {
            conversationLocalDataSource.detailedMembersOfConversation(CONVERSATION_ID_VALUE)
                .shouldSucceed { it shouldBeEqualTo result }
        }
    }

    @Test
    fun `given conversationMembersDao fails, when getting detailed members of a conversation, then forward the failure`() {
        coEvery { conversationMembersDao.detailedConversationMembers(any()) } throws SQLiteException()

        runBlockingTest {
            conversationLocalDataSource.detailedMembersOfConversation(CONVERSATION_ID_VALUE)
                .shouldFail { }
        }
    }

    companion object {
        private const val CONVERSATION_ID_VALUE = "2133215644868"
        private const val CONVERSATION_DOMAIN = "conv-domain"
        private val TEST_CONVERSATION_ID = ConversationID(CONVERSATION_ID_VALUE, CONVERSATION_DOMAIN)
        private const val CONVERSATION_NAME = "Android Team"
    }

}