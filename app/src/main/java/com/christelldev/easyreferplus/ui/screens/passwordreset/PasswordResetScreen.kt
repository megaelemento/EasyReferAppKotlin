package com.christelldev.easyreferplus.ui.screens.passwordreset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.PasswordResetStep
import com.christelldev.easyreferplus.ui.viewmodel.PasswordResetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetScreen(
    viewModel: PasswordResetViewModel,
    onBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar personalizado
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reset_password),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignConstants.CARD_MARGIN_HORIZONTAL)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.currentStep) {
                    PasswordResetStep.PHONE -> PhoneStep(
                        phone = uiState.phone,
                        phoneError = uiState.phoneError,
                        isLoading = uiState.isLoading,
                        onPhoneChange = viewModel::updatePhone,
                        onSubmit = viewModel::requestPasswordReset
                    )
                    PasswordResetStep.CODE -> CodeStep(
                        code1 = uiState.code1,
                        code2 = uiState.code2,
                        code3 = uiState.code3,
                        code4 = uiState.code4,
                        codeError = uiState.codeError,
                        isLoading = uiState.isLoading,
                        isResendEnabled = uiState.isResendEnabled,
                        waitSeconds = uiState.waitSeconds,
                        onCodeChange = viewModel::updateCode,
                        onResend = viewModel::resendCode,
                        onVerifyCode = viewModel::verifyResetCode
                    )
                    PasswordResetStep.NEW_PASSWORD -> NewPasswordStep(
                        newPassword = uiState.newPassword,
                        newPasswordError = uiState.newPasswordError,
                        confirmPassword = uiState.confirmPassword,
                        confirmPasswordError = uiState.confirmPasswordError,
                        passwordVisible = uiState.passwordVisible,
                        confirmPasswordVisible = uiState.confirmPasswordVisible,
                        isLoading = uiState.isLoading,
                        onNewPasswordChange = viewModel::updateNewPassword,
                        onConfirmPasswordChange = viewModel::updateConfirmPassword,
                        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                        onToggleConfirmPasswordVisibility = viewModel::toggleConfirmPasswordVisibility,
                        onSubmit = viewModel::resetPassword
                    )
                    PasswordResetStep.COMPLETE -> CompleteStep(
                        onBackToLogin = onBackToLogin
                    )
                }

                // Mensajes de error y éxito
                Spacer(modifier = Modifier.height(16.dp))

                uiState.errorMessage?.let { error ->
                    ErrorCard(message = error)
                }

                uiState.successMessage?.let { success ->
                    SuccessCard(message = success)
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

@Composable
fun PhoneStep(
    phone: String,
    phoneError: String?,
    isLoading: Boolean,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.reset_password_subtitle),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = DesignConstants.TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            PhoneTextField(
                value = phone,
                onValueChange = onPhoneChange,
                isError = phoneError != null,
                errorMessage = phoneError,
                enabled = !isLoading,
                onDone = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && phone.length == 10,
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                Text(stringResource(R.string.send_code), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    enabled: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.phone_label)) },
            placeholder = { Text(stringResource(R.string.phone_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = stringResource(R.string.content_description_phone),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        )

        AnimatedVisibility(visible = isError && errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = stringResource(R.string.content_description_error),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CodeStep(
    code1: String,
    code2: String,
    code3: String,
    code4: String,
    codeError: String?,
    isLoading: Boolean,
    isResendEnabled: Boolean,
    waitSeconds: Int?,
    onCodeChange: (Int, String) -> Unit,
    onResend: () -> Unit,
    onVerifyCode: () -> Unit
) {
    val focus1 = remember { FocusRequester() }
    val focus2 = remember { FocusRequester() }
    val focus3 = remember { FocusRequester() }
    val focus4 = remember { FocusRequester() }

    // Estado local para el contador
    var countdownSeconds by remember(waitSeconds) { mutableStateOf(waitSeconds ?: 0) }

    // Contador regresivo
    LaunchedEffect(waitSeconds) {
        if (waitSeconds != null && waitSeconds > 0) {
            countdownSeconds = waitSeconds
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                countdownSeconds = countdownSeconds - 1
            }
        }
    }

    // Solicitar focus al primer campo al iniciar
    LaunchedEffect(Unit) {
        focus1.requestFocus()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.enter_code),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DesignConstants.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.code_sent_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OtpDigit(
                    value = code1,
                    onValueChange = {
                        onCodeChange(1, it)
                        if (it.isNotEmpty()) focus2.requestFocus()
                    },
                    focusRequester = focus1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigit(
                    value = code2,
                    onValueChange = {
                        onCodeChange(2, it)
                        if (it.isNotEmpty()) focus3.requestFocus()
                    },
                    focusRequester = focus2,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigit(
                    value = code3,
                    onValueChange = {
                        onCodeChange(3, it)
                        if (it.isNotEmpty()) focus4.requestFocus()
                    },
                    focusRequester = focus3,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigit(
                    value = code4,
                    onValueChange = {
                        onCodeChange(4, it)
                        // Si se completaron los 4 dígitos, verificar automáticamente
                        if (it.isNotEmpty()) {
                            // Construir el código completo con el nuevo valor
                            val currentCode = "$code1$code2$code3"
                            val newValue = if (it.length == 1) it else it.last().toString()
                            val fullCode = "$currentCode$newValue"
                            if (fullCode.length == 4) {
                                onVerifyCode()
                            }
                        }
                    },
                    focusRequester = focus4,
                    modifier = Modifier.weight(1f)
                )
            }

            if (codeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = stringResource(R.string.content_description_error),
                        tint = DesignConstants.ErrorColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = codeError,
                        color = DesignConstants.ErrorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar botón o contador
            if (isResendEnabled || countdownSeconds <= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.didnt_receive_code),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.resend_code),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignConstants.PrimaryColor,
                        modifier = Modifier.clickable(enabled = isResendEnabled) { onResend() }
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.resend_wait, countdownSeconds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun OtpDigit(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .height(72.dp)
            .focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun NewPasswordStep(
    newPassword: String,
    newPasswordError: String?,
    confirmPassword: String,
    confirmPasswordError: String?,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    isLoading: Boolean,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.new_password),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DesignConstants.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.enter_new_password),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            PasswordTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = stringResource(R.string.new_password),
                placeholder = stringResource(R.string.password_placeholder),
                isError = newPasswordError != null,
                errorMessage = newPasswordError,
                passwordVisible = passwordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
                onDone = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.confirm_password_label),
                placeholder = stringResource(R.string.confirm_password_placeholder),
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                passwordVisible = confirmPasswordVisible,
                onToggleVisibility = onToggleConfirmPasswordVisibility,
                onDone = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Requisitos de contraseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DesignConstants.WarningColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.password_requirements),
                        style = MaterialTheme.typography.labelMedium,
                        color = DesignConstants.WarningColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.password_min_8), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(stringResource(R.string.password_uppercase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(stringResource(R.string.password_lowercase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(stringResource(R.string.password_number), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading &&
                        newPassword.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        newPassword == confirmPassword,
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                Text(stringResource(R.string.change_password), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onDone: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled
        )

        AnimatedVisibility(visible = isError && errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = stringResource(R.string.content_description_error),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CompleteStep(
    onBackToLogin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(DesignConstants.GradientSuccess),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.success),
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.password_changed),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DesignConstants.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.password_changed_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_link),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.content_description_error),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SuccessCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.success),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
