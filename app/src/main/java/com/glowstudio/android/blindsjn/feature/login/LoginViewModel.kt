package com.glowstudio.android.blindsjn.feature.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glowstudio.android.blindsjn.data.network.AuthRepository
import com.glowstudio.android.blindsjn.data.network.AutoLoginManager
import com.glowstudio.android.blindsjn.data.network.isNetworkAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val autoLoginEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val showEmptyFieldsPopup: Boolean = false,
    val showInvalidCredentialsPopup: Boolean = false,
    val showNetworkErrorPopup: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var onLoginSuccess: ((Boolean) -> Unit)? = null

    /**
     * Sets a callback to be invoked when a login attempt completes.
     *
     * @param callback Function to be called with `true` if login succeeds, `false` otherwise.
     */
    fun setOnLoginSuccess(callback: (Boolean) -> Unit) {
        onLoginSuccess = callback
    }

    /**
     * Updates the phone number in the UI state, allowing only digit characters.
     *
     * Non-digit characters in the input are removed before updating the state.
     *
     * @param phoneNumber The input string to set as the phone number.
     */
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber.filter { it.isDigit() }
        )
    }

    /**
     * Updates the password field in the login UI state.
     *
     * @param password The new password entered by the user.
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    /**
     * Updates the auto-login enabled state in the UI.
     *
     * @param enabled Whether auto-login should be enabled.
     */
    fun updateAutoLogin(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoLoginEnabled = enabled)
    }

    /**
     * Checks if auto-login is enabled and, if so, attempts to log in using saved credentials.
     *
     * Updates the UI state to reflect network errors, loading status, and invalid credentials as appropriate.
     * Invokes the login success callback if login is successful.
     */
    fun checkAutoLogin(context: Context) {
        viewModelScope.launch {
            if (!isNetworkAvailable(context)) {
                _uiState.value = _uiState.value.copy(showNetworkErrorPopup = true)
                return@launch
            }

            val autoLoginEnabled = AutoLoginManager.isAutoLoginEnabled(context)
            _uiState.value = _uiState.value.copy(autoLoginEnabled = autoLoginEnabled)

            if (autoLoginEnabled) {
                AutoLoginManager.getSavedCredentials(context)?.let { (savedPhone, savedPassword) ->
                    _uiState.value = _uiState.value.copy(
                        phoneNumber = savedPhone,
                        password = savedPassword,
                        isLoading = true
                    )
                    try {
                        val success = AuthRepository.login(context, savedPhone, savedPassword)
                        if (success) {
                            onLoginSuccess?.invoke(true)
                        } else {
                            _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                    } finally {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    /**
     * Attempts to log in with the provided phone number and password.
     *
     * Validates input fields and network availability before performing the login operation asynchronously.
     * Updates the UI state to reflect loading, error popups for empty fields, invalid credentials, or network errors.
     * On successful login, saves login information and invokes the success callback if set.
     */
    fun login(context: Context, phoneNumber: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            if (phoneNumber.isEmpty() || password.isEmpty()) {
                _uiState.value = _uiState.value.copy(showEmptyFieldsPopup = true)
                return@launch
            }

            if (!isNetworkAvailable(context)) {
                _uiState.value = _uiState.value.copy(showNetworkErrorPopup = true)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val success = AuthRepository.login(context, phoneNumber, password)
                if (success) {
                    AutoLoginManager.saveLoginInfo(
                        context,
                        phoneNumber,
                        password,
                        _uiState.value.autoLoginEnabled
                    )
                    onLoginSuccess?.invoke(true)
                } else {
                    _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = true)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Hides the popup indicating that required fields are empty.
     */
    fun dismissEmptyFieldsPopup() {
        _uiState.value = _uiState.value.copy(showEmptyFieldsPopup = false)
    }

    /**
     * Hides the invalid credentials popup in the login UI state.
     */
    fun dismissInvalidCredentialsPopup() {
        _uiState.value = _uiState.value.copy(showInvalidCredentialsPopup = false)
    }

    /**
     * Hides the network error popup in the login UI state.
     */
    fun dismissNetworkErrorPopup() {
        _uiState.value = _uiState.value.copy(showNetworkErrorPopup = false)
    }
}