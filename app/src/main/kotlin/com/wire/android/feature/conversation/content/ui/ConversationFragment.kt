package com.wire.android.feature.conversation.content.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wire.android.R
import com.wire.android.core.extension.EMPTY
import kotlinx.android.synthetic.main.fragment_conversation.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val viewModel by sharedViewModel<ConversationViewModel>()
    private val conversationAdapter by inject<ConversationAdapter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler(view)
        observeConversationId()
        observeMessages()
    }

    private fun observeConversationId() {
        viewModel.conversationIdLiveData.observe(viewLifecycleOwner) {
            viewModel.fetchMessages(it)
            viewModel.updateCurrentConversationId(it)
        }
    }

    private fun setUpRecycler(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.conversationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = conversationAdapter
    }

    private fun observeMessages() {
        viewModel.conversationMessagesLiveData.observe(viewLifecycleOwner) {
            conversationRecyclerView.scrollToPosition(it.size - 1)
            conversationAdapter.submitList(it)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateCurrentConversationId(String.EMPTY)
    }
}