package com.wire.android.mapper

import android.content.res.Resources
import com.wire.android.config.CoroutineTestExtension
import com.wire.android.framework.TestMessage
import com.wire.android.framework.TestUser
import com.wire.android.ui.home.conversations.model.MessageContent.AssetMessage
import com.wire.android.ui.home.conversations.model.MessageContent.ImageMessage
import com.wire.android.ui.home.conversations.model.MessageContent.SystemMessage
import com.wire.android.util.ui.UIText
import com.wire.kalium.logic.data.conversation.Member
import com.wire.kalium.logic.data.conversation.MemberDetails
import com.wire.kalium.logic.data.id.QualifiedID
import com.wire.kalium.logic.data.message.AssetContent
import com.wire.kalium.logic.data.message.AssetContent.AssetMetadata
import com.wire.kalium.logic.data.message.Message
import com.wire.kalium.logic.data.message.MessageContent
import com.wire.kalium.logic.data.user.UserId
import com.wire.kalium.logic.feature.asset.GetMessageAssetUseCase
import com.wire.kalium.logic.feature.asset.MessageAssetResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class MessageContentMapperTest {

    @Test
    fun givenMemberDetails_whenMappingToSystemMessageMemberName_thenCorrectValuesShouldBeReturned() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val selfMemberDetails = TestUser.MEMBER_SELF
        val deletedMemberDetails = TestUser.MEMBER_OTHER.copy(TestUser.OTHER_USER.copy(name = null))
        val otherMemberDetails = TestUser.MEMBER_OTHER
        // When
        val selfName = mapper.toSystemMessageMemberName(selfMemberDetails, MessageContentMapper.SelfNameType.NameOrDeleted)
        val selfResLower = mapper.toSystemMessageMemberName(selfMemberDetails, MessageContentMapper.SelfNameType.ResourceLowercase)
        val selfResTitle = mapper.toSystemMessageMemberName(selfMemberDetails, MessageContentMapper.SelfNameType.ResourceTitleCase)
        val deleted = mapper.toSystemMessageMemberName(deletedMemberDetails, MessageContentMapper.SelfNameType.NameOrDeleted)
        val otherName = mapper.toSystemMessageMemberName(otherMemberDetails, MessageContentMapper.SelfNameType.NameOrDeleted)
        // Then
        assert(selfName is UIText.DynamicString && selfName.value == selfMemberDetails.selfUser.name)
        assert(selfResLower is UIText.StringResource && selfResLower.resId == arrangement.messageResourceProvider.memberNameYouLowercase)
        assert(selfResTitle is UIText.StringResource && selfResTitle.resId == arrangement.messageResourceProvider.memberNameYouTitlecase)
        assert(deleted is UIText.StringResource && deleted.resId == arrangement.messageResourceProvider.memberNameDeleted)
        assert(otherName is UIText.DynamicString && otherName.value == otherMemberDetails.otherUser.name)
    }

    @Test
    fun givenTextOrNullContent_whenMappingToTextMessageContent_thenCorrectValuesShouldBeReturned() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val textContent = MessageContent.Text("text-message")
        val nonTextContent = MessageContent.Unknown("type-name")
        // When
        val resultText = mapper.toText(textContent)
        val resultNonText = mapper.toText(nonTextContent)
        with(resultText) {
            assert(
                messageBody.message is UIText.DynamicString &&
                        (messageBody.message as UIText.DynamicString).value == textContent.value
            )
        }
        with(resultNonText) {
            assert(
                messageBody.message is UIText.StringResource &&
                        (messageBody.message as UIText.StringResource).resId == arrangement.messageResourceProvider.sentAMessageWithContent
            )
        }
    }

    @Test
    fun givenServerContent_whenMappingToUIMessageContent_thenCorrectValuesShouldBeReturned() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val userId1 = UserId("user-id1", "user-domain")
        val userId2 = UserId("user-id2", "user-domain")
        val userId3 = UserId("user-id3", "user-domain")
        val contentLeft = MessageContent.MemberChange.Removed(listOf(userId1))
        val contentRemoved = MessageContent.MemberChange.Removed(listOf(userId2))
        val contentAdded = MessageContent.MemberChange.Added(listOf(userId2, userId3))
        val contentAddedSelf = MessageContent.MemberChange.Added(listOf(userId1))
        val member1 = TestUser.MEMBER_OTHER.copy(TestUser.OTHER_USER.copy(id = userId1))
        val member2 = TestUser.MEMBER_OTHER.copy(TestUser.OTHER_USER.copy(id = userId2))
        val member3 = TestUser.MEMBER_OTHER.copy(TestUser.OTHER_USER.copy(id = userId3))
        val missedCallMessage = TestMessage.MISSED_CALL_MESSAGE
        val selfCaller = MemberDetails.Self(TestUser.SELF_USER.copy(id = missedCallMessage.senderUserId))
        val otherCaller = member1.copy(otherUser = member1.otherUser.copy(id = missedCallMessage.senderUserId))
        // When
        val resultContentLeft = mapper.mapMemberChangeMessage(contentLeft, userId1, listOf(member1))
        val resultContentRemoved = mapper.mapMemberChangeMessage(contentRemoved, userId1, listOf(member1, member2))
        val resultContentAdded = mapper.mapMemberChangeMessage(contentAdded, userId1, listOf(member1, member2, member3))
        val resultContentAddedSelf = mapper.mapMemberChangeMessage(contentAddedSelf, userId1, listOf(member1))
        val resultMyMissedCall = mapper.fromMessage(missedCallMessage, listOf(selfCaller))
        val resultOtherMissedCall = mapper.fromMessage(missedCallMessage, listOf(otherCaller))
        // Then
        assert(
            resultContentLeft is SystemMessage.MemberLeft &&
                    resultContentLeft.author.asString(arrangement.resources) == member1.otherUser.name
        )
        assert(
            resultContentRemoved is SystemMessage.MemberRemoved &&
                    resultContentRemoved.author.asString(arrangement.resources) == member1.otherUser.name &&
                    resultContentRemoved.memberNames.size == 1 &&
                    resultContentRemoved.memberNames[0].asString(arrangement.resources) == member2.otherUser.name

        )
        assert(
            resultContentAdded is SystemMessage.MemberAdded &&
                    resultContentAdded.author.asString(arrangement.resources) == member1.otherUser.name &&
                    resultContentAdded.memberNames.size == 2 &&
                    resultContentAdded.memberNames[0].asString(arrangement.resources) == member2.otherUser.name &&
                    resultContentAdded.memberNames[1].asString(arrangement.resources) == member3.otherUser.name
        )
        assert(resultContentAddedSelf == null)
        assert(
            resultOtherMissedCall is SystemMessage.MissedCall &&
                    resultOtherMissedCall.author.asString(arrangement.resources) == TestUser.OTHER_USER.name
        )
        assert(
            resultMyMissedCall is SystemMessage.MissedCall &&
                    (resultMyMissedCall.author as UIText.StringResource).resId == arrangement.messageResourceProvider.memberNameYouTitlecase
        )
    }

    @Test
    fun givenAssetContent_whenMappingToUIMessageContent_thenCorrectValuesShouldBeReturned() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val unknownImageFormatMessage = AssetContent(
            0L,
            "name1",
            "image/xrz",
            AssetMetadata.Image(100, 100),
            TestMessage.DUMMY_ASSET_REMOTE_DATA.copy(assetId = "image-id"),
            Message.DownloadStatus.NOT_DOWNLOADED
        )
        val correctJPGImage = AssetContent(
            0L,
            "name2",
            "image/jpg",
            AssetMetadata.Image(100, 100),
            TestMessage.DUMMY_ASSET_REMOTE_DATA.copy(assetId = "image-id2"),
            Message.DownloadStatus.NOT_DOWNLOADED
        )
        // When - Then
        val resultContentOther = mapper.toAsset(QualifiedID("id", "domain"), "message-id", unknownImageFormatMessage)
        coVerify(exactly = 0) { arrangement.getMessageAssetUseCase.invoke(any(), any()) }
        assert(resultContentOther is AssetMessage && resultContentOther.assetId.value == unknownImageFormatMessage.remoteData.assetId)
        // When - Then
        val resultContentImage = mapper.toAsset(QualifiedID("id", "domain"), "message-id", correctJPGImage)
        coVerify(exactly = 1) { arrangement.getMessageAssetUseCase.invoke(any(), any()) }
        assert(resultContentImage is ImageMessage && resultContentImage.assetId.value == correctJPGImage.remoteData.assetId)
    }

    @Test
    fun givenSVGImageAssetContent_whenMappingToUIMessageContent_thenIsMappedToAssetMessage() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val contentImage = AssetContent(
            0L,
            "name",
            "image/svg",
            AssetMetadata.Image(100, 100),
            TestMessage.DUMMY_ASSET_REMOTE_DATA.copy(assetId = "image-id"),
            Message.DownloadStatus.NOT_DOWNLOADED
        )

        // When
        val resultContentImage = mapper.toAsset(QualifiedID("id", "domain"), "message-id", contentImage)

        // Then
        coVerify(inverse = true) { arrangement.getMessageAssetUseCase.invoke(any(), any()) }
        assert(resultContentImage is AssetMessage)
    }

    @Test
    fun givenPNGImageAssetContentWith0Width_whenMappingToUIMessageContent_thenIsMappedToAssetMessage() = runTest {
        // Given
        val (arrangement, mapper) = Arrangement().arrange()
        val contentImage1 = AssetContent(
            0L,
            "name1",
            "image/png",
            AssetMetadata.Image(0, 0),
            TestMessage.DUMMY_ASSET_REMOTE_DATA.copy(assetId = "image-id"),
            Message.DownloadStatus.NOT_DOWNLOADED
        )
        val contentImage2 = AssetContent(
            0L,
            "name2",
            "image/png",
            AssetMetadata.Image(100, 100),
            TestMessage.DUMMY_ASSET_REMOTE_DATA.copy(assetId = "image-id2"),
            Message.DownloadStatus.NOT_DOWNLOADED
        )

        // When
        val resultContentImage1 = mapper.toAsset(QualifiedID("id", "domain"), "message-id", contentImage1)
        val resultContentImage2 = mapper.toAsset(QualifiedID("id", "domain"), "message-id", contentImage2)

        // Then
        assert(resultContentImage1 is AssetMessage)
        assert(resultContentImage2 is ImageMessage)

        // Only the image with valid metadata is downloaded
        coVerify(exactly = 1) { arrangement.getMessageAssetUseCase.invoke(any(), any()) }
    }

    @Test
    fun givenMessage_whenMappingToUIMessageContent_thenCorrectValuesShouldBeReturned() = runTest {
        // Given
        val (_, mapper) = Arrangement().arrange()
        val visibleMessage = TestMessage.TEXT_MESSAGE.copy(visibility = Message.Visibility.VISIBLE)
        val deletedMessage = TestMessage.TEXT_MESSAGE.copy(
            visibility = Message.Visibility.DELETED,
            content = MessageContent.DeleteMessage("")
        )
        val hiddenMessage = TestMessage.TEXT_MESSAGE.copy(
            visibility = Message.Visibility.HIDDEN,
            content = MessageContent.DeleteMessage("")
        )
        // When
        val resultContentVisible = mapper.fromMessage(visibleMessage, listOf())
        val resultContentDeleted = mapper.fromMessage(deletedMessage, listOf())
        val resultContentHidden = mapper.fromMessage(hiddenMessage, listOf())
        // Then
        assert(resultContentVisible != null)
        assert(resultContentDeleted == null)
        assert(resultContentHidden == null)
    }

    private class Arrangement {

        @MockK
        lateinit var getMessageAssetUseCase: GetMessageAssetUseCase

        @MockK
        lateinit var messageResourceProvider: MessageResourceProvider

        @MockK
        lateinit var resources: Resources

        private val messageContentMapper by lazy {
            MessageContentMapper(getMessageAssetUseCase, messageResourceProvider)
        }

        init {
            MockKAnnotations.init(this, relaxUnitFun = true)
            coEvery { getMessageAssetUseCase.invoke(any(), any()) } returns MessageAssetResult.Success("mocked-image".toByteArray())
            every { messageResourceProvider.memberNameDeleted } returns 10584735
            every { messageResourceProvider.memberNameYouLowercase } returns 24153498
            every { messageResourceProvider.memberNameYouTitlecase } returns 38946214
            every { messageResourceProvider.sentAMessageWithContent } returns 45407124
        }

        fun arrange() = this to messageContentMapper
    }
}
