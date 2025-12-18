package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.util.Log
import android.widget.Toast
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.damprojectfinal.feature_auth.ui.*
import com.example.damprojectfinal.core.repository.AuthRepository
import com.example.damprojectfinal.feature_auth.viewmodels.*
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.retro.RetrofitClient
import com.example.damprojectfinal.core.retro.RetrofitClient as PostsRetrofitClient
import com.example.damprojectfinal.core.api.DebugUserLogger
import com.example.damprojectfinal.core.api.UserApiService
import com.example.damprojectfinal.core.repository.MenuItemRepository
import com.example.damprojectfinal.core.repository.UserRepository
import com.example.damprojectfinal.core.repository.OrderRepository
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import com.example.damprojectfinal.user.feautre_order.ui.OrderHistoryScreen
import com.example.damprojectfinal.user.feautre_order.ui.OrderDetailsScreen
import com.example.damprojectfinal.core.utils.LogoutViewModelFactory
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel
import com.example.damprojectfinal.feature_auth.ui.LoginScreen
import com.example.damprojectfinal.feature_auth.ui.SignupScreen
import com.example.damprojectfinal.feature_auth.ui.SplashScreen
import com.example.damprojectfinal.feature_auth.ui.ProSignupScreen
import com.example.damprojectfinal.user.feature_profile.ui.ProfileScreen
import com.example.damprojectfinal.user.feature_profile.ui.AllProfilePosts
import com.example.damprojectfinal.user.feature_profile.ui.AllSavedPosts
import com.example.damprojectfinal.user.feature_profile.ui.UpdateProfileScreen
import com.example.damprojectfinal.user.feature_profile.ui.ProfileSettingsScreen
import com.example.damprojectfinal.user.feature_profile.ui.ChangePasswordScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.common._component.ProfessionalMenuScreen
import com.example.damprojectfinal.professional.feature_posts.CreateContentScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileManagementScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalEmailNameUpdateScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalChangePasswordScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileSettingsScreen
import com.example.damprojectfinal.professional.feature_profile.viewmodel.ProfessionalProfileViewModel
import com.example.damprojectfinal.professional.feature_profile.ui.AllProfilePosts
import com.example.damprojectfinal.user.feature_chat.ui.ChatDetailScreen
import com.example.damprojectfinal.professional.feature_menu.ui.MenuItemManagementScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.CreateMenuItemScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.ItemDetailsScreen
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.core.utils.ForgotPasswordViewModelFactory
import com.example.damprojectfinal.core.utils.ResetPasswordViewModelFactory
import com.example.damprojectfinal.core.utils.VerifyOtpViewModelFactory
import com.example.damprojectfinal.feature_deals.AddEditDealScreen
import com.example.damprojectfinal.feature_deals.DealsViewModel
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModel
import com.example.damprojectfinal.feature_relamation.ReclamationsRestaurantViewModelFactory
import com.example.damprojectfinal.feature_relamation.ReclamationViewModelFactory
import com.example.damprojectfinal.professional.feature_relamation.ReclamationDetailRestaurantScreen
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.user.common._component.UserMenuScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.CreatePostScreen
import com.example.damprojectfinal.user.feature_notifications.ui.NotificationsScreen
import com.example.damprojectfinal.professional.feature_notifications.ui.ProNotificationsScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.CaptionAndPublishScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.EditPostScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostDetailsScreen
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.ReelsScreen
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.CommentScreen
import com.example.damprojectfinal.user.feature_posts.ui.trends.TrendsScreen
import com.example.damprojectfinal.professional.feature_deals.ProDealsManagementScreen
import com.example.damprojectfinal.professional.feature_event.EventDetailScreen
import com.example.damprojectfinal.user.feature_deals.DealDetailScreen
import com.example.damprojectfinal.user.feature_deals.DealsListScreen
import com.example.damprojectfinal.user.feature_chat.ui.ChatManagementScreen
import com.example.damprojectfinal.user.feature_pro_profile.ui.RestaurantProfileView
import com.example.damprojectfinal.user.feature_pro_profile.ui.RestaurantProfileViewScreen
import com.example.damprojectfinal.user.feature_profile.ui.UserViewModel
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.user.feature_cart_item.ui.ShoppingCartScreen
import com.example.damprojectfinal.user.feature_menu.ui.RestaurantMenuScreen
import com.example.damprojectfinal.user.feautre_order.ui.OrderConfirmationScreen
import com.example.damprojectfinal.core.repository.CartRepository
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModel
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModelFactory
import com.google.gson.Gson
import com.example.damprojectfinal.user.feature_relamation.ReclamationTemplateScreen
import com.example.damprojectfinal.professional.feature_event.CreateEventScreen
import com.example.damprojectfinal.professional.feature_event.EventListScreenRemote
import com.example.damprojectfinal.user.feature_event.EventListScreen
import com.example.damprojectfinal.feature_event.EventViewModel
import com.example.damprojectfinal.feature_event.EventStatus
import com.example.damprojectfinal.core.api.EventRetrofitClient
import com.example.damprojectfinal.feature_relamation.ReclamationDetailScreen
import com.example.damprojectfinal.core.repository.ReclamationRepository
import com.example.damprojectfinal.feature_relamation.ReclamationViewModel
import com.example.damprojectfinal.professional.feature_event.EditEventScreen
import com.example.damprojectfinal.professional.feature_relamation.ReclamationListRestaurantScreen
import com.example.damprojectfinal.user.feature_relamation.ReclamationListUserScreen
import com.example.damprojectfinal.feature_relamation.LoyaltyPointsScreen
import com.example.damprojectfinal.feature_relamation.LoyaltyData
import com.example.damprojectfinal.feature_relamation.Reward
import com.example.damprojectfinal.feature_relamation.PointsTransaction
import com.example.damprojectfinal.core.api.ReclamationRetrofitClient
import com.example.damprojectfinal.feature_event.Event
import com.example.damprojectfinal.user.feature_chat.ui.ProChatManagementScreen

object AuthRoutes {
    const val SPLASH = "splash_route"
    const val LOGIN = "login_route"
    const val SIGNUP = "signup_route"
    const val VERIFY_OTP = "verify_otp_route"
    const val RESET_PASSWORD = "reset_password_route"
    const val FORGET_PASSWORD = "forget_password_route"
    const val PRO_SIGNUP = "pro_signup_route" // Added Pro Signup route
}

/**
 * Routes for authenticated user flow
 */
object UserRoutes {
    const val HOME_SCREEN = "home_screen"
    const val HOME_SCREEN_PRO = "home_screen_pro" // professional dashboard
    const val CREATE_POST = "create_post_route"
    const val CAPTION_PUBLISH_SCREEN = "caption_publish_route"
    const val MEDIA_URI_ARG = "mediaUri"
    const val REELS_SCREEN = "reels_screen"
    const val EDIT_POST_SCREEN = "edit_post_screen/{postId}/{initialCaption}"
    const val POST_DETAILS_SCREEN = "post_details_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val ALL_PROFILE_POSTS = "all_profile_posts"
    const val COMMENT_SCREEN = "comment_screen"
    const val ALL_SAVED_POSTS = "all_saved_posts"
    const val TRENDS_SCREEN = "trends_screen"
    // Base route for navigation must match the NavHost setup, including argument placeholders
    const val PROFILE_VIEW = "profile_view/{userId}"
    const val PROFILE_UPDATE = "profile_update/{userId}"
    const val PROFILE_SETTINGS = "profile_settings"
    const val CHANGE_PASSWORD = "change_password/{userId}"
    const val ORDERS_SCREEN = "orders"
    const val ORDERS_ROUTE = "orders_history_route"
    const val NOTIFICATIONS_SCREEN = "notifications_screen"
}

object ProRoutes {
    const val MENU_MANAGEMENT = "menu_management/{professionalId}"
    const val CART_ROUTE = "shopping_cart_route"
    const val CREATE_CONTENT_SCREEN = "create_content_screen"
    const val PROFESSIONAL_PROFILE_SCREEN = "professional_profile_screen/{professionalId}"
    const val PROFESSIONAL_PROFILE_MANAGEMENT = "professional_profile_management/{professionalId}"
    const val PROFESSIONAL_PROFILE_SETTINGS = "professional_profile_settings/{professionalId}"
    const val PROFESSIONAL_EMAIL_NAME_UPDATE = "professional_email_name_update/{professionalId}"
    const val PROFESSIONAL_CHANGE_PASSWORD = "professional_change_password/{professionalId}"
    const val ALL_PROFILE_POSTS = "all_profile_posts"
    const val NOTIFICATIONS_SCREEN = "pro_notifications_screen"
}

object ProfileRoutes {
    const val PROFESSIONAL_PROFILE_EDIT = "pro_profile_edit/{professionalId}"
    const val CLIENT_PROFILE_VIEW = "client_profile_view/{professionalId}"
    const val HOME_SCREEN_PRO = "home_screen_pro"
}



