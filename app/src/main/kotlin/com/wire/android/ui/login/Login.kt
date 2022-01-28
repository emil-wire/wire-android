package com.wire.android.ui.login

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wire.android.R
import com.wire.android.ui.common.AnimatedButtonColors
import com.wire.android.ui.common.CircularProgressIndicator
import com.wire.android.ui.common.textfield.WirePasswordTextField
import com.wire.android.ui.common.textfield.WireTextField
import com.wire.android.ui.theme.body02
import com.wire.android.ui.theme.button02


@Preview
@Composable
fun LoginScreen() {
    LoginContent()
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent() {

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(topBar = { LoginTopBar() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.weight(1f, true),
                verticalArrangement = Arrangement.Center,
            ) {
                EmailInput(modifier = Modifier.fillMaxWidth(), email = email, onEmailChange = { email = it })
                Spacer(modifier = Modifier.height(16.dp))
                PasswordInput(modifier = Modifier.fillMaxWidth(), password = password, onPasswordChange = { password = it })
                Spacer(modifier = Modifier.height(16.dp))
                ForgotPasswordLabel(modifier = Modifier.fillMaxWidth())
            }
            LoginButton(modifier = Modifier.fillMaxWidth(), email = email.text, password = password.text)
        }
    }
}

@Composable
private fun EmailInput(modifier: Modifier, email: TextFieldValue, onEmailChange: (TextFieldValue) -> Unit) {
    WireTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholderText = stringResource(R.string.login_email_placeholder),
        labelText = stringResource(R.string.login_email_label),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        modifier = modifier,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PasswordInput(modifier: Modifier, password: TextFieldValue, onPasswordChange: (TextFieldValue) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    WirePasswordTextField(
        value = password,
        onValueChange = onPasswordChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = modifier,
    )
}

@Composable
private fun ForgotPasswordLabel(modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        val context = LocalContext.current
        Text(
            text = stringResource(R.string.login_forgot_password),
            style = MaterialTheme.typography.body02.copy(
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { Toast.makeText(context, "Forgot password click 💥", Toast.LENGTH_SHORT).show() } //TODO
                )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LoginButton(modifier: Modifier, email: String, password: String) {
    var isLoading by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = modifier) {
        val enabled = validInput(email, password) && !isLoading
        val buttonColors = AnimatedButtonColors(enabled = enabled)
        Button(
            interactionSource = interactionSource,
            shape = RoundedCornerShape(16.dp),
            colors = buttonColors,
            onClick = { isLoading = true }, //TODO
            enabled = validInput(email, password) && !isLoading,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                val text = if (isLoading) stringResource(R.string.label_logging_in) else stringResource(R.string.label_login)
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.button02,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                )
                androidx.compose.animation.AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    visible = isLoading,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    val progressColor = buttonColors.contentColor(enabled).value
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        progressColor = progressColor
                    )
                }
            }
        }
    }
}

private fun validInput(email: String, password: String): Boolean =
    email.isNotEmpty() && password.isNotEmpty()