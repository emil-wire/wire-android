package com.wire.android.feature.auth.registration.personal.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wire.android.core.async.DispatcherProvider
import com.wire.android.core.ui.SingleLiveEvent
import com.wire.android.core.usecase.DefaultUseCaseExecutor
import com.wire.android.core.usecase.UseCaseExecutor
import com.wire.android.shared.user.name.ValidateNameParams
import com.wire.android.shared.user.name.ValidateNameUseCase

class CreatePersonalAccountNameViewModel(
    override val dispatcherProvider: DispatcherProvider,
    private val validateNameUseCase: ValidateNameUseCase
) : ViewModel(), UseCaseExecutor by DefaultUseCaseExecutor(dispatcherProvider) {

    private val _confirmationButtonEnabled = SingleLiveEvent<Boolean>()
    val confirmationButtonEnabled: LiveData<Boolean> = _confirmationButtonEnabled

    fun validateName(name: String) = validateNameUseCase(viewModelScope, ValidateNameParams(name)) {
        _confirmationButtonEnabled.value = it.isRight
    }
}