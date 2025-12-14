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
import androidx.compose.runtime.setValue
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
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_posts.CreateContentScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileScreen
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
import com.example.damprojectfinal.user.feature_posts.ui.post_management.CreatePostScreen
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
    const val ORDERS_SCREEN = "orders"
    const val ORDERS_ROUTE = "orders_history_route"
}

object ProRoutes {
    const val MENU_MANAGEMENT = "menu_management/{professionalId}"
    const val CART_ROUTE = "shopping_cart_route"
    const val CREATE_CONTENT_SCREEN = "create_content_screen"
    const val PROFESSIONAL_PROFILE_SCREEN = "professional_profile_screen/{professionalId}"
    const val ALL_PROFILE_POSTS = "all_profile_posts"
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

                        loyaltyData = balance?.let {
                            LoyaltyData(
                                loyaltyPoints = it.loyaltyPoints,
                                validReclamations = it.validReclamations,
                                invalidReclamations = it.invalidReclamations,
                                reliabilityScore = it.reliabilityScore,
                                availableRewards = emptyList(),
                                history = emptyList()
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

        // 1️⃣2️⃣ Créer réclamation (CLIENT)
        composable(
            route = "create_reclamation?orderId={orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
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

            ReclamationTemplateScreen(
                complaintTypes = listOf("Livraison en retard", "Article manquant", "Problème de qualité", "Autre"),
                commandeconcernees = listOf("Commande #12345", "Commande #12346", "Commande #12347", "Commande #12348"),
                onSubmit = { commandeConcernee, complaintType, description, photos ->
                    vm.createReclamation(
                        commandeConcernee = commandeConcernee,
                        complaintType = complaintType,
                        description = description,
                        photoUris = photos
                    ) { reclamation ->
                        Toast.makeText(context, "Réclamation créée avec succès !", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                },
                initialOrderId = orderId
            )
        }

        // 1️⃣3️⃣ Liste événements
        composable("event_list") {
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) { eventViewModel.loadEvents() }

            EventListScreen(
                events = events,
                onEventClick = { event -> navController.navigate("event_detail/${event._id}") },
                onAddEventClick = {},
                onEditClick = { event -> },
                onDeleteClick = { eventId -> eventViewModel.deleteEvent(eventId) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 1️⃣4️⃣ Détail événement
        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            val eventViewModel: EventViewModel = viewModel()
            val events by eventViewModel.events.collectAsState()
            val selectedEvent = events.find { it._id == eventId }

            if (selectedEvent != null) {
                EventDetailScreen(event = selectedEvent, onBackClick = { navController.popBackStack() })
            }
        }

        // 1️⃣5️⃣ Créer événement
        composable("create_event") {
            val context = LocalContext.current
            val eventViewModel: EventViewModel = viewModel()

            CreateEventScreen(
                navController = navController,
                onSubmit = { nom, description, dateDebut, dateFin, image, lieu, categorie, statut ->
                    // ⚠️ Backend validation: image must be a valid URL (http/https).
                    // Local content:// URIs will cause 400 Bad Request.
                    // Temporary fix: Send null if not a web URL.
                    val validImage = if (image?.startsWith("http") == true) image else null

                    if (image != null && validImage == null) {
                        Toast.makeText(context, "L'envoi d'images locales n'est pas encore supporté.", Toast.LENGTH_SHORT).show()
                    }

                    eventViewModel.createEvent(
                        nom,
                        description,
                        dateDebut,
                        dateFin,
                        validImage,
                        lieu,
                        categorie,
                        statut,
                        onSuccess = {
                            Toast.makeText(context, "Événement créé avec succès!", Toast.LENGTH_SHORT).show()
                            navController.navigate("event_list") {
                                popUpTo("create_event") { inclusive = true }
                            }
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 1️⃣5️⃣ Liste événements (PROFESSIONAL) - ✅ AVEC ViewModel partagé
        composable("event_list_remote") {
            val eventViewModel: EventViewModel = viewModel() // ✅ ViewModel partagé
            val events by eventViewModel.events.collectAsState()
            val isLoading by eventViewModel.isLoading.collectAsState()
            val error by eventViewModel.error.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                eventViewModel.loadEvents()
            }

            EventListScreenRemote(
                events = events,
                isLoading = isLoading,
                error = error,
                onEventClick = { event ->
                    // Navigation vers les détails (si nécessaire)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onAddEventClick = {
                    navController.navigate("create_event")
                },
                onEditEventClick = { event ->
                    navController.navigate("edit_event/${event._id}")
                },
                onDeleteEventClick = { eventId ->
                    eventViewModel.deleteEvent(
                        eventId,
                        onSuccess = {
                            Toast.makeText(context, "Événement supprimé", Toast.LENGTH_SHORT).show()
                        },
                        onError = { msg ->
                            Toast.makeText(context, "Erreur suppression: $msg", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }

// 🆕 Route pour l'édition d'un événement - ✅ UTILISE le ViewModel partagé
        composable(
            route = "edit_event/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable

            // ✅ IMPORTANT: Récupérer le ViewModel du parent NavBackStackEntry
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("event_list_remote")
            }
            val eventViewModel: EventViewModel = viewModel(parentEntry) // ✅ ViewModel partagé!

            val events by eventViewModel.events.collectAsState()
            val context = LocalContext.current

            // Charger les événements si nécessaire
            LaunchedEffect(Unit) {
                if (events.isEmpty()) {
                    eventViewModel.loadEvents()
                }
            }

            // Trouver l'événement à éditer
            val selectedEvent = events.find { it._id == eventId }

            if (selectedEvent != null) {
                EditEventScreen(
                    navController = navController,
                    event = selectedEvent,
                    onUpdate = { id, nom, description, dateDebut, dateFin, image, lieu, categorie, statut ->
                        Log.d("AppNavigation", "🎯 onUpdate callback appelé")
                        Log.d("AppNavigation", "📝 ID reçu: $id")
                        Log.d("AppNavigation", "📝 Nom reçu: $nom")

                        // ⚠️ Validation Image
                        val validImage = if (image?.startsWith("http") == true) image else null
                        if (image != null && validImage == null) {
                            Toast.makeText(context, "L'envoi d'images locales n'est pas encore supporté.", Toast.LENGTH_SHORT).show()
                        }

                        eventViewModel.updateEvent(
                            id = id,
                            nom = nom,
                            description = description,
                            dateDebut = dateDebut,
                            dateFin = dateFin,
                            image = validImage,
                            lieu = lieu,
                            categorie = categorie,
                            statut = statut,
                            onSuccess = {
                                Log.d("AppNavigation", "✅ Mise à jour réussie")
                                Toast.makeText(context, "Événement mis à jour avec succès!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { errorMsg ->
                                Log.e("AppNavigation", "❌ Échec de la mise à jour: $errorMsg")
                                Toast.makeText(context, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        )

                        Log.d("AppNavigation", "✅ updateEvent() du ViewModel appelé (en attente de résultat)")
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Afficher un loading pendant le chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                    navController.navigate("create_reclamation?orderId=$orderId")
                },
                onLogout = {
                    // Perform logout logic here if needed
                }
            )
        }

        // Order Details Route
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