@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    initialDeepLinkToken: String? = null
) {
    val navController = rememberNavController()
    val authApiService = AuthApiService()
    val authRepository = AuthRepository(authApiService)

    val dealsViewModel: DealsViewModel = viewModel()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    // Initialize User API Service and Repository for ProfileViewModel
    val userApiService = remember { UserApiService(tokenManager) }
    val userRepository = remember { UserRepository(userApiService) }

    // Order Repository
    val orderApiService = remember { RetrofitClient.orderApi }
    val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }

    // Debugger runs at the highest level
    DebugUserLogger(tokenManager = tokenManager)

    val ServiceLocator = KtorClient
    val postsApiService = remember { PostsRetrofitClient.postsApiService }
    val startDestination = AuthRoutes.SPLASH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // 1️⃣ Splash Screen
        // 1️⃣ Splash Screen (kept for potential future use, but not used as start destination)
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

        // 2️⃣ Login Screen
        composable(AuthRoutes.LOGIN) {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)

            LoginScreen(
                navController = navController,
                authApiService = authApiService,
                tokenManager = tokenManager,
                onNavigateToSignup = { navController.navigate(AuthRoutes.SIGNUP) },
                onNavigateToForgetPassword = { navController.navigate(AuthRoutes.FORGET_PASSWORD) }
            )
        }

        // 3️⃣ Signup Screen
        composable(AuthRoutes.SIGNUP) {
            SignupScreen(
                navController = navController,
                onNavigateToLogin = { navController.navigate(AuthRoutes.LOGIN) }
            )
        }

        // 4️⃣ Forgot Password
        composable(AuthRoutes.FORGET_PASSWORD) {
            val vm: ForgotPasswordViewModel = viewModel(
                factory = ForgotPasswordViewModelFactory(authRepository)
            )
            ForgotPasswordScreen(navController = navController, viewModel = vm)
        }

        // 5️⃣ Verify OTP
        composable(
            route = "${AuthRoutes.VERIFY_OTP}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val vm: VerifyOtpViewModel = viewModel(
                factory = VerifyOtpViewModelFactory(authRepository)
            )
            VerifyOtpScreen(email = email, navController = navController, viewModel = vm)
        }

        // 6️⃣ Reset Password
        composable(
            route = "${AuthRoutes.RESET_PASSWORD}/{email}/{resetToken}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("resetToken") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
            val vm: ResetPasswordViewModel = viewModel(
                factory = ResetPasswordViewModelFactory(authRepository)
            )
            ResetPasswordScreen(
                email = email,
                resetToken = resetToken,
                navController = navController,
                viewModel = vm
            )
        }

        composable(
            route = UserRoutes.PROFILE_VIEW,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for profile view.")

            // --- Setup ViewModel ---
            val context = LocalContext.current

            // Fix 1: Ensure Factory uses the correct constructor
            val userViewModel: UserViewModel = viewModel()


            ProfileScreen(
                navController = navController,
                viewModel = userViewModel
            )
        }

        // Profile Update Screen
        composable(
            route = UserRoutes.PROFILE_UPDATE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for profile update.")

            val context = LocalContext.current
            val userRepository = UserRepository(UserApiService(TokenManager(context)))
            val profileViewModel: com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel = viewModel(
                factory = com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel.Factory(userRepository, context)
            )

            // Fetch user profile on first load
            LaunchedEffect(Unit) {
                profileViewModel.fetchUserProfile()
            }

            // Get UserViewModel to refresh profile when navigating back
            val userViewModel: UserViewModel = viewModel()

            UpdateProfileScreen(
                viewModel = profileViewModel,
                onBackClick = {
                    // Refresh profile when navigating back
                    userViewModel.refreshProfile()
                    navController.popBackStack()
                }
            )
        }

        // Profile Settings Screen (Hub)
        composable(UserRoutes.PROFILE_SETTINGS) {
            val context = LocalContext.current
            val userRepository = UserRepository(UserApiService(TokenManager(context)))
            val profileViewModel: com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel = viewModel(
                factory = com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel.Factory(userRepository, context)
            )

            ProfileSettingsScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }

        // Change Password Screen
        composable(
            route = UserRoutes.CHANGE_PASSWORD,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for password change.")

            val context = LocalContext.current
            val userRepository = UserRepository(UserApiService(TokenManager(context)))
            val profileViewModel: com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel = viewModel(
                factory = com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel.Factory(userRepository, context)
            )

            // Fetch user profile on first load
            LaunchedEffect(Unit) {
                profileViewModel.fetchUserProfile()
            }

            ChangePasswordScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // User Menu Screen (Full Screen Menu)
        composable("user_menu") {
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            var loyaltyPoints by remember { mutableStateOf<Int?>(null) }

            // Load loyalty points
            LaunchedEffect(Unit) {
                try {
                    val token = tokenManager.getAccessTokenAsync()
                    if (!token.isNullOrEmpty()) {
                        val api = com.example.damprojectfinal.core.api.ReclamationRetrofitClient.createClient(token)
                        val balance = api.getUserLoyalty()
                        loyaltyPoints = balance?.loyaltyPoints
                    }
                } catch (e: Exception) {
                    Log.e("UserMenuScreen", "Error loading loyalty points: ${e.message}")
                }
            }

            // Initialize Logout ViewModel
            val logoutViewModel: LogoutViewModel = viewModel(
                factory = LogoutViewModelFactory(
                    authApiService = AuthApiService(),
                    tokenManager = tokenManager
                )
            )

            UserMenuScreen(
                navController = navController,
                onLogout = {
                    logoutViewModel.logout()
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                loyaltyPoints = loyaltyPoints
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

        // Reels Screen
        composable(UserRoutes.REELS_SCREEN) {
            ReelsScreen(navController = navController)
        }

        // 6️⃣ PROFESSIONAL HOME SCREEN (Corrected - Removed duplicate "Text" composable)
        // This is the correct composable for the professional's home screen
        composable("${UserRoutes.HOME_SCREEN_PRO}/{professionalId}") { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            HomeScreenPro(
                professionalId = professionalId,
                navController = navController,
                onLogout = {
                    println("Logout triggered from HomeScreenPro (not directly used by UI in this version)")
                    // navController.navigate(AuthRoutes.LOGIN) { // Example of actual logout navigation
                    //     popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    // }
                }
            )
        }

        // Chat management (copied from other project, minimal integration)
        // Notifications Screen (User)
        composable(UserRoutes.NOTIFICATIONS_SCREEN) {
            NotificationsScreen(navController = navController)
        }
        
        // Notifications Screen (Professional)
        composable(ProRoutes.NOTIFICATIONS_SCREEN) {
            ProNotificationsScreen(navController = navController)
        }

        // User chat screen
        composable("chatList") {
            ChatManagementScreen(navController = navController)
        }

        // Professional chat screen (uses pro-specific UI)
        composable("chatListPro") {
            ProChatManagementScreen(navController = navController)
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

        // 🆕 Create Content Screen (for professional users to choose what to create)
        composable(ProRoutes.CREATE_CONTENT_SCREEN) {
            CreateContentScreen(navController = navController)
        }

        // 🆕 All Profile Posts Screen (for professional users to view all their posts)
        composable("${ProRoutes.ALL_PROFILE_POSTS}/{professionalId}") { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: ""
            AllProfilePosts(
                navController = navController,
                professionalId = professionalId
            )
        }

        // 🆕 Add Post Screen (Your existing media selection screen)
        composable(UserRoutes.CREATE_POST) {
            CreatePostScreen(navController = navController)
        }

        // 🆕 Caption and Publish Screen (Your existing captioning/publishing screen)
        composable(
            route = "${UserRoutes.CAPTION_PUBLISH_SCREEN}/{${UserRoutes.MEDIA_URI_ARG}}",
            arguments = listOf(
                navArgument(UserRoutes.MEDIA_URI_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val mediaUriString = backStackEntry.arguments?.getString(UserRoutes.MEDIA_URI_ARG)
            CaptionAndPublishScreen(navController = navController, mediaUriString = mediaUriString)
        }

        // 🆕 Edit Post Composable
        composable(
            route = UserRoutes.EDIT_POST_SCREEN,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("initialCaption") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val initialCaption = backStackEntry.arguments?.getString("initialCaption") ?: ""
            EditPostScreen(
                navController = navController,
                postId = postId,
                initialCaption = initialCaption
            )
        }

        // 🆕 Post Details Composable
        composable(
            route = "${UserRoutes.POST_DETAILS_SCREEN}/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailsScreen(
                navController = navController,
                postId = postId
            )
        }


        // Professional Signup
        composable(AuthRoutes.PRO_SIGNUP) {
            ProSignupScreen(navController = navController, authApiService = authApiService)
        }

        // Professional Home
        composable("${UserRoutes.HOME_SCREEN_PRO}/{professionalId}") { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"

            val context = LocalContext.current

            // ⭐ 1. Initialize Logout ViewModel
            val logoutViewModel: LogoutViewModel = viewModel(
                factory = LogoutViewModelFactory(
                    authApiService = AuthApiService(),
                    tokenManager = TokenManager(context)
                )
            )

            HomeScreenPro(
                professionalId = professionalId,
                navController = navController,

                // ⭐ 2. Pass the onLogout callback to HomeScreenPro
                onLogout = {
                    // Perform logout logic (clear tokens etc.)
                    logoutViewModel.logout()

                    // Navigate to Login and clear the whole back stack
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Professional Menu Screen (Full Screen Menu)
        composable(
            route = "professional_menu/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            val context = LocalContext.current

            // Initialize Logout ViewModel
            val logoutViewModel: LogoutViewModel = viewModel(
                factory = LogoutViewModelFactory(
                    authApiService = AuthApiService(),
                    tokenManager = TokenManager(context)
                )
            )

            ProfessionalMenuScreen(
                navController = navController,
                professionalId = professionalId,
                onLogout = {
                    logoutViewModel.logout()
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
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

        // 🆕 All Profile Posts Screen (for normal users to view all their posts)
        composable("${UserRoutes.ALL_PROFILE_POSTS}/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AllProfilePosts(
                navController = navController,
                userId = userId
            )
        }

        // 🆕 Comment Screen (for viewing and adding comments on posts/reels)
        composable("${UserRoutes.COMMENT_SCREEN}/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentScreen(
                navController = navController,
                postId = postId
            )
        }

        // 🆕 All Saved Posts Screen (for viewing saved posts)
        composable("${UserRoutes.ALL_SAVED_POSTS}/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AllSavedPosts(
                navController = navController,
                userId = userId
            )
        }

        composable(UserRoutes.TRENDS_SCREEN) {
            TrendsScreen(navController = navController)
        }

        composable(UserRoutes.PROFILE_SCREEN) {
            ProfileScreen(navController = navController)
        }

        composable(ProRoutes.PROFESSIONAL_PROFILE_SCREEN) {
            ProfessionalProfileScreen(
                navController = navController,
                viewModel = viewModel(
                    factory = ProfessionalProfileViewModel.Factory(
                        tokenManager = TokenManager(LocalContext.current),
                        professionalApiService = PostsRetrofitClient.professionalApiService,
                        postsApiService = PostsRetrofitClient.postsApiService
                    )
                )
            )
        }

        // Restaurant Profile View (Client-side view of professional)
        composable(
            route = "restaurant_profile_view/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: ""
            RestaurantProfileViewScreen(
                professionalId = professionalId,
                navController = navController
            )
        }

        // 🔟 Liste réclamations (CLIENT)
        composable("list_reclamation_route") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )
            val reclamations by vm.reclamations.collectAsState()
            val error by vm.errorMessage.collectAsState()

            LaunchedEffect(Unit) { vm.loadReclamations() }

            ReclamationListUserScreen(
                reclamations = reclamations,
                onReclamationClick = fun(reclamation) {
                    val reclamationId = reclamation.id ?: return
                    navController.navigate("reclamation_detail/$reclamationId")
                },
                onBackClick = { navController.popBackStack() }
            )
            error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        }

        // 💰 Points de Fidélité
        composable("loyalty_points_route") {
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            var loyaltyData: LoyaltyData? by remember { mutableStateOf(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage: String? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                isLoading = true
                errorMessage = null
                try {
                    val token = tokenManager.getAccessTokenAsync()
                    if (!token.isNullOrEmpty()) {
                        val api = ReclamationRetrofitClient.createClient(token)
                        val balance = api.getUserLoyalty()

                        loyaltyData = balance?.let { bal ->
                            val rewards = bal.availableRewards.orEmpty().map { reward ->
                                com.example.damprojectfinal.feature_relamation.Reward(
                                    name = reward.name,
                                    pointsCost = reward.pointsCost,
                                    available = reward.available
                                )
                            }
                            val history = bal.history.orEmpty().map { entry ->
                                com.example.damprojectfinal.feature_relamation.PointsTransaction(
                                    points = entry.points,
                                    reason = entry.reason,
                                    date = entry.date,
                                    reclamationId = entry.reclamationId
                                )
                            }

                            LoyaltyData(
                                loyaltyPoints = bal.loyaltyPoints,
                                validReclamations = bal.validReclamations,
                                invalidReclamations = bal.invalidReclamations,
                                reliabilityScore = bal.reliabilityScore,
                                availableRewards = rewards,
                                history = history
                            )
                        }

                        if (loyaltyData == null) {
                            errorMessage = "Aucune donnée disponible"
                        }
                    } else {
                        errorMessage = "Token non disponible"
                    }
                } catch (e: Exception) {
                    Log.e("LoyaltyRoute", "❌ Erreur: ${e.message}", e)
                    errorMessage = "Erreur de chargement: ${e.message}"
                } finally {
                    isLoading = false
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "Erreur inconnue",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Retour")
                            }
                        }
                    }
                }
                else -> {
                    LoyaltyPointsScreen(
                        loyaltyData = loyaltyData,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // 1️⃣1️⃣ Détail réclamation (CLIENT)
        composable(
            route = "reclamation_detail/{reclamationId}",
            arguments = listOf(navArgument("reclamationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId") ?: return@composable
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )

            LaunchedEffect(reclamationId) { vm.loadReclamationById(reclamationId) }
            val selectedReclamation by vm.selectedReclamation.collectAsState()

            if (selectedReclamation != null) {
                ReclamationDetailScreen(
                    reclamation = selectedReclamation!!,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize())
            }
        }

        // 1️⃣2️⃣ Créer réclamation (CLIENT) - avec orderId
        composable(
            route = "create_reclamation/{orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )

            // Log de l'orderId reçu
            Log.d("ReclamationNav", "════════════════════════════════")
            Log.d("ReclamationNav", "📥 OrderId reçu depuis navigation: $orderId")
            Log.d("ReclamationNav", "════════════════════════════════")

            // Charger les commandes de l'utilisateur
            val orders by vm.orders.collectAsState()

            // Log des commandes chargées
            LaunchedEffect(orders.size) {
                Log.d("ReclamationNav", "📋 Commandes chargées: ${orders.size}")
                orders.take(3).forEach { order ->
                    Log.d("ReclamationNav", "   - Commande ID: ${order.id}")
                }
            }

            // Formater les commandes pour l'affichage avec l'ID complet ou les 8 derniers caractères
            val commandeconcernees = remember(orders, orderId) {
                val baseList = orders.map { order ->
                    val orderIdDisplay = if (order.id.length >= 8) order.id.takeLast(8) else order.id
                    "Commande #$orderIdDisplay"
                }

                // Si un orderId est fourni, l'ajouter en premier dans la liste avec le format "Commande #xxxxx"
                if (orderId != null) {
                    val orderIdSuffix = if (orderId.length >= 8) orderId.takeLast(8) else orderId
                    val orderFormat = "Commande #$orderIdSuffix"
                    // Vérifier si la commande existe déjà dans la liste
                    val orderExists = baseList.any {
                        it == orderFormat ||
                        it.endsWith(orderIdSuffix) ||
                        orders.any { o -> o.id == orderId || o.id.endsWith(orderIdSuffix) }
                    }
                    if (!orderExists) {
                        // Ajouter la commande en premier dans la liste
                        listOf(orderFormat) + baseList
                    } else {
                        // Si elle existe, la mettre en premier
                        listOf(orderFormat) + baseList.filter {
                            it != orderFormat && !it.endsWith(orderIdSuffix)
                        }
                    }
                } else {
                    baseList
                }
            }

            // Observer les erreurs du ViewModel
            val error by vm.errorMessage.collectAsState()
            LaunchedEffect(error) {
                error?.let { errorMsg ->
                    Log.e("ReclamationNav", "❌ Erreur lors de la création: $errorMsg")
                    Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            ReclamationTemplateScreen(
                complaintTypes = listOf("Livraison en retard", "Article manquant", "Problème de qualité", "Autre"),
                commandeconcernees = commandeconcernees,
                initialOrderId = orderId,
                onSubmit = { commandeConcernee, complaintType, description, photos ->
                    // Extraire l'ID de la commande depuis le format "Commande #xxxxx"
                    val selectedOrderId = if (commandeConcernee.startsWith("Commande #")) {
                        val orderIdSuffix = commandeConcernee.substringAfter("#").trim()
                        // Priorité 1: Utiliser l'orderId initial s'il existe (le plus fiable)
                        if (orderId != null) {
                            Log.d("ReclamationNav", "✅ Utilisation de l'orderId initial: $orderId")
                            orderId
                        } else {
                            // Priorité 2: Chercher dans les commandes chargées par suffixe
                            val foundOrder = orders.find { order ->
                                val orderSuffix = if (order.id.length >= 8) order.id.takeLast(8) else order.id
                                orderSuffix == orderIdSuffix || order.id.endsWith(orderIdSuffix)
                            }
                            if (foundOrder != null) {
                                Log.d("ReclamationNav", "✅ Commande trouvée dans la liste: ${foundOrder.id}")
                                foundOrder.id
                            } else {
                                // Priorité 3: Utiliser le suffixe directement (peut fonctionner si c'est l'ID complet)
                                Log.d("ReclamationNav", "⚠️ Utilisation du suffixe directement: $orderIdSuffix")
                                orderIdSuffix
                            }
                        }
                    } else {
                        // Si ce n'est pas au format "Commande #", utiliser directement ou l'orderId initial
                        if (commandeConcernee.isNotBlank()) {
                            commandeConcernee
                        } else {
                            orderId ?: ""
                        }
                    }

                    // Validation: s'assurer qu'on a un ID valide
                    if (selectedOrderId.isNotBlank()) {
                        Log.d("ReclamationNav", "════════════════════════════════")
                        Log.d("ReclamationNav", "📝 Données de la réclamation:")
                        Log.d("ReclamationNav", "   Commande sélectionnée (affichage): $commandeConcernee")
                        Log.d("ReclamationNav", "   Commande ID (envoyé): $selectedOrderId")
                        Log.d("ReclamationNav", "   Type: $complaintType")
                        Log.d("ReclamationNav", "   Description: ${description.take(50)}...")
                        Log.d("ReclamationNav", "   Photos: ${photos.size}")
                        Log.d("ReclamationNav", "════════════════════════════════")

                        vm.createReclamation(
                            commandeConcernee = selectedOrderId,
                            complaintType = complaintType,
                            description = description,
                            photoUris = photos
                        ) { reclamation ->
                            Log.d("ReclamationNav", "✅ Réclamation créée avec succès: ${reclamation.id}")
                            Toast.makeText(context, "Réclamation créée avec succès !", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    } else {
                        Log.e("ReclamationNav", "❌ Erreur: Aucun ID de commande valide")
                        Toast.makeText(context, "Erreur: Veuillez sélectionner une commande valide", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // 1️⃣2️⃣-bis Créer réclamation (CLIENT) - sans orderId
        composable("create_reclamation") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val userApiService = UserApiService(tokenManager)
            val vm: ReclamationViewModel = viewModel(
                factory = ReclamationViewModelFactory(userApiService, tokenManager, context)
            )

            // Charger les commandes de l'utilisateur
            val orders by vm.orders.collectAsState()

            // Observer les erreurs du ViewModel
            val error by vm.errorMessage.collectAsState()
            LaunchedEffect(error) {
                error?.let { errorMsg ->
                    Log.e("ReclamationNav", "❌ Erreur lors de la création: $errorMsg")
                    Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            // Formater les commandes pour l'affichage
            val commandeconcernees = orders.map { "Commande #${it.id.takeLast(8)}" }

            ReclamationTemplateScreen(
                complaintTypes = listOf("Livraison en retard", "Article manquant", "Problème de qualité", "Autre"),
                commandeconcernees = commandeconcernees,
                initialOrderId = null,
                onSubmit = { commandeConcernee, complaintType, description, photos ->
                    // Extraire l'ID de la commande depuis le format "Commande #xxxxx"
                    val selectedOrderId = if (commandeConcernee.startsWith("Commande #")) {
                        val orderIdSuffix = commandeConcernee.substringAfter("#")
                        orders.find { it.id.endsWith(orderIdSuffix) }?.id ?: commandeConcernee
                    } else {
                        commandeConcernee
                    }

                    vm.createReclamation(
                        commandeConcernee = selectedOrderId,
                        complaintType = complaintType,
                        description = description,
                        photoUris = photos
                    ) { reclamation ->
                        Toast.makeText(context, "Réclamation créée avec succès !", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }
            )
        }

        // 1️⃣3️⃣ Liste événements
        composable("event_list") {
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val isLoading by eventViewModel.isLoading.collectAsState()
            val error by eventViewModel.error.collectAsState()

            EventListScreen(
                events = events,
                onEventClick = { event -> navController.navigate("event_detail/${event._id}") },
                onAddEventClick = {},
                onBackClick = { navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = error
            )
        }

        // 1️⃣4️⃣ Détail événement
        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val context = LocalContext.current
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val isLoading by eventViewModel.isLoading.collectAsState()
            
            var selectedEvent by remember { mutableStateOf<Event?>(null) }
            var isLoadingEvent by remember { mutableStateOf(false) }
            
            // Charger les événements si la liste est vide
            LaunchedEffect(Unit) {
                if (events.isEmpty()) {
                    eventViewModel.loadEvents()
                }
            }
            
            // Chercher l'événement dans la liste ou le charger depuis l'API
            LaunchedEffect(eventId, events, isLoading) {
                val foundEvent = events.find { it._id == eventId }
                if (foundEvent != null) {
                    selectedEvent = foundEvent
                } else if (!isLoading && events.isNotEmpty() && selectedEvent == null && !isLoadingEvent) {
                    // Si l'événement n'est pas dans la liste, le charger depuis l'API
                    isLoadingEvent = true
                    try {
                        Log.d("AppNavigation", "🔍 Chargement de l'événement $eventId depuis l'API pour les détails")
                        val event = EventRetrofitClient.api.getEventById(eventId)
                        selectedEvent = event
                        Log.d("AppNavigation", "✅ Événement chargé depuis l'API: ${event.nom}")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "❌ Erreur lors du chargement de l'événement: ${e.message}")
                        Toast.makeText(context, "Erreur lors du chargement de l'événement: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoadingEvent = false
                    }
                }
            }

            when {
                isLoadingEvent || (isLoading && selectedEvent == null) -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                selectedEvent != null -> {
                    EventDetailScreen(
                        event = selectedEvent!!,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                else -> {
                    if (!isLoading && !isLoadingEvent) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Événement introuvable",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "L'événement avec l'ID $eventId n'a pas pu être chargé.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Retour")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1️⃣5️⃣ Créer événement
        composable("create_event") {
            val context = LocalContext.current
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val error by eventViewModel.error.collectAsState()
            
            // Initialiser avec la taille actuelle pour éviter les déclenchements intempestifs
            var previousEventsCount by remember { 
                mutableStateOf(events.size) 
            }
            var hasSubmitted by remember { mutableStateOf(false) }

            // Observe the ViewModel state for success/error - seulement après soumission
            LaunchedEffect(error, events.size) {
                // Ne naviguer que si on a soumis un formulaire ET qu'un nouvel événement a été créé
                if (hasSubmitted && error == null && events.size > previousEventsCount) {
                    Toast.makeText(context, "Événement créé avec succès!", Toast.LENGTH_SHORT).show()
                    navController.navigate("event_list_remote") {
                        popUpTo("event_list_remote") { inclusive = false }
                    }
                    hasSubmitted = false
                } else if (hasSubmitted && error != null) {
                    Toast.makeText(context, "Erreur: $error", Toast.LENGTH_LONG).show()
                    hasSubmitted = false
                }
                previousEventsCount = events.size
            }

            val scope = rememberCoroutineScope()
            
            CreateEventScreen(
                navController = navController,
                onSubmit = { nom, description, dateDebut, dateFin, image, lieu, categorie, statut ->
                    // Marquer qu'on a soumis le formulaire
                    hasSubmitted = true
                    previousEventsCount = events.size
                    
                    // Convertir l'image de manière asynchrone
                    scope.launch {
                        // Convertir l'URI local en Base64 si nécessaire
                        val validImage = if (image != null) {
                            if (image.startsWith("http")) {
                                // C'est déjà une URL, on l'utilise telle quelle
                                Log.d("AppNavigation", "✅ Image URL: $image")
                                image
                            } else {
                                // C'est un URI local, on le convertit en Base64
                                try {
                                    Log.d("AppNavigation", "🖼️ Conversion URI en Base64: $image")
                                    val uri = Uri.parse(image)
                                    val base64Image = uriToBase64(context, uri)
                                    if (base64Image != null) {
                                        val imageLength = base64Image.length
                                        Log.d("AppNavigation", "✅ Image convertie en Base64 ($imageLength caractères)")
                                    } else {
                                        Log.e("AppNavigation", "❌ Échec de la conversion Base64")
                                        Toast.makeText(context, "Erreur lors de la conversion de l'image", Toast.LENGTH_SHORT).show()
                                    }
                                    base64Image
                                } catch (e: Exception) {
                                    Log.e("AppNavigation", "❌ Erreur conversion image: ${e.message}", e)
                                    Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                                    null
                                }
                            }
                        } else {
                            Log.d("AppNavigation", "ℹ️ Aucune image fournie")
                            null
                        }

                        Log.d("AppNavigation", "🚀 Création événement avec image: ${validImage != null}")
                        eventViewModel.createEvent(
                            nom,
                            description,
                            dateDebut,
                            dateFin,
                            validImage,
                            lieu,
                            categorie,
                            statut
                        )
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 1️⃣5️⃣ Liste événements (PROFESSIONAL)
        composable("event_list_remote") {
            EventListScreenRemote(navController = navController)
        }

// 🆕 Route pour l'édition d'un événement - ✅ UTILISE le ViewModel partagé
        composable(
            route = "edit_event/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable

            val context = LocalContext.current

            // Essayer de récupérer le ViewModel du parent si possible, sinon créer un nouveau
            val parentEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry("event_list_remote")
                } catch (e: Exception) {
                    null
                }
            }

            val eventViewModel: EventViewModel = if (parentEntry != null) {
                viewModel(parentEntry)
            } else {
                Log.d("AppNavigation", "⚠️ Parent entry non trouvé, création d'un nouveau ViewModel")
                viewModel()
            }

            val events by eventViewModel.events.collectAsState()
            val error by eventViewModel.error.collectAsState()
            val isLoading by eventViewModel.isLoading.collectAsState()

            // Charger les événements si nécessaire
            LaunchedEffect(Unit) {
                eventViewModel.loadEvents()
            }

            // Trouver ou charger l'événement à éditer
            var selectedEvent by remember { mutableStateOf<Event?>(null) }
            var isLoadingEvent by remember { mutableStateOf(false) }
            var hasInitiatedUpdate by remember { mutableStateOf(false) }
            var initialEventState by remember { mutableStateOf<Event?>(null) }
            var updateStartTime by remember { mutableStateOf<Long?>(null) }
            
            LaunchedEffect(eventId, events, isLoading) {
                // D'abord chercher dans la liste chargée
                val foundEvent = events.find { it._id == eventId }
                if (foundEvent != null) {
                    selectedEvent = foundEvent
                    // Sauvegarder l'état initial de l'événement pour détecter les changements
                    if (initialEventState == null) {
                        initialEventState = foundEvent
                    }
                    Log.d("AppNavigation", "✅ Événement trouvé dans la liste: ${foundEvent.nom}")
                } else if (!isLoading && events.isNotEmpty() && selectedEvent == null && !isLoadingEvent) {
                    // Si l'événement n'est pas dans la liste, le charger depuis l'API
                    isLoadingEvent = true
                    try {
                        Log.d("AppNavigation", "🔍 Chargement de l'événement $eventId depuis l'API")
                        val event = EventRetrofitClient.api.getEventById(eventId)
                        selectedEvent = event
                        initialEventState = event
                        Log.d("AppNavigation", "✅ Événement chargé depuis l'API: ${event.nom}")
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "❌ Erreur lors du chargement de l'événement: ${e.message}")
                        Toast.makeText(context, "Erreur lors du chargement de l'événement: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoadingEvent = false
                    }
                }
            }
            
            // Observer l'événement mis à jour dans la liste
            var lastObservedEvent by remember { mutableStateOf<Event?>(null) }
            
            // Initialiser lastObservedEvent avec l'événement actuel
            LaunchedEffect(selectedEvent) {
                selectedEvent?.let {
                    if (lastObservedEvent == null) {
                        lastObservedEvent = it
                        initialEventState = it
                    }
                }
            }
            
            // Observer les changements dans la liste d'événements pour détecter la mise à jour
            LaunchedEffect(events) {
                if (hasInitiatedUpdate && updateStartTime != null) {
                    val currentEvent = events.find { it._id == eventId }
                    val previousEvent = lastObservedEvent
                    
                    if (currentEvent != null && previousEvent != null) {
                        // Comparer les champs pour détecter un changement réel
                        val hasChanged = currentEvent.nom != previousEvent.nom ||
                                currentEvent.description != previousEvent.description ||
                                currentEvent.date_debut != previousEvent.date_debut ||
                                currentEvent.date_fin != previousEvent.date_fin ||
                                currentEvent.lieu != previousEvent.lieu ||
                                currentEvent.categorie != previousEvent.categorie ||
                                currentEvent.statut != previousEvent.statut
                        
                        if (hasChanged) {
                            Log.d("AppNavigation", "✅ Mise à jour réussie - changement détecté dans la liste")
                            Log.d("AppNavigation", "   Nom avant: ${previousEvent.nom}, après: ${currentEvent.nom}")
                            Toast.makeText(context, "Événement mis à jour avec succès!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                            hasInitiatedUpdate = false
                            updateStartTime = null
                            lastObservedEvent = currentEvent
                        }
                    }
                }
            }
            
            // Observe error state for update success/error - seulement si une mise à jour a été initiée
            LaunchedEffect(hasInitiatedUpdate, error, updateStartTime) {
                if (!hasInitiatedUpdate || updateStartTime == null) {
                    return@LaunchedEffect
                }
                
                // Si une erreur survient
                if (error != null) {
                    Log.e("AppNavigation", "❌ Échec de la mise à jour: $error")
                    Toast.makeText(context, "Erreur: $error", Toast.LENGTH_LONG).show()
                    hasInitiatedUpdate = false
                    updateStartTime = null
                    return@LaunchedEffect
                }
                
                // Attendre un peu pour que la liste soit mise à jour par le ViewModel
                kotlinx.coroutines.delay(1000)
                
                // Si pas d'erreur après le délai, considérer comme succès
                if (error == null && hasInitiatedUpdate) {
                    val currentEvent = events.find { it._id == eventId }
                    if (currentEvent != null) {
                        Log.d("AppNavigation", "✅ Mise à jour réussie (pas d'erreur après 1s)")
                        Toast.makeText(context, "Événement mis à jour avec succès!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        hasInitiatedUpdate = false
                        updateStartTime = null
                        lastObservedEvent = currentEvent
                    }
                }
            }

            when {
                isLoadingEvent || (isLoading && selectedEvent == null) -> {
                    // Afficher un loading pendant le chargement
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                selectedEvent != null -> {
                    val scope = rememberCoroutineScope()
                    
                    EditEventScreen(
                        navController = navController,
                        event = selectedEvent!!,
                        onUpdate = { id: String, nom: String, description: String, dateDebut: String, dateFin: String, image: String?, lieu: String, categorie: String, statut: EventStatus ->
                            Log.d("AppNavigation", "🎯 onUpdate callback appelé")
                            Log.d("AppNavigation", "📝 ID reçu: $id")
                            Log.d("AppNavigation", "📝 Nom reçu: $nom")
                            Log.d("AppNavigation", "📝 Image reçue: ${if (image != null) "${image.take(50)}..." else "null"}")

                            // Marquer qu'une mise à jour a été initiée
                            hasInitiatedUpdate = true
                            updateStartTime = System.currentTimeMillis()
                            
                            // Sauvegarder l'état actuel de l'événement pour comparaison
                            selectedEvent?.let {
                                initialEventState = it
                            }

                            // Convertir l'image de manière asynchrone
                            scope.launch {
                                // Convertir l'URI local en Base64 si nécessaire
                                val validImage = if (image != null) {
                                    if (image.startsWith("http")) {
                                        // C'est déjà une URL, on l'utilise telle quelle
                                        Log.d("AppNavigation", "✅ Image URL: $image")
                                        image
                                    } else {
                                        // C'est un URI local, on le convertit en Base64
                                        try {
                                            Log.d("AppNavigation", "🖼️ Conversion URI en Base64 pour édition: $image")
                                            val uri = Uri.parse(image)
                                            val base64Image = uriToBase64(context, uri)
                                            if (base64Image != null) {
                                                val imageLength = base64Image.length
                                                Log.d("AppNavigation", "✅ Image convertie en Base64 ($imageLength caractères)")
                                            } else {
                                                Log.e("AppNavigation", "❌ Échec de la conversion Base64")
                                                Toast.makeText(context, "Erreur lors de la conversion de l'image", Toast.LENGTH_SHORT).show()
                                            }
                                            base64Image
                                        } catch (e: Exception) {
                                            Log.e("AppNavigation", "❌ Erreur conversion image: ${e.message}", e)
                                            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                                            null
                                        }
                                    }
                                } else {
                                    // Si aucune image n'est fournie, garder l'image existante
                                    Log.d("AppNavigation", "ℹ️ Aucune nouvelle image, conservation de l'image existante")
                                    selectedEvent?.image
                                }

                                Log.d("AppNavigation", "🚀 Mise à jour événement avec image: ${validImage != null}")
                                eventViewModel.updateEvent(
                                    event = selectedEvent!!,
                                    nom = nom,
                                    description = description,
                                    dateDebut = dateDebut,
                                    dateFin = dateFin,
                                    image = validImage,
                                    lieu = lieu,
                                    categorie = categorie,
                                    statut = statut
                                )

                                Log.d("AppNavigation", "✅ updateEvent() du ViewModel appelé (en attente de résultat)")
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> {
                    // Afficher un message d'erreur si l'événement n'est pas trouvé
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Événement introuvable",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "L'événement avec l'ID $eventId n'a pas pu être chargé.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Retour")
                            }
                        }
                    }
                }
            }
        }

        // 1️⃣6️⃣ Liste réclamations (RESTAURANT)
        composable("restaurant_reclamations") {
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager, context)
            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )
            val reclamations by vm.reclamations.collectAsState()
            val isLoading by vm.isLoading.collectAsState()

            LaunchedEffect(Unit) { vm.loadMyRestaurantReclamations() }

            ReclamationListRestaurantScreen(
                reclamations = reclamations,
                isLoading = isLoading,
                onReclamationClick = { rec ->
                    vm.selectReclamation(rec)
                    navController.navigate("restaurant_reclamation_detail/${rec.id}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 1️⃣7️⃣ Détail réclamation (RESTAURANT)
        composable(
            route = "restaurant_reclamation_detail/{reclamationId}",
            arguments = listOf(navArgument("reclamationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reclamationId = backStackEntry.arguments?.getString("reclamationId") ?: return@composable
            val context = LocalContext.current
            val tokenManager = TokenManager(context)
            val repository = ReclamationRepository(tokenManager, context)
            val vm: ReclamationsRestaurantViewModel = viewModel(
                factory = ReclamationsRestaurantViewModelFactory(repository)
            )
            val reclamations by vm.reclamations.collectAsState()
            val selectedReclamation by vm.selected.collectAsState()

            LaunchedEffect(reclamationId) {
                if (reclamations.isEmpty()) vm.loadMyRestaurantReclamations()
            }

            LaunchedEffect(reclamations) {
                if (reclamations.isNotEmpty() && selectedReclamation == null) {
                    reclamations.find { it.id == reclamationId }?.let { vm.selectReclamation(it) }
                }
            }

            if (selectedReclamation != null) {
                ReclamationDetailRestaurantScreen(
                    reclamation = selectedReclamation!!,
                    onBackClick = {
                        vm.clearSelected()
                        navController.popBackStack()
                    },
                    onRespond = { responseMessage ->
                        vm.respond(reclamationId, responseMessage) {
                            Toast.makeText(context, "Réponse envoyée!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }



        // 1️⃣8️⃣ Gestion PRO des deals
        composable("pro_deals") {
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                Log.d("AppNavigation", "📊 Chargement écran pro_deals")
                dealsViewModel.loadDeals()
            }

            ProDealsManagementScreen(
                viewModel = dealsViewModel,
                onAddDealClick = {
                    Log.d("AppNavigation", "➕ Navigation vers deal_add")
                    navController.navigate("deal_add")
                },
                onEditDealClick = { dealId ->
                    Log.d("AppNavigation", "✏️ Navigation vers deal_edit/$dealId")
                    navController.navigate("deal_edit/$dealId")
                },
                onDealClick = { dealId ->
                    Log.d("AppNavigation", "👁️ Navigation vers dealDetail/$dealId")
                    navController.navigate("dealDetail/$dealId")
                },
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis pro_deals")
                    navController.popBackStack()
                }
            )
        }

        composable("deal_add") {
            Log.d("AppNavigation", "➕ Écran d'ajout de deal")

            AddEditDealScreen(
                dealId = null,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis deal_add")
                    navController.popBackStack()
                }
            )
        }



        composable(
            route = "menu_order_route/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: ""
            val context = LocalContext.current

            val tokenManager = remember { TokenManager(context) }
            val userId = remember { tokenManager.getUserIdBlocking() ?: "" }

            val cartApiService = remember { RetrofitClient.cartApi }
            val cartRepository = remember { CartRepository(cartApiService, tokenManager) }
            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }

            val menuItemRepository = remember { MenuItemRepository(RetrofitClient.menuItemApi, Gson()) }

            val cartViewModel: CartViewModel = viewModel(
                factory = CartViewModelFactory(cartRepository, orderRepository, menuItemRepository, tokenManager, userId)
            )

            RestaurantMenuScreen(
                restaurantId = professionalId,
                onBackClick = { navController.popBackStack() },
                onViewCartClick = { navController.navigate("shopping_cart_route/$professionalId") },
                onConfirmOrderClick = {
                    navController.navigate("order_confirmation_route/$professionalId")
                },
                cartViewModel = cartViewModel,
                userId = userId
            )
        }

        composable(
            route = ProfileRoutes.PROFESSIONAL_PROFILE_EDIT, // e.g., "pro_profile_edit/{professionalId}"
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Restaurant ID is required for Pro profile edit.")


            ProfessionalProfileScreen(
                navController = navController,
                viewModel = viewModel(
                    factory = ProfessionalProfileViewModel.Factory(
                        tokenManager = TokenManager(context),
                        professionalApiService = RetrofitClient.professionalApiService,
                        postsApiService = RetrofitClient.postsApiService
                    )
                )
            )
        }

        // Professional Profile Management Screen (Intermediate screen)
        composable(
            route = ProRoutes.PROFESSIONAL_PROFILE_MANAGEMENT,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Professional ID is required for profile management.")

            ProfessionalProfileManagementScreen(
                navController = navController,
                professionalId = professionalId
            )
        }

        // Professional Profile Settings Screen
        composable(
            route = ProRoutes.PROFESSIONAL_PROFILE_SETTINGS,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Professional ID is required for profile settings.")

            ProfessionalProfileSettingsScreen(
                professionalId = professionalId,
                navController = navController
            )
        }

        // Professional Email & Name Update Screen
        composable(
            route = ProRoutes.PROFESSIONAL_EMAIL_NAME_UPDATE,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Professional ID is required for email/name update.")

            ProfessionalEmailNameUpdateScreen(
                navController = navController,
                professionalId = professionalId
            )
        }

        // Professional Change Password Screen
        composable(
            route = ProRoutes.PROFESSIONAL_CHANGE_PASSWORD,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Professional ID is required for password change.")

            ProfessionalChangePasswordScreen(
                navController = navController,
                professionalId = professionalId
            )
        }


        // 🧭 NEW ROUTE: The Cart/Order Confirmation Screen
        composable(
            route = "order_confirmation_route/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: ""
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val userId = remember { tokenManager.getUserIdBlocking() ?: "" }

            // Repositories
            val cartApiService = remember { RetrofitClient.cartApi }
            val cartRepository = remember { CartRepository(cartApiService, tokenManager) }
            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }

            // CartViewModel (for checkout)
            val menuItemRepository = remember { MenuItemRepository(RetrofitClient.menuItemApi, Gson()) }
            val cartVMFactory = remember { CartViewModelFactory(cartRepository, orderRepository, menuItemRepository, tokenManager, userId) }
            val cartViewModel: CartViewModel = viewModel(factory = cartVMFactory)

            OrderConfirmationScreen(
                cartViewModel = cartViewModel,
                professionalId = professionalId,
                onBackClick = { navController.popBackStack() },
                onOrderSuccess = {
                    navController.navigate(UserRoutes.ORDERS_ROUTE) {
                        popUpTo("menu_order_route/$professionalId") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 🧭 NEW ROUTE: Order History
        composable(UserRoutes.ORDERS_ROUTE) {
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val userId = remember { tokenManager.getUserIdBlocking() ?: "" }

            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }

            val orderViewModel: OrderViewModel = viewModel(
                factory = OrderViewModel.Factory(orderRepository)
            )

            OrderHistoryScreen(
                navController = navController,
                orderViewModel = orderViewModel,
                userId = userId,
                onOrderClick = { orderId ->
                    navController.navigate("order_details/$orderId")
                },
                onReclamationClick = { orderId ->
                    navController.navigate("create_reclamation/$orderId")
                },
                onLogout = {
                    // Perform logout logic here if needed
                }
            )
        }

        composable(
            route = "order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val userId = remember { tokenManager.getUserIdBlocking() ?: "" }
            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }
            val orderViewModel: OrderViewModel = viewModel(
                factory = OrderViewModel.Factory(orderRepository)
            )

            OrderDetailsScreen(
                orderId = orderId,
                navController = navController,
                orderViewModel = orderViewModel,
                userId = userId
            )
        }

        // Professional Order Details Route
        composable(
            route = "pro_order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }
            val orderViewModel: OrderViewModel = viewModel(
                factory = OrderViewModel.Factory(orderRepository)
            )

            com.example.damprojectfinal.professional.feature_order.ProfessionalOrderDetailsScreen(
                orderId = orderId,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }



        composable(
            route = "shopping_cart_route/{professionalId}",
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: ""
            val context = LocalContext.current
            val tokenManager = remember { TokenManager(context) }
            val userId = remember { tokenManager.getUserIdBlocking() ?: "" }

            val cartApiService = remember { RetrofitClient.cartApi }

            // Repositories
            val cartRepository = remember { CartRepository(cartApiService, tokenManager) }
            val orderApiService = remember { RetrofitClient.orderApi }
            val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }

            // ViewModel with userId and orderRepository
            val menuItemRepository = remember { MenuItemRepository(RetrofitClient.menuItemApi, Gson()) }
            val cartVMFactory = remember { CartViewModelFactory(cartRepository, orderRepository, menuItemRepository, tokenManager, userId) }
            val cartVM: CartViewModel = viewModel(factory = cartVMFactory)

            ShoppingCartScreen(
                navController = navController,
                cartVM = cartVM,
                professionalId = professionalId
            )
        }



        // 2️⃣0️⃣ Modifier un deal
        composable(
            route = "deal_edit/{dealId}",
            arguments = listOf(navArgument("dealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dealId = backStackEntry.arguments?.getString("dealId")

            Log.d("AppNavigation", "✏️ Écran d'édition de deal: $dealId")

            AddEditDealScreen(
                dealId = dealId,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis deal_edit")
                    navController.popBackStack()
                }
            )
        }

        // 2️⃣1️⃣ Liste des deals (pour les clients)
        composable("deals") {
            LaunchedEffect(Unit) {
                Log.d("AppNavigation", "📋 Chargement liste des deals")
                dealsViewModel.loadDeals()
            }

            DealsListScreen(
                viewModel = dealsViewModel,
                onDealClick = { dealId ->
                    Log.d("AppNavigation", "🔍 Navigation vers dealDetail/$dealId")
                    navController.navigate("dealDetail/$dealId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 2️⃣2️⃣ Détail d'un deal
        composable(
            route = "dealDetail/{dealId}",
            arguments = listOf(navArgument("dealId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dealId = backStackEntry.arguments?.getString("dealId") ?: ""

            Log.d("AppNavigation", "🔍 Détail du deal: $dealId")

            DealDetailScreen(
                dealId = dealId,
                viewModel = dealsViewModel,
                onBackClick = {
                    Log.d("AppNavigation", "⬅️ Retour depuis dealDetail")
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Convertit un URI local en Base64 pour l'envoi au backend
 */
internal fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (originalBitmap == null) return null

        // Redimensionner si nécessaire pour réduire la taille
        val maxSize = 1200
        val bitmap = if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
            val ratio = minOf(
                maxSize.toFloat() / originalBitmap.width,
                maxSize.toFloat() / originalBitmap.height
            )
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                originalBitmap.recycle()
            }
        } else {
            originalBitmap
        }

        // Compresser en JPEG
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Convertir en Base64
        val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        bitmap.recycle()
        outputStream.close()

        // Retourner au format data URI
        "data:image/jpeg;base64,$base64String"
    } catch (e: Exception) {
        Log.e("AppNavigation", "❌ Erreur conversion image: ${e.message}", e)
        null
    }
}

