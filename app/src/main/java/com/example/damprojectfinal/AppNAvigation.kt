package com.example.damprojectfinal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.damprojectfinal.core.api.AuthApiService
import com.example.damprojectfinal.core.api.TokenManager
import com.example.damprojectfinal.core.api.posts.RetrofitClient
import com.example.damprojectfinal.feature_auth.ui.ForgetPasswordScreen
import com.example.damprojectfinal.feature_auth.ui.LoginScreen
import com.example.damprojectfinal.feature_auth.ui.SignupScreen
import com.example.damprojectfinal.feature_auth.ui.SplashScreen
import com.example.damprojectfinal.feature_auth.ui.ProSignupScreen
import com.example.damprojectfinal.feature_profile.ui.ProfileScreen
import com.example.damprojectfinal.professional.common.HomeScreenPro
// ADJUSTED IMPORT for CreateContentScreen to match your provided path
import com.example.damprojectfinal.professional.feature_posts.CreateContentScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileScreen
import com.example.damprojectfinal.professional.feature_profile.ui.ProfessionalProfileViewModel
import com.example.damprojectfinal.user.common.HomeScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.CreatePostScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.CaptionAndPublishScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.EditPostScreen
import com.example.damprojectfinal.user.feature_posts.ui.post_management.PostDetailsScreen
import com.example.damprojectfinal.user.feature_posts.ui.reel_management.ReelsScreen
import com.example.damprojectfinal.user.feature_posts.ui.trends.TrendsScreen

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
    const val CREATE_POST = "create_post_route"

    const val CAPTION_PUBLISH_SCREEN = "caption_publish_route"
    const val MEDIA_URI_ARG = "mediaUri"

    const val REELS_SCREEN = "reels_screen"

    const val EDIT_POST_SCREEN = "edit_post_screen/{postId}/{initialCaption}"

    const val POST_DETAILS_SCREEN = "post_details_screen"

    const val PROFILE_SCREEN = "profile_screen"

    const val TRENDS_SCREEN = "trends_screen"
}

object ProRoutes {
    const val CREATE_CONTENT_SCREEN = "create_content_screen"

    const val PROFESSIONAL_PROFILE_SCREEN = "professional_profile_screen/{professionalId}"

}


@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authApiService = AuthApiService() // Initialized but only used in some screens directly
    val context = LocalContext.current // <--- 'context' is correctly defined here
    val tokenManager = remember { TokenManager(context) } // Ensure TokenManager is accessible
    val postsApiService = remember { RetrofitClient.postsApiService }
    val startDestination = AuthRoutes.SPLASH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier // Modifier passed to NavHost
    ) {
        // 1️⃣ Splash Screen
        composable(AuthRoutes.SPLASH) {
            SplashScreen(
                durationMs = 1600,
                onFinished = {
                    navController.navigate(AuthRoutes.LOGIN) {
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

        // 7️⃣ Professional Signup Screen
        composable(AuthRoutes.PRO_SIGNUP) {
            ProSignupScreen(
                navController = navController,
                authApiService = authApiService
            )
        }

        // 🆕 Create Content Screen (for professional users to choose what to create)
        composable(ProRoutes.CREATE_CONTENT_SCREEN) {
            CreateContentScreen(navController = navController)
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

        composable(UserRoutes.TRENDS_SCREEN) {
            TrendsScreen(navController = navController)
        }

        composable(UserRoutes.PROFILE_SCREEN) {
            ProfileScreen(navController = navController)
        }

        composable(
            route = ProRoutes.PROFESSIONAL_PROFILE_SCREEN,
            arguments = listOf(navArgument("professionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString("professionalId") ?: "unknown"
            // Provide ViewModel with factory
            ProfessionalProfileScreen(
                navController = navController,
                professionalId = professionalId,
                viewModel = viewModel(
                    factory = ProfessionalProfileViewModel.Factory(tokenManager)
                )
            )
        }

    }
}
