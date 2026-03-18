package com.christelldev.easyreferplus.ui.screens.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
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

    LaunchedEffect(Unit) {
        viewModel.goToPhoneStep()
    }

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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Fondo con gradiente sutil superior (Consistente con Login)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

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
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color.White,
                    fontWeight = FontWeight.ExtraBold
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
                    CompleteRegistrationStep(
                        nombres = uiState.nombres,
                        nombresError = uiState.nombresError,
                        onNombresChange = viewModel::updateNombres,
                        onNombresNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
                        apellidos = uiState.apellidos,
                        apellidosError = uiState.apellidosError,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.already_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.login_link),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = snackbarData.visuals.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: RegisterStep, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
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
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LineConnector(isActive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(3.dp)
            .padding(horizontal = 6.dp)
            .background(
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
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
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                isError = phoneError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            AnimatedVisibility(visible = phoneError != null, enter = fadeIn(), exit = fadeOut()) {
                ErrorText(phoneError)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && phone.length == 10,
                shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(R.string.send_code), 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
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

    var countdownSeconds by remember(waitSeconds) { mutableStateOf(waitSeconds ?: 0) }

    LaunchedEffect(waitSeconds) {
        if (waitSeconds != null && waitSeconds > 0) {
            countdownSeconds = waitSeconds
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                countdownSeconds -= 1
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Verificación de Código",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enviamos un código de 4 dígitos a\n+593${phone.removePrefix("0")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                Spacer(modifier = Modifier.width(12.dp))
                OtpDigitField(
                    value = code2,
                    onValueChange = { value ->
                        onCodeChange(2, value)
                        if (value.isNotEmpty()) focus3.requestFocus()
                        else focus1.requestFocus()
                    },
                    focusRequester = focus2,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OtpDigitField(
                    value = code3,
                    onValueChange = { value ->
                        onCodeChange(3, value)
                        if (value.isNotEmpty()) focus4.requestFocus()
                        else focus2.requestFocus()
                    },
                    focusRequester = focus3,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OtpDigitField(
                    value = code4,
                    onValueChange = { value ->
                        onCodeChange(4, value)
                        if (value.isEmpty()) focus3.requestFocus()
                    },
                    focusRequester = focus4,
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(visible = codeError != null, enter = fadeIn(), exit = fadeOut()) {
                ErrorText(codeError)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isResendEnabled || countdownSeconds <= 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.didnt_receive_code),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.resend_code),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = isResendEnabled) { onResend() }
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reenviar en $countdownSeconds segundos",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        modifier = modifier
            .height(68.dp)
            .focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}

@Composable
fun CompleteRegistrationStep(
    nombres: String, nombresError: String?, onNombresChange: (String) -> Unit, onNombresNext: () -> Unit,
    apellidos: String, apellidosError: String?, onApellidosChange: (String) -> Unit,
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
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            title = privacyPolicyTitle,
            content = privacyPolicyContent,
            isLoading = isLoadingPrivacyPolicy,
            onAccept = onAcceptPrivacyPolicy,
            onDismiss = onDismissPrivacyPolicyDialog
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignConstants.CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                text = "Datos Personales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            RegistrationTextField(
                value = nombres,
                onValueChange = onNombresChange,
                label = stringResource(R.string.nombres_label),
                icon = Icons.Default.Person,
                error = nombresError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { onNombresNext() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = apellidos,
                onValueChange = onApellidosChange,
                label = stringResource(R.string.apellidos_label),
                icon = Icons.Default.Person,
                error = apellidosError
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = cedulaRuc,
                onValueChange = onCedulaRucChange,
                label = stringResource(R.string.cedula_ruc_label),
                icon = Icons.Default.Lock,
                error = cedulaRucError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.email_label),
                icon = Icons.Default.Mail,
                error = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.password_create_label),
                icon = Icons.Default.Lock,
                error = passwordError,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = onTogglePassword,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.confirm_password_label),
                icon = Icons.Default.Lock,
                error = confirmPasswordError,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePassword = onToggleConfirmPassword,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationTextField(
                value = referralCode,
                onValueChange = onReferralCodeChange,
                label = stringResource(R.string.referral_code_label),
                placeholder = stringResource(R.string.referral_code_placeholder),
                icon = Icons.Default.Person,
                error = referralCodeError
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onToggleAdult() }, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAdult, 
                            onCheckedChange = { onToggleAdult() }, 
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.is_adult), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onPrivacyPolicyClick() }, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptsPrivacyPolicy, 
                            onCheckedChange = { onPrivacyPolicyClick() }, 
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = if (acceptsPrivacyPolicyError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.accepts_privacy_policy), 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = if (acceptsPrivacyPolicyError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (acceptsPrivacyPolicyError != null) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = acceptsPrivacyPolicyError != null, enter = fadeIn(), exit = fadeOut()) { 
                ErrorText(acceptsPrivacyPolicyError) 
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && isAdult && acceptsPrivacyPolicy,
                shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(
                        stringResource(R.string.register_button), 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
    placeholder: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        )
        AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
            ErrorText(error)
        }
    }
}

@Composable
fun ErrorText(message: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = message ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
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
                    shape = RoundedCornerShape(DesignConstants.BUTTON_CORNER_RADIUS),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                ) {
                    Text(stringResource(R.string.send_code), color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}
