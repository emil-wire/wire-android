package com.wire.android.ui.calling.incoming

import androidx.lifecycle.SavedStateHandle
import com.wire.android.config.CoroutineTestExtension
import com.wire.android.navigation.NavigationManager
import com.wire.kalium.logic.feature.call.AnswerCallUseCase
import com.wire.kalium.logic.feature.call.usecase.RejectCallUseCase
import com.wire.kalium.logic.feature.conversation.ObserveConversationDetailsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class IncomingCallViewModelTest {

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK
    lateinit var navigationManager: NavigationManager

    @MockK
    lateinit var conversationDetails: ObserveConversationDetailsUseCase

    @MockK
    lateinit var rejectCall: RejectCallUseCase

    @MockK
    lateinit var acceptCall: AnswerCallUseCase

    private lateinit var viewModel: IncomingCallViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { savedStateHandle.get<String>(any()) } returns ("conversationId")

        // Default empty values
        coEvery { navigationManager.navigateBack() } returns Unit
        coEvery { navigationManager.navigate(any()) } returns Unit
        coEvery { conversationDetails(any()) } returns flowOf()
        coEvery { rejectCall(any()) } returns Unit
        coEvery { acceptCall(any()) } returns Unit

        viewModel = IncomingCallViewModel(
            savedStateHandle = savedStateHandle,
            navigationManager = navigationManager,
            conversationDetails = conversationDetails,
            rejectCall = rejectCall,
            acceptCall = acceptCall
        )
    }

    @Test
    fun `given an incoming call, when the user decline the call, then the reject call use case is called`() {
        viewModel.declineCall()

        coVerify(exactly = 1) { rejectCall(conversationId = any()) }
        coVerify(exactly = 1) { navigationManager.navigateBack() }
    }

    @Test
    fun `given an incoming call, when the user accepts the call, then the accept call use case is called`() {
        viewModel.acceptCall()

        coVerify(exactly = 1) { acceptCall(conversationId = any()) }
        coVerify(exactly = 1) { navigationManager.navigate(command = any()) }
    }
}