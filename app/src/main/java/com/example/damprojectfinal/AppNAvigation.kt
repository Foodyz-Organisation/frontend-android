package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.DebugUserLogger
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.core.repository.UserRepository
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.utils.LogoutViewModelFactory
import com.example.damprojectfinal.feature_auth.ui.*
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_menu.ui.MenuItemManagementScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.CreateMenuItemScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.ItemDetailsScreen
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.user.feature_profile.ui.UpdateProfileScreen
import com.example.damprojectfinal.user.feature_profile.ui.UserProfile
import com.example.damprojectfinal.user.feature_profile.ui.UserProfileScreen
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel
import com.google.gson.Gson
import androidx.compose.runtime.getValue // <-- Add this!
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
    // Base route for navigation must match the NavHost setup, including argument placeholders
    const val PROFILE_VIEW = "profile_view/{userId}"
    const val PROFILE_UPDATE = "profile_update/{userId}"
}

object ProRoutes {
    const val MENU_MANAGEMENT = "menu_management/{professionalId}"
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authApiService = AuthApiService()

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    // Initialize User API Service and Repository for ProfileViewModel
    val userApiService = remember { UserApiService() }
    val userRepository = remember { UserRepository(userApiService) }

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

        // In your AppNavigation.kt file

        composable(
            route = UserRoutes.PROFILE_VIEW,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for profile view.")

            // --- Setup ViewModel ---
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository)
            )

            val token = "YOUR_ACCESS_TOKEN"

            // Fetch the user profile when the screen is composed
            LaunchedEffect(userId, token) {
                if (token.isNotBlank()) {
                    profileViewModel.fetchUserProfile(userId, token)
                }
            }

            // --- Corrected Composable Call ---
            UserProfileScreen(
                viewModel = profileViewModel,

                // ⭐ FIX: Implement the onBackClick action to return to the previous screen
                onBackClick = { navController.popBackStack() },

                onEditProfileClick = {
                    navController.navigate("profile_update/$userId")
                }
            )
        }
        // ----------------------------------------------------
        // ⭐ PROFILE UPDATE SCREEN (USER) ⭐
        // ----------------------------------------------------

        composable(
            route = UserRoutes.PROFILE_UPDATE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for profile update.")

            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository)
            )

            // You MUST provide the access token here (e.g., from a token manager/session)
            val token = "YOUR_ACCESS_TOKEN" // Replace with actual token retrieval logic

            // ⭐ CRITICAL FIX: Fetch data when the screen is accessed ⭐
            LaunchedEffect(userId, token) {
                // Only fetch if the ViewModel doesn't already have the data
                if (token.isNotBlank() && profileViewModel.userState.value == null) {
                    profileViewModel.fetchUserProfile(userId, token)
                }
            }

            // ... rest of the navigation logic (LaunchedEffect for updateSuccess) ...

            UpdateProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onUpdateSuccess = { /* ... */ }
            )
        }
        // ----------------------------------------------------
        // HomeScreen
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
                onLogout = {
                    // 1. Perform logout logic (clear tokens etc.)
                    logoutViewModel.logout()

                    // 2. Navigate to Login and clear the whole back stack
                    navController.navigate(AuthRoutes.LOGIN) {
                        // Pop everything in the current graph (root) off the back stack
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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

            val repository = remember { MenuItemRepository(RetrofitClient.menuItemApi, Gson()) }

            val menuItemViewModel: MenuViewModel = viewModel(
                factory = MenuViewModel.Factory(repository)
            )

            val lifecycleOwner: LifecycleOwner = backStackEntry
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner
            ) {
                MenuItemManagementScreen(
                    navController = navController,
                    professionalId = professionalId,
                    viewModel = menuItemViewModel
                )
            }

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

        composable(
            route = "edit_menu_item/{itemId}/{professionalId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("professionalId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val itemId = backStackEntry.arguments?.getString("itemId")
                ?: throw IllegalStateException("itemId is required")
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("professionalId is required")

            val repository = remember { MenuItemRepository(RetrofitClient.menuItemApi, Gson()) }

            val menuItemViewModel: MenuViewModel = viewModel(
                factory = MenuViewModel.Factory(repository)
            )

            ItemDetailsScreen(
                itemId = itemId,
                professionalId = professionalId,
                viewModel = menuItemViewModel,
                navController = navController
            )
        }
    }
}