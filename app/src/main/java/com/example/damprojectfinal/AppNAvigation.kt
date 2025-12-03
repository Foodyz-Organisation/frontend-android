package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.AuthApiService

import com.example.damprojectfinal.feature_auth.ui.ForgetPasswordScreen
import com.example.damprojectfinal.feature_auth.ui.LoginScreen
import com.example.damprojectfinal.feature_auth.ui.SignupScreen
import com.example.damprojectfinal.feature_auth.ui.SplashScreen
import com.example.damprojectfinal.feature_auth.ui.ProSignupScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.ui.theme.screens.chat.ChatDetailScreen
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.ui.theme.screens.chat.ChatManagementScreen
import com.example.damprojectfinal.core.api.TokenManager


/**
 * Define all the routes for the authentication flow
 */
object AuthRoutes {
    const val SPLASH = "splash_route"
    const val LOGIN = "login_route"
    const val SIGNUP = "signup_route"
    const val FORGET_PASSWORD = "forget_password_route"
    const val PRO_SIGNUP = "pro_signup_route" // Added Pro Signup route
}

/**
 * Routes for authenticated user flow
 */
object UserRoutes {
    const val HOME_SCREEN = "home_screen"
    const val HOME_SCREEN_PRO = "home_screen_pro" // professional dashboard
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authApiService = AuthApiService()

    val startDestination = AuthRoutes.SPLASH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1️⃣ Splash Screen
        composable(AuthRoutes.SPLASH) {
            val context = LocalContext.current
            val nextRoute = remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val authState = TokenManager(context).getAuthState()
                nextRoute.value = when (authState?.role) {
                    "professional" -> "${UserRoutes.HOME_SCREEN_PRO}/${authState.userId}"
                    "user" -> UserRoutes.HOME_SCREEN
                    else -> null
                } ?: AuthRoutes.LOGIN
            }

            SplashScreen(
                durationMs = 1600,
                onFinished = {
                    val target = nextRoute.value ?: AuthRoutes.LOGIN
                    navController.navigate(target) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2️⃣ Login Screen
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                navController = navController,
                authApiService = authApiService,
                onNavigateToSignup = {
                    navController.navigate(AuthRoutes.SIGNUP)
                },
                onNavigateToForgetPassword = {
                    navController.navigate(AuthRoutes.FORGET_PASSWORD)
                }
            )
        }

        // 3️⃣ Signup Screen
        composable(AuthRoutes.SIGNUP) {
            SignupScreen(
                navController = navController,
                onNavigateToLogin = {
                    navController.navigate(AuthRoutes.LOGIN)
                }
            )
        }

        // 4️⃣ Forget Password Screen
        composable(AuthRoutes.FORGET_PASSWORD) {
            ForgetPasswordScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onStartReset = { email ->
                    println("Sending reset email to: $email")
                }
            )
        }

        // 5️⃣ User Home/Home Screen
        composable(UserRoutes.HOME_SCREEN) {
            HomeScreen(navController = navController, currentRoute = UserRoutes.HOME_SCREEN)
        }

        // 6️⃣ Professional Account
        composable("${UserRoutes.HOME_SCREEN_PRO}/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "unknown"
            Text(text = "Professional Account for User ID: $userId", modifier = Modifier.fillMaxSize())
        }

        // 7️⃣ Professional Signup Screen
        composable(AuthRoutes.PRO_SIGNUP) {
            ProSignupScreen(
                navController = navController,
                authApiService = authApiService
            )
        }

        // Chat management (copied from other project, minimal integration)
        composable("chatList") {
            ChatManagementScreen(navController = navController)
        }

        composable("chatDetail/{conversationId}/{chatName}/{currentUserId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
            // On récupère le currentUserId passé dans la route si disponible,
            // sinon on peut tomber sur une valeur par défaut récupérée depuis l'auth
            val currentUserId = backStackEntry.arguments?.getString("currentUserId") ?: "USER_ID_DEPuis_AUTH"

            ChatDetailScreen(
                chatName = chatName,
                conversationId = conversationId,
                currentUserId = currentUserId,
                navController = navController
            )
        }


        composable("${UserRoutes.HOME_SCREEN_PRO}/{professionalId}") { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            HomeScreenPro(
                professionalId = professionalId,
                navController = navController
            )
        }
    }
}
