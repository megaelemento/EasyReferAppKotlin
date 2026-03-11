package com.christelldev.easyreferplus.ui.screens.register

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.theme.EasyReferPlusTheme
import com.christelldev.easyreferplus.ui.viewmodel.RegisterStep
import com.christelldev.easyreferplus.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

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
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_phone),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            StepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (uiState.currentStep) {
                RegisterStep.PHONE -> PhoneStepContent(
                    phone = uiState.phone,
                    phoneError = uiState.phoneError,
                    onPhoneChange = viewModel::updatePhone,
                    onSubmit = {
                        focusManager.clearFocus()
                        viewModel.verifyPhone()
                    },
                    isLoading = uiState.isLoading
                )

                RegisterStep.CODE -> CodeStepContent(
                    phone = uiState.phone,
                    code1 = uiState.code1,
                    code2 = uiState.code2,
                    code3 = uiState.code3,
                    code4 = uiState.code4,
                    codeError = uiState.codeError,
                    isResendEnabled = uiState.isResendEnabled,
                    isLoading = uiState.isLoading,
                    waitSeconds = uiState.waitSeconds,
                    onCodeChange = viewModel::updateCode,
                    onResend = viewModel::resendCode
                )

                RegisterStep.COMPLETE -> {
                    var dummy by remember { mutableStateOf("") }
                    CompleteRegistrationStep(
                        nombres = uiState.nombres,
                        nombresError = uiState.nombresError,
                        onNombresChange = viewModel::updateNombres,
                        onNombresNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
                        value = uiState.apellidos,
                        error = uiState.apellidosError,
                        onApellidosChange = viewModel::updateApellidos,
                        cedulaRuc = uiState.cedulaRuc,
                        cedulaRucError = uiState.cedulaRucError,
                        onCedulaRucChange = viewModel::updateCedulaRuc,
                        email = uiState.email,
                        emailError = uiState.emailError,
                        onEmailChange = viewModel::updateEmail,
                        password = uiState.password,
                        passwordError = uiState.passwordError,
                        passwordVisible = uiState.passwordVisible,
                        onPasswordChange = viewModel::updatePassword,
                        onTogglePassword = viewModel::togglePasswordVisibility,
                        confirmPassword = uiState.confirmPassword,
                        confirmPasswordError = uiState.confirmPasswordError,
                        confirmPasswordVisible = uiState.confirmPasswordVisible,
                        onConfirmPasswordChange = viewModel::updateConfirmPassword,
                        onToggleConfirmPassword = viewModel::toggleConfirmPasswordVisibility,
                        referralCode = uiState.referralCode,
                        referralCodeError = uiState.referralCodeError,
                        onReferralCodeChange = viewModel::updateReferralCode,
                        isAdult = uiState.isAdult,
                        onToggleAdult = viewModel::toggleIsAdult,
                        acceptsPrivacyPolicy = uiState.acceptsPrivacyPolicy,
                        acceptsPrivacyPolicyError = uiState.acceptsPrivacyPolicyError,
                        onPrivacyPolicyClick = viewModel::onPrivacyPolicyCheckClick,
                        showPrivacyPolicyDialog = uiState.showPrivacyPolicyDialog,
                        privacyPolicyTitle = uiState.privacyPolicyTitle,
                        privacyPolicyContent = uiState.privacyPolicyContent,
                        isLoadingPrivacyPolicy = uiState.isLoadingPrivacyPolicy,
                        onAcceptPrivacyPolicy = viewModel::acceptPrivacyPolicy,
                        onDismissPrivacyPolicyDialog = viewModel::dismissPrivacyPolicyDialog,
                        isLoading = uiState.isLoading,
                        onSubmit = {
                            focusManager.clearFocus()
                            viewModel.completeRegistration()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.currentStep == RegisterStep.PHONE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.already_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.login_link),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { snackbarData ->
            // Snackbar personalizado con mejor visibilidad
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: RegisterStep, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepItem(
                number = 1,
                label = stringResource(R.string.step_phone),
                isActive = currentStep == RegisterStep.PHONE || currentStep == RegisterStep.CODE || currentStep == RegisterStep.COMPLETE,
                isCompleted = currentStep == RegisterStep.CODE || currentStep == RegisterStep.COMPLETE
            )
            LineConnector(isActive = currentStep == RegisterStep.CODE || currentStep == RegisterStep.COMPLETE, modifier = Modifier.weight(1f))
            StepItem(
                number = 2,
                label = stringResource(R.string.step_code),
                isActive = currentStep == RegisterStep.CODE || currentStep == RegisterStep.COMPLETE,
                isCompleted = currentStep == RegisterStep.COMPLETE
            )
            LineConnector(isActive = currentStep == RegisterStep.COMPLETE, modifier = Modifier.weight(1f))
            StepItem(
                number = 3,
                label = stringResource(R.string.step_complete),
                isActive = currentStep == RegisterStep.COMPLETE,
                isCompleted = false
            )
        }
    }
}

@Composable
fun StepItem(number: Int, label: String, isActive: Boolean, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    brush = when {
                        isCompleted -> Brush.linearGradient(DesignConstants.GradientSuccess)
                        isActive -> Brush.linearGradient(DesignConstants.GradientPrimary)
                        else -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) DesignConstants.PrimaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LineConnector(isActive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
    )
}

@Composable
fun PhoneStepContent(
    phone: String,
    phoneError: String?,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
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
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(R.string.phone_label)) },
                placeholder = { Text(stringResource(R.string.phone_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DesignConstants.PrimaryColor
                    )
                },
                isError = phoneError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = phoneError != null) {
                ErrorText(phoneError)
            }

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
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(R.string.send_code), 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
fun CodeStepContent(
    phone: String,
    code1: String, code2: String, code3: String, code4: String,
    codeError: String?,
    isResendEnabled: Boolean,
    isLoading: Boolean,
    waitSeconds: Int?,
    onCodeChange: (Int, String) -> Unit,
    onResend: () -> Unit
) {
    val focus1 = remember { FocusRequester() }
    val focus2 = remember { FocusRequester() }
    val focus3 = remember { FocusRequester() }
    val focus4 = remember { FocusRequester() }

    // Estado local para el contador - iniciar con waitSeconds
    var countdownSeconds by remember(waitSeconds) { mutableStateOf(waitSeconds ?: 0) }

    // Contador regresivo - solo se activa cuando waitSeconds cambia (no cuando countdownSeconds cambia)
    LaunchedEffect(waitSeconds) {
        if (waitSeconds != null && waitSeconds > 0) {
            countdownSeconds = waitSeconds
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                countdownSeconds = countdownSeconds - 1
            }
        }
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
                text = stringResource(R.string.step_code),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DesignConstants.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enviamos un código a +593${phone.removePrefix("0")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OtpDigitField(
                    value = code1,
                    onValueChange = { value ->
                        onCodeChange(1, value)
                        if (value.isNotEmpty()) focus2.requestFocus()
                    },
                    focusRequester = focus1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigitField(
                    value = code2,
                    onValueChange = { value ->
                        onCodeChange(2, value)
                        if (value.isNotEmpty()) focus3.requestFocus()
                    },
                    focusRequester = focus2,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigitField(
                    value = code3,
                    onValueChange = { value ->
                        onCodeChange(3, value)
                        if (value.isNotEmpty()) focus4.requestFocus()
                    },
                    focusRequester = focus3,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OtpDigitField(
                    value = code4,
                    onValueChange = { value ->
                        onCodeChange(4, value)
                    },
                    focusRequester = focus4,
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(visible = codeError != null) {
                ErrorText(codeError)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar botón cuando countdown llegó a 0 o cuando está habilitado
            if (isResendEnabled || countdownSeconds <= 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.didnt_receive_code),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                    text = "Reenviar en $countdownSeconds segundos...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun OtpDigitField(
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
        modifier = modifier.height(64.dp).focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}

@Composable
fun CompleteRegistrationStep(
    nombres: String, nombresError: String?, onNombresChange: (String) -> Unit, onNombresNext: () -> Unit,
    value: String, error: String?, onApellidosChange: (String) -> Unit,
    cedulaRuc: String, cedulaRucError: String?, onCedulaRucChange: (String) -> Unit,
    email: String, emailError: String?, onEmailChange: (String) -> Unit,
    password: String, passwordError: String?, passwordVisible: Boolean, onPasswordChange: (String) -> Unit, onTogglePassword: () -> Unit,
    confirmPassword: String, confirmPasswordError: String?, confirmPasswordVisible: Boolean, onConfirmPasswordChange: (String) -> Unit, onToggleConfirmPassword: () -> Unit,
    referralCode: String, referralCodeError: String?, onReferralCodeChange: (String) -> Unit,
    isAdult: Boolean, onToggleAdult: () -> Unit,
    acceptsPrivacyPolicy: Boolean, acceptsPrivacyPolicyError: String?, onPrivacyPolicyClick: () -> Unit,
    showPrivacyPolicyDialog: Boolean, privacyPolicyTitle: String, privacyPolicyContent: String,
    isLoadingPrivacyPolicy: Boolean, onAcceptPrivacyPolicy: () -> Unit, onDismissPrivacyPolicyDialog: () -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit
) {
    // Diálogo de Política de Privacidad
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            title = privacyPolicyTitle,
            content = privacyPolicyContent,
            isLoading = isLoadingPrivacyPolicy,
            onAccept = onAcceptPrivacyPolicy,
            onDismiss = onDismissPrivacyPolicyDialog
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = DesignConstants.CARD_ELEVATION, shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            OutlinedTextField(
                value = nombres,
                onValueChange = onNombresChange,
                label = { Text(stringResource(R.string.nombres_label)) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = DesignConstants.PrimaryColor) },
                isError = nombresError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { onNombresNext() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = nombresError != null) { ErrorText(nombresError) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onApellidosChange,
                label = { Text(stringResource(R.string.apellidos_label)) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = DesignConstants.PrimaryColor) },
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = error != null) { ErrorText(error) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cedulaRuc,
                onValueChange = onCedulaRucChange,
                label = { Text(stringResource(R.string.cedula_ruc_label)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = DesignConstants.PrimaryColor) },
                isError = cedulaRucError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = cedulaRucError != null) { ErrorText(cedulaRucError) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                leadingIcon = { Icon(Icons.Default.Mail, null, tint = DesignConstants.PrimaryColor) },
                isError = emailError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = emailError != null) { ErrorText(emailError) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_create_label)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = DesignConstants.PrimaryColor) },
                trailingIcon = { IconButton(onClick = onTogglePassword) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = DesignConstants.PrimaryColor) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = passwordError != null) { ErrorText(passwordError) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(R.string.confirm_password_label)) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = DesignConstants.PrimaryColor) },
                trailingIcon = { IconButton(onClick = onToggleConfirmPassword) { Icon(if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = DesignConstants.PrimaryColor) } },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = confirmPasswordError != null) { ErrorText(confirmPasswordError) }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = referralCode,
                onValueChange = onReferralCodeChange,
                label = { Text(stringResource(R.string.referral_code_label)) },
                placeholder = { Text(stringResource(R.string.referral_code_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = DesignConstants.PrimaryColor) },
                isError = referralCodeError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesignConstants.PrimaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = DesignConstants.ErrorColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = referralCodeError != null) { ErrorText(referralCodeError) }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().clickable { onToggleAdult() }, verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Checkbox(checked = isAdult, onCheckedChange = { onToggleAdult() }, colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = DesignConstants.PrimaryColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.is_adult), style = MaterialTheme.typography.bodyMedium, color = DesignConstants.TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth().clickable { onPrivacyPolicyClick() }, verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Checkbox(checked = acceptsPrivacyPolicy, onCheckedChange = { onPrivacyPolicyClick() }, colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = DesignConstants.PrimaryColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.accepts_privacy_policy), style = MaterialTheme.typography.bodyMedium, color = if (acceptsPrivacyPolicyError != null) DesignConstants.ErrorColor else DesignConstants.TextPrimary)
            }

            AnimatedVisibility(visible = acceptsPrivacyPolicyError != null) { ErrorText(acceptsPrivacyPolicyError) }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && isAdult && acceptsPrivacyPolicy,
                shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.register_button), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}

@Composable
fun ErrorText(message: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(message ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PrivacyPolicyDialog(
    title: String,
    content: String,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title.ifEmpty { "Política de Privacidad" },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = content.ifEmpty { "Cargando contenido..." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onAccept,
                enabled = !isLoading
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    EasyReferPlusTheme {
        var phone by remember { mutableStateOf("0987654321") }
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                    label = { Text(stringResource(R.string.phone_label)) },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {}, 
                    modifier = Modifier.fillMaxWidth().height(56.dp), 
                    shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                ) {
                    Text(stringResource(R.string.send_code), color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}
