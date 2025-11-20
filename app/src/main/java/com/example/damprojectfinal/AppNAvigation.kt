package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.DebugUserLogger
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.utils.LogoutViewModelFactory
import com.example.damprojectfinal.feature_auth.ui.*
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_menu.ui.MenuItemManagementScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.CreateMenuItemScreen
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.user.common.HomeScreen
import com.google.gson.Gson

object AuthRoutes {
    const val SPLASH = "splash_route"
    const val LOGIN = "login_route"
    const val SIGNUP = "signup_route"
    const val FORGET_PASSWORD = "forget_password_route"
    const val PRO_SIGNUP = "pro_signup_route"
}

object UserRoutes {
    const val HOME_SCREEN = "home_screen"
    const val HOME_SCREEN_PRO = "home_screen_pro"
}

object ProRoutes {
    const val MENU_MANAGEMENT = "menu_management/{professionalId}"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authApiService = AuthApiService()

    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Debugger runs at the highest level
    DebugUserLogger(tokenManager = tokenManager)

    NavHost(
        navController = navController,
        startDestination = AuthRoutes.SPLASH,
        modifier = modifier
    ) {
        // Splash
        composable(AuthRoutes.SPLASH) {
            SplashScreen(
                durationMs = 1600,
                // ✅ FIX: Using the correct single callback function signature and adding explicit types
                onAuthCheckComplete = { userId: String?, role: String? ->
                    val destinationRoute: String = when (role?.lowercase()) {
                        // 1. PROFESSIONAL LOGIC: Navigate to Pro Home with ID
                        "professional" ->
                            // Added parentheses around the negation for compiler robustness
                            if (!(userId.isNullOrEmpty())) {
                                "${UserRoutes.HOME_SCREEN_PRO}/$userId"
                            } else {
                                AuthRoutes.LOGIN // Fallback if ID is missing
                            }

                        // 2. USER (Normal) LOGIC: Navigate to standard user home
                        "user" -> UserRoutes.HOME_SCREEN

                        // 3. LOGGED OUT / UNKNOWN ROLE LOGIC: Default to Login screen
                        else -> AuthRoutes.LOGIN
                    }

                    navController.navigate(destinationRoute) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                },
                tokenManager = tokenManager
            )
        }

        // Login
        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                navController = navController,
                authApiService = authApiService,
                tokenManager = tokenManager,
                onNavigateToSignup = { navController.navigate(AuthRoutes.SIGNUP) },
                onNavigateToForgetPassword = { navController.navigate(AuthRoutes.FORGET_PASSWORD) }
            )
        }

        // Signup
        composable(AuthRoutes.SIGNUP) {
            SignupScreen(
                navController = navController,
                onNavigateToLogin = { navController.navigate(AuthRoutes.LOGIN) }
            )
        }

        // Forget Password
        composable(AuthRoutes.FORGET_PASSWORD) {
            ForgetPasswordScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() },
                onStartReset = { email ->
                    println("Sending reset email to: $email")
                }
            )
        }

        composable(UserRoutes.HOME_SCREEN) {

            val context = LocalContext.current

            val logoutViewModel: LogoutViewModel = viewModel(
                factory = LogoutViewModelFactory(
                    authApiService = AuthApiService(),
                    tokenManager = TokenManager(context)
                )
            )

            HomeScreen(
                navController = navController,
                currentRoute = UserRoutes.HOME_SCREEN,
                onLogout = { logoutViewModel.logout() },
                logoutSuccess = logoutViewModel.logoutSuccess
            )
        }


        // Professional Signup
        composable(AuthRoutes.PRO_SIGNUP) {
            ProSignupScreen(navController = navController, authApiService = authApiService)
        }

        // Professional Home
        composable("${UserRoutes.HOME_SCREEN_PRO}/{professionalId}") { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            HomeScreenPro(professionalId, navController)
        }



        // ---------- Menu Management ----------
        composable(
            route = ProRoutes.MENU_MANAGEMENT,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->

            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("professionalId must be provided.")

            val gson = remember { Gson() } // Create Gson instance once per composition
            val repository = remember { MenuItemRepository(RetrofitClient.menuItemApi, gson) }

            val menuItemViewModel: MenuViewModel = viewModel(
                factory = MenuViewModel.Factory(repository)
            )

            MenuItemManagementScreen(
                navController = navController,
                professionalId = professionalId,
                viewModel = menuItemViewModel // ✅ Correct instance
            )
        }

// ---------- Create Menu Item ----------
        composable(
            route = "create_menu_item/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->

            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("professionalId is required")

            val context = LocalContext.current
            val gson = remember { Gson() } // Create Gson instance once per composition
            val repository = remember { MenuItemRepository(RetrofitClient.menuItemApi, gson) }

            val menuItemViewModel: MenuViewModel = viewModel(
                factory = MenuViewModel.Factory(repository)
            )

            CreateMenuItemScreen(
                navController = navController,
                professionalId = professionalId,
                viewModel = menuItemViewModel,
                context = context
            )
        }
    }
}