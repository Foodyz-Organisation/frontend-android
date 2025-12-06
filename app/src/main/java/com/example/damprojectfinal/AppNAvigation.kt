package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.example.damprojectfinal.core.repository.OrderRepository
import com.example.damprojectfinal.user.feautre_order.viewmodel.OrderViewModel
import com.example.damprojectfinal.user.feautre_order.ui.OrderHistoryScreen
import com.example.damprojectfinal.user.feautre_order.ui.OrderDetailsScreen
import com.example.damprojectfinal.core.utils.LogoutViewModelFactory
import com.example.damprojectfinal.feature_auth.ui.*
import com.example.damprojectfinal.feature_auth.viewmodels.LogoutViewModel
import com.example.damprojectfinal.professional.common.HomeScreenPro
import com.example.damprojectfinal.professional.feature_menu.ui.MenuItemManagementScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.CreateMenuItemScreen
import com.example.damprojectfinal.professional.feature_menu.ui.components.ItemDetailsScreen
import com.example.damprojectfinal.professional.feature_menu.viewmodel.MenuViewModel
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.user.feature_profile.ui.UserProfileScreen
import com.example.damprojectfinal.user.feature_profile.viewmodel.ProfileViewModel
import com.google.gson.Gson
import com.example.damprojectfinal.core.`object`.KtorClient
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileScreen
import com.example.damprojectfinal.professional.feature_profile.ui.mockChilis
import com.example.damprojectfinal.user.feature_cart_item.ui.ShoppingCartScreen
import com.example.damprojectfinal.user.feature_menu.ui.RestaurantMenuScreen
import com.example.damprojectfinal.user.feature_pro_profile.ui.ClientRestaurantProfileScreen
import com.example.damprojectfinal.user.feautre_order.ui.OrderConfirmationScreen
import com.example.damprojectfinal.core.repository.CartRepository
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModel
import com.example.damprojectfinal.user.feature_cart_item.viewmodel.CartViewModelFactory

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

    const val ORDERS_SCREEN = "orders"

    const val ORDERS_ROUTE = "orders_history_route"


}

object ProRoutes {
    const val MENU_MANAGEMENT = "menu_management/{professionalId}"

    const val CART_ROUTE = "shopping_cart_route"

}

object ProfileRoutes {
    const val PROFESSIONAL_PROFILE_EDIT = "pro_profile_edit/{professionalId}"
    const val CLIENT_PROFILE_VIEW = "client_profile_view/{professionalId}"
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
    
    // Order Repository
    val orderApiService = remember { RetrofitClient.orderApi }
    val orderRepository = remember { OrderRepository(orderApiService, tokenManager) }
    
    // Debugger runs at the highest level
    DebugUserLogger(tokenManager = tokenManager)

    val ServiceLocator = KtorClient

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
            val context = LocalContext.current

            // Fix 1: Ensure Factory uses the correct constructor (referencing the logic from your newer block)
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, context)
            )

            // Fix 2: Remove arguments from fetchUserProfile.
            // The ViewModel now handles token/userId internally via TokenManager.
            LaunchedEffect(userId) {
                profileViewModel.fetchUserProfile()
            }

            // --- Composable Call ---
            UserProfileScreen(
                viewModel = profileViewModel,
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
            route = UserRoutes.PROFILE_VIEW,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->

            // 1. GET CONTEXT: Required for the ViewModel Factory (and TokenManager inside the VM)
            val context = LocalContext.current

            val userId = backStackEntry.arguments?.getString("userId")
                ?: throw IllegalStateException("userId is required for profile view.")

            // 2. VIEWMODEL INITIALIZATION: Pass both the repository and the context
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Companion.Factory(userRepository, context)
            )

            // The ViewModel handles token retrieval internally now.

            // 3. DATA FETCHING: Call the parameter-less fetchUserProfile()
            LaunchedEffect(userId) {
                // The ViewModel will use the context to get the stored User ID and Token
                if (profileViewModel.userState.value == null) {
                    profileViewModel.fetchUserProfile()
                }
            }

            // --- Corrected Composable Call ---
            UserProfileScreen(
                viewModel = profileViewModel,

                // Implement the onBackClick action to return to the previous screen
                onBackClick = { navController.popBackStack() },

                onEditProfileClick = {
                    navController.navigate("profile_update/$userId")
                }
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
                // Assuming HomeScreenPro also takes a logoutSuccess parameter if needed:
                // , logoutSuccess = logoutViewModel.logoutSuccess
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






        // ----------------------------------------------------
// ⭐ 1. PROFESSIONAL PROFILE EDIT SCREEN (Editable) ⭐
// ----------------------------------------------------
        composable(
            route = ProfileRoutes.PROFESSIONAL_PROFILE_EDIT, // e.g., "pro_profile_edit/{restaurantId}"
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                ?: throw IllegalStateException("Restaurant ID is required for Pro profile edit.")

            // TODO: Fetch the specific restaurant details for editing
            val restaurantDetails = mockChilis // Using mock data for compilation

            ProfessionalProfileScreen(
                restaurantDetails = restaurantDetails,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { editedState ->
                    // TODO: Call ViewModel to save changes
                    println("Pro Profile Saved: ${editedState.name}")
                    // Optional: navController.popBackStack()
                }
            )
        }

        composable(
            route = ProfileRoutes.CLIENT_PROFILE_VIEW, // "client_profile_view/{professionalId}"
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId")
                ?: throw IllegalStateException("Professional ID is required for client profile view.")

            // TODO: Fetch the professional details for viewing
            val restaurantDetails = mockChilis

            ClientRestaurantProfileScreen(
                professionalId = professionalId,
                restaurantDetails = restaurantDetails,
                onBackClick = { navController.popBackStack() },
                onViewMenuClick = { id ->
                    // Navigate to the existing menu/order screen
                    navController.navigate("menu_order_route/$id")
                }
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
    }
}
