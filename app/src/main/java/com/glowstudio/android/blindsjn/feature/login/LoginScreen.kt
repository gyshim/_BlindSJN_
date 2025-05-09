package com.glowstudio.android.blindsjn.feature.login

/**
 * 로그인 스크린 로직
 *
 * TODO: 자동로그인 체크 박스
 **/

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.data.model.LoginRequest
import com.glowstudio.android.blindsjn.data.network.InternalServer
import com.glowstudio.android.blindsjn.data.network.AuthRepository
import com.glowstudio.android.blindsjn.R
import com.glowstudio.android.blindsjn.data.network.AutoLoginManager
import com.glowstudio.android.blindsjn.data.network.isNetworkAvailable
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import com.glowstudio.android.blindsjn.ui.components.*
import androidx.lifecycle.viewmodel.compose.viewModel

// 로그인 함수 (서버 통신)
/**
 * Attempts to authenticate a user with the provided phone number and password.
 *
 * Sends a login request to the server and returns `true` if authentication is successful, or `false` otherwise.
 *
 * @param phoneNumber The user's phone number.
 * @param password The user's password.
 * @return `true` if login is successful; `false` if credentials are invalid or a network error occurs.
 */
suspend fun login(phoneNumber: String, password: String): Boolean {
    val request = LoginRequest(phoneNumber, password)
    val response = InternalServer.api.login(request)

    return if (response.isSuccessful) {
        val result = response.body()
        Log.d("LoginScreen", "Login result: $result")
        result?.status == "success"
    } else {
        Log.e("LoginScreen", "Error: ${response.errorBody()?.string()}")
        false
    }
}

/**
 * Displays the login screen UI, handling user input, login actions, and related dialogs.
 *
 * Presents input fields for phone number and password, auto-login toggle, and navigation options for sign-up and password recovery. Manages UI state and side effects via the provided [LoginViewModel]. Shows appropriate dialogs for empty fields, invalid credentials, and network errors. Invokes [onLoginClick] upon successful login, and provides callbacks for sign-up and password reset actions.
 *
 * @param onLoginClick Called with `true` when login succeeds.
 * @param onSignupClick Invoked when the user selects the sign-up option.
 * @param onForgotPasswordClick Invoked when the user selects the forgot password option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginClick: (Boolean) -> Unit,
    onSignupClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // onLoginSuccess 콜백 설정
    LaunchedEffect(Unit) {
        viewModel.setOnLoginSuccess { success ->
            if (success) {
                onLoginClick(true)
            }
        }
    }

    // 자동 로그인 체크
    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_image),
                contentDescription = "Login Image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 환영 메시지
        Text(
            text = "어서오세요, 사장님!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 입력 필드들을 담을 Column
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 전화번호 입력
            CommonTextField(
                value = uiState.phoneNumber,
                onValueChange = { viewModel.updatePhoneNumber(it) },
                label = "전화번호",
                placeholder = "전화번호를 입력하세요",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            CommonTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = "비밀번호",
                placeholder = "비밀번호를 입력하세요",
                isPassword = true
            )

            // 자동 로그인과 비밀번호 찾기
            AutoLoginRow(
                autoLoginEnabled = uiState.autoLoginEnabled,
                onAutoLoginChange = { viewModel.updateAutoLogin(it) },
                onForgotPasswordClick = onForgotPasswordClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인 버튼
            CommonButton(
                text = "로그인",
                onClick = {
                    viewModel.login(context, uiState.phoneNumber, uiState.password)
                },
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입 안내
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "계정이 없으신가요? ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onSignupClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "회원가입",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 팝업들
        if (uiState.showEmptyFieldsPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissEmptyFieldsPopup() },
                title = { Text("입력 오류") },
                text = { Text("전화번호와 비밀번호를 입력해주세요.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissEmptyFieldsPopup() }) {
                        Text("확인")
                    }
                }
            )
        }

        if (uiState.showInvalidCredentialsPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissInvalidCredentialsPopup() },
                title = { Text("로그인 실패") },
                text = { Text("전화번호 또는 비밀번호가 올바르지 않습니다.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissInvalidCredentialsPopup() }) {
                        Text("확인")
                    }
                }
            )
        }

        if (uiState.showNetworkErrorPopup) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissNetworkErrorPopup() },
                title = { Text("네트워크 오류") },
                text = { Text("인터넷 연결이 필요합니다. 연결 상태를 확인해주세요.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissNetworkErrorPopup() }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

/**
 * Displays a preview of the login screen composable with default callback implementations.
 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = {},
        onSignupClick = {},
        onForgotPasswordClick = {}
    )
}