package com.wire.android.feature.auth.registration.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wire.android.R
import com.wire.android.core.async.DispatcherProvider
import com.wire.android.core.exception.Failure
import com.wire.android.core.exception.NetworkConnection
import com.wire.android.core.extension.failure
import com.wire.android.core.extension.success
import com.wire.android.core.functional.Either
import com.wire.android.core.ui.SingleLiveEvent
import com.wire.android.core.ui.dialog.ErrorMessage
import com.wire.android.core.ui.dialog.GeneralErrorMessage
import com.wire.android.core.ui.dialog.NetworkErrorMessage
import com.wire.android.core.usecase.DefaultUseCaseExecutor
import com.wire.android.core.usecase.UseCaseExecutor
import com.wire.android.feature.auth.registration.personal.usecase.ActivateEmailParams
import com.wire.android.feature.auth.registration.personal.usecase.ActivateEmailUseCase
import com.wire.android.feature.auth.registration.personal.usecase.InvalidEmailCode

class CreateAccountEmailVerificationCodeViewModel(
    override val dispatcherProvider: DispatcherProvider,
    private val activateEmailUseCase: ActivateEmailUseCase
) : ViewModel(), UseCaseExecutor by DefaultUseCaseExecutor(dispatcherProvider) {

    private val _activateEmailLiveData = SingleLiveEvent<Either<ErrorMessage, String>>()
    val activateEmailLiveData: LiveData<Either<ErrorMessage, String>> = _activateEmailLiveData

    fun activateEmail(email: String, code: String) = activateEmailUseCase(viewModelScope, ActivateEmailParams(email, code)) {
        it.fold(::activateEmailFailure) { activateEmailSuccess(code) }
    }

    private fun activateEmailSuccess(code: String) = _activateEmailLiveData.success(code)

    private fun activateEmailFailure(failure: Failure) {
        val errorMessage = when (failure) {
            is NetworkConnection -> NetworkErrorMessage
            is InvalidEmailCode -> ErrorMessage(R.string.create_personal_account_code_invalid_code_error)
            else -> GeneralErrorMessage
        }
        _activateEmailLiveData.failure(errorMessage)
    }
}