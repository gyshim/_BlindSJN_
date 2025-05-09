package com.glowstudio.android.blindsjn.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.glowstudio.android.blindsjn.feature.login.LoginScreen
import com.glowstudio.android.blindsjn.feature.login.SignupScreen

/**
 * Defines the authentication navigation graph with login, signup, and forgot password routes.
 *
 * Sets up navigation destinations for authentication-related screens. Invokes the provided callback upon successful authentication.
 *
 * @param navController Controller used to manage navigation within the authentication graph.
 * @param onAuthSuccess Callback invoked when authentication succeeds.
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        startDestination = "login",
        route = "auth"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { success ->
                    if (success) onAuthSuccess()
                },
                onSignupClick = { navController.navigate("signup") },
                onForgotPasswordClick = { navController.navigate("forgot") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupClick = { phoneNumber, password ->
                    onAuthSuccess()
                },
                onBackToLoginClick = { navController.navigateUp() }
            )
        }

        composable("forgot") {
            // TODO: ForgotPasswordScreen 구현
        }
    }
} 