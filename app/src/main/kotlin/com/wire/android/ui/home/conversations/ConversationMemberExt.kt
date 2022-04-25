package com.wire.android.ui.home.conversations

import com.wire.android.model.UserAvatarAsset
import com.wire.kalium.logic.data.conversation.MemberDetails
import com.wire.kalium.logic.data.user.UserId

fun List<MemberDetails>.findSender(senderId: UserId): MemberDetails? = firstOrNull { member ->
    when (member) {
        is MemberDetails.Other -> member.otherUser.id == senderId
        is MemberDetails.Self -> member.selfUser.id == senderId
    }
}

val MemberDetails.name
    get() = when (this) {
        is MemberDetails.Other -> this.otherUser.name
        is MemberDetails.Self -> this.selfUser.name
    }

val MemberDetails.previewAsset: UserAvatarAsset?
    get() = when(this) {
        is MemberDetails.Other -> this.otherUser.previewPicture
        is MemberDetails.Self -> this.selfUser.previewPicture
    }?.let { UserAvatarAsset(it) }